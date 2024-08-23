package icu.hilin.tick.client.handler;

import cn.hutool.json.JSONUtil;
import icu.hilin.tick.client.ChannelDataCache;
import icu.hilin.tick.core.BufferUtils;
import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.ChannelCloseRequest;
import icu.hilin.tick.core.entity.request.ChannelConnectedRequest;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.entity.response.ChannelDataResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@Component
@Slf4j
public class ChannelConnectorHandler extends BaseCmdHandler<ChannelConnectedRequest> {

    private static final Map<Long, Object> LOCKS = new HashMap<>();

    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return BaseEntity.TYPE_REQUEST_CHANNEL_CONNECTOR_DATA == body.getByte(0);
    }

    @Override
    public ChannelConnectedRequest buffer2Entity(Long clientID, Buffer body) {
        BufferUtils.printBuffer("收到隧道连接信息", body);
        return new ChannelConnectedRequest(body);
    }

    @Override
    public void doDeal(Long clientID, ChannelConnectedRequest entity) {
        // 当前序列号，初始0，依次加1
        final LongAdder currentSeq = new LongAdder();

        final ChannelDataCache channelDataCache = new ChannelDataCache();


        log.info("开始启动通道");
        ChannelConnectedRequest.ChannelData channelData = entity.toDataEntity();

        LOCKS.put(channelData.getChannelID(), new Object());

        AuthResponse.TunnelInfo tunnel = AuthResponseHandler.getTUNNEL(channelData.getTunnelID());

        List<MessageConsumer<?>> consumers = new ArrayList<>();

        // 存放通道启动结果 1成功 2失败 0正在启动
        LongAdder channelResult = new LongAdder();

        // 连接内网服务器
        TickConstant.VERTX.createNetClient(new NetClientOptions().setRegisterWriteHandler(true)).connect(tunnel.getTargetPort(), tunnel.getTargetHost(), r -> {
            if (r.succeeded()) {
                log.info("通道连接成功");
                // 连接成功
                final NetSocket socket = r.result();

                socket.handler(buf -> {
                    BufferUtils.printBuffer("收到通道数据", buf);

                    // 收到目的服务器的数据
                    ChannelDataResponse.ChannelData responseData = new ChannelDataResponse.ChannelData();
                    responseData.setChannelID(channelData.getChannelID());
                    responseData.setResponseData(buf);
                    ChannelDataResponse response = new ChannelDataResponse(responseData);
                    // 封装后发送到服务器
                    TickConstant.EVENT_BUS
                            .publish(String.format(TickConstant.CMD_CLIENT_ALL, "send"), response.toBuf());
                });

                // 关闭通道
                consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "close", channelData.getChannelID()), event -> {
                    socket.close();
                }));

                // 监听需要发送的数据
                consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "send", channelData.getChannelID()), (Handler<Message<Buffer>>) event -> {
                    addChannelDataCache(channelData.getChannelID(), channelDataCache, event.body());
                    // todo
                    if (channelResult.intValue() == 1) {
                        // 连接成功了，发送数据
                        sendAndWrite(socket, channelData.getChannelID(), currentSeq, channelDataCache);
                    }
                }));
                socket.closeHandler(v -> {
                    LOCKS.remove(channelData.getChannelID());
                    consumers.forEach(MessageConsumer::unregister);
                    ChannelCloseRequest.ChannelData closeChannel = new ChannelCloseRequest.ChannelData();
                    closeChannel.setClientID(clientID);
                    closeChannel.setTunnelID(tunnel.getTunnelId());
                    closeChannel.setChannelID(channelData.getChannelID());
                    ChannelCloseRequest request = new ChannelCloseRequest(closeChannel);
                    TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_CLIENT_ALL, "send"), request.toBuf());
                });
                // 连接成功，发送缓存中的数据
                sendAndWrite(socket, channelData.getChannelID(), currentSeq, channelDataCache);

                channelResult.add(1);
            } else {
                log.info("通道连接失败 {}", JSONUtil.toJsonStr(tunnel), r.cause());
                consumers.forEach(MessageConsumer::unregister);
                channelResult.add(2);
            }
        });

    }


    public static void sendAndWrite(NetSocket socket, Long channelID, LongAdder currentSeq, ChannelDataCache bufCache) {
        while (true) {
            synchronized (LOCKS.get(channelID)) {
                Buffer buffer = bufCache.getFirst();
                bufCache.removeFirst();
            }
        }
    }

    public static void addChannelDataCache(Long channelID, ChannelDataCache bufCache, Buffer buf) {
        synchronized (LOCKS.get(channelID)) {
            bufCache.add(buf);
        }
    }
}
