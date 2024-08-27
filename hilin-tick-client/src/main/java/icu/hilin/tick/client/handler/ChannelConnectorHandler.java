package icu.hilin.tick.client.handler;

import cn.hutool.json.JSONUtil;
import icu.hilin.tick.core.ChannelDataCache;
import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.ChannelCloseRequest;
import icu.hilin.tick.core.entity.request.ChannelConnectedRequest;
import icu.hilin.tick.core.entity.request.ChannelDataRequest;
import icu.hilin.tick.core.entity.response.AuthResponse;
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
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

@Component
@Slf4j
public class ChannelConnectorHandler extends BaseCmdHandler<ChannelConnectedRequest> {

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

        final ChannelDataCache channelDataCache = new ChannelDataCache();


        ChannelConnectedRequest.ChannelData channelData = entity.toDataEntity();
        long channelID = channelData.getChannelID();
        List<MessageConsumer<?>> consumers = new ArrayList<>();
        // 监听需要发送的数据
        consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "send", channelID), (Handler<Message<Buffer>>) event -> {
            addChannelDataCache(channelDataCache, event.body());
        }));

        log.info("开始启动通道 {}", channelID);
        AuthResponse.TunnelInfo tunnel = AuthResponseHandler.getTUNNEL(channelData.getTunnelID());



        // channel状态 1在线，2离线
        LongAdder channelResult = new LongAdder();

        // 连接内网服务器
        TickConstant.VERTX.createNetClient(new NetClientOptions().setRegisterWriteHandler(true)).connect(tunnel.getTargetPort(), tunnel.getTargetHost(), r -> {
            if (r.succeeded()) {
                log.info("通道连接成功 channelID:{}", channelID);
                // 连接成功
                final NetSocket socket = r.result();

                LongAdder sendSeq = new LongAdder();
                // 关闭通道
                consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "close", channelID), event -> {
                    socket.close();
                }));


                socket.handler(buf -> {
                    log.info("收到通道数据 channelID:{}", channelID);
                    synchronized (sendSeq) {
                        sendSeq.add(1);
                        // 收到目的服务器的数据
                        ChannelDataRequest.ChannelData responseData = new ChannelDataRequest.ChannelData();
                        responseData.setChannelID(channelID);
                        responseData.setSeq(sendSeq.longValue());
                        responseData.setRequestData(buf);
                        ChannelDataRequest response = new ChannelDataRequest(responseData);
                        // 封装后发送到服务器
                        TickConstant.EVENT_BUS
                                .publish(String.format(TickConstant.CMD_CLIENT_ALL, "send"), response.toBuf());
                    }
                });


                socket.closeHandler(v -> {
                    channelResult.reset();
                    channelResult.add(2);
                    consumers.forEach(MessageConsumer::unregister);

                    ChannelCloseRequest.ChannelData closeChannel = new ChannelCloseRequest.ChannelData();
                    closeChannel.setChannelID(channelID);
                    ChannelCloseRequest request = new ChannelCloseRequest(closeChannel);
                    TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_CLIENT_ALL, "send"), request.toBuf());
                });
                // 连接成功，发送缓存中的数据
                new Thread(() -> {
                    LongAdder currentSeq = new LongAdder();
                    while (true) {
                        // 如果连接断开，跳出循环
                        if (channelResult.longValue() == 2) {
                            break;
                        }
                        try {
                            sendAndWrite(socket, currentSeq, channelDataCache);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }).start();

            } else {
                log.info("通道连接失败 {}", JSONUtil.toJsonStr(tunnel), r.cause());
                consumers.forEach(MessageConsumer::unregister);
            }
        });

    }


    private static void sendAndWrite(NetSocket socket, LongAdder currentSeq, ChannelDataCache bufCache) throws InterruptedException {
        // 如果没有数据，返回-1，跳出循环
        if (bufCache.isEmpty()) {
            return;
        }
        Buffer buffer = bufCache.getFirst();
        long seq = buffer.getLong(0);
        if (currentSeq.longValue() + 1 == seq) {
            // 如果序号是相邻的，直接发送
            socket.write(buffer.getBuffer(8, buffer.length()));
            // 发送之后序号加1
            currentSeq.add(1);
            bufCache.removeFirst();
        } else if (currentSeq.longValue() >= seq) {
            // 如果当前序号大于buf序号,抛弃当前buf
            bufCache.removeFirst();
        } else {
            // 如果中间差了序号，等待100毫秒，但是不跳出循环
            Thread.sleep(100);
            // 再次判断序列号，如果还是不对，跳过这个序列号
            buffer = bufCache.getFirst();
            seq = buffer.getLong(0);
            if (currentSeq.longValue() + 1 != seq) {
                currentSeq.add(1);
            } else if (seq <= currentSeq.longValue()) {
                bufCache.removeFirst();
            }
        }
    }

    private static void addChannelDataCache(ChannelDataCache bufCache, Buffer buf) {
        bufCache.add(buf);
    }
}
