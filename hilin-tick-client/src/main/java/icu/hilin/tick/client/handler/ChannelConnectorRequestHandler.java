package icu.hilin.tick.client.handler;

import cn.hutool.json.JSONUtil;
import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.ChannelCloseRequest;
import icu.hilin.tick.core.entity.request.ChannelConnectedRequest;
import icu.hilin.tick.core.entity.request.ChannelDataRequest;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.netty.buffer.Unpooled;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

@Component
@Slf4j
public class ChannelConnectorRequestHandler extends BaseCmdHandler<ChannelConnectedRequest> {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return BaseEntity.TYPE_REQUEST_CHANNEL_CONNECTOR_DATA == body.getByte(0);
    }

    @Override
    public ChannelConnectedRequest buffer2Entity(Long clientID, Buffer body) {
        return new ChannelConnectedRequest(body);
    }

    @Override
    public void doDeal(Long clientID, ChannelConnectedRequest entity) {
        ChannelConnectedRequest.ChannelData channelData = entity.toDataEntity();
        long channelID = channelData.getChannelID();
        List<MessageConsumer<?>> consumers = new ArrayList<>();
        log.info("开始启动通道 {}", channelID);
        AuthResponse.TunnelInfo tunnel = AuthResponseHandler.getTUNNEL(channelData.getTunnelID());


        // channel状态 1在线，2离线
        LongAdder channelResult = new LongAdder();

        List<Buffer> cacheBuf = new ArrayList<>();
        consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "receive", channelID), (Handler<Message<Buffer>>) event -> {
            synchronized (cacheBuf) {
                cacheBuf.add(event.body());
                TickConstant.EVENT_BUS.publish(String.format(TickConstant.CHANNEL_CLIENT, "send", channelID), Buffer.buffer());
            }
        }));

        // 连接内网服务器
        TickConstant.VERTX.createNetClient(new NetClientOptions().setRegisterWriteHandler(true)).connect(tunnel.getTargetPort(), tunnel.getTargetHost(), r -> {
            if (r.succeeded()) {
                log.info("通道连接成功 channelID:{}", channelID);
                // 连接成功
                final NetSocket socket = r.result();

                LongAdder sendSeq = new LongAdder();
                // 关闭通道
                consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "close", channelID), event -> {
                    log.info("收到通道关闭信息 {}", channelID);
                    socket.close();
                }));

                consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "send", channelID), (Handler<Message<Buffer>>) event -> {
                    synchronized (cacheBuf) {
                        if (channelResult.longValue() == 1) {
                            cacheBuf.forEach(socket::write);
                            cacheBuf.clear();
                        }
                    }
                }));

                socket.handler(buf -> {
                    log.info("收到通道数据 channelID:{}", channelID);
                    // 收到目的服务器的数据
                    ChannelDataRequest.ChannelData responseData = new ChannelDataRequest.ChannelData();
                    responseData.setChannelID(channelID);
                    responseData.setRequestData(buf);
                    ChannelDataRequest response = new ChannelDataRequest(responseData);
                    Buffer.buffer(Unpooled.buffer());
                    // 封装后发送到服务器
                    TickConstant.EVENT_BUS
                            .publish(String.format(TickConstant.CMD_CLIENT_ALL, "send"), response.toBuf());
                });


                socket.closeHandler(v -> {
                    channelResult.reset();
                    channelResult.add(2);
                    consumers.forEach(MessageConsumer::unregister);

                    log.info("通道关闭 {}", channelID);

                    ChannelCloseRequest.ChannelData closeChannel = new ChannelCloseRequest.ChannelData();
                    closeChannel.setChannelID(channelID);
                    ChannelCloseRequest request = new ChannelCloseRequest(closeChannel);
                    TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_CLIENT_ALL, "send"), request.toBuf());
                });
                channelResult.reset();
                channelResult.add(1);

                synchronized (cacheBuf) {
                    cacheBuf.forEach(socket::write);
                    cacheBuf.clear();
                }
            } else {
                log.info("通道连接失败 {}", JSONUtil.toJsonStr(tunnel), r.cause());
                consumers.forEach(MessageConsumer::unregister);
            }
        });

    }

}
