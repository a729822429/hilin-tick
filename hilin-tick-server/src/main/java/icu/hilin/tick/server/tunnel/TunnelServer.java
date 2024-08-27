package icu.hilin.tick.server.tunnel;

import cn.hutool.core.util.IdUtil;
import icu.hilin.tick.core.ChannelDataCache;
import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.request.ChannelCloseRequest;
import icu.hilin.tick.core.entity.request.ChannelConnectedRequest;
import icu.hilin.tick.core.entity.request.ChannelDataRequest;
import icu.hilin.tick.core.entity.response.AuthResponse;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Component
@Slf4j
public class TunnelServer {

    private static final Map<Long, AuthResponse.TunnelInfo> CHANNEL_TUNNEL_INFO = new ConcurrentHashMap<>();

    public TunnelServer() {
        // 隧道服务启动监听
        TickConstant.EVENT_BUS.consumer(String.format(TickConstant.TUNNEL_SERVER_ALL, "start"), (Handler<Message<Buffer>>) event -> {
            AuthResponse response = new AuthResponse((byte) 1, event.body());
            List<AuthResponse.TunnelInfo> tunnels = response.toDataEntity();
            for (AuthResponse.TunnelInfo tunnel : tunnels) {
                if (tunnel.getType() == 1) {
                    startTcpTunnelServer(tunnel);
                } else {
                    startUdpTunnelServer(tunnel);
                }
            }
        });
    }

    public void startTcpTunnelServer(final AuthResponse.TunnelInfo tunnelInfo) {
        TickConstant.VERTX.createNetServer()
                .connectHandler(socket -> {

                    // channel状态 1在线，2离线
                    LongAdder channelResult = new LongAdder();

                    // 连接进入，创建连接id
                    final long channelID = IdUtil.getSnowflakeNextId();
                    CHANNEL_TUNNEL_INFO.put(channelID, tunnelInfo);

                    // 收到数据
                    final LongAdder seq = new LongAdder();
                    socket.handler(buf -> {
                        log.info("发送通道连接数据 channelID : {}", channelID);
                        synchronized (seq) {
                            ChannelDataRequest.ChannelData channelData1 = new ChannelDataRequest.ChannelData();
                            channelData1.setChannelID(channelID);
                            seq.add(1);
                            channelData1.setSeq(seq.longValue());
                            channelData1.setRequestData(buf);
                            ChannelDataRequest request = new ChannelDataRequest(channelData1);
                            TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", tunnelInfo.getClientID()), request.toBuf());
                        }
                    });


                    // 通知客户端连接进入
                    ChannelConnectedRequest.ChannelData channelData = new ChannelConnectedRequest.ChannelData();
                    channelData.setClientID(tunnelInfo.getClientID());
                    channelData.setTunnelID(tunnelInfo.getTunnelId());
                    channelData.setChannelID(channelID);
                    Buffer connectorBuf = new ChannelConnectedRequest(channelData).toBuf();

                    log.info("发送通道连接信息 channelID : {}", channelID);

                    TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", tunnelInfo.getClientID()), connectorBuf);

                    // 连接进入
                    List<MessageConsumer<Buffer>> consumers = new ArrayList<>();


                    ChannelDataCache cache = new ChannelDataCache();

                    // 发送数据到外网客户
                    consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_SERVER, "send", channelID), event -> {
                        cache.add(event.body());
                    }));

                    // 关闭连接
                    consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_SERVER, "close", channelID), event -> {
                        socket.close();
                    }));

                    socket.closeHandler(v -> {

                        channelResult.reset();
                        channelResult.add(2);

                        CHANNEL_TUNNEL_INFO.remove(channelID);

                        consumers.forEach(MessageConsumer::unregister);

                        ChannelCloseRequest.ChannelData channelData1 = new ChannelCloseRequest.ChannelData();
                        channelData.setClientID(tunnelInfo.getClientID());
                        channelData.setTunnelID(channelID);
                        channelData.setChannelID(channelID);
                        // 通知客户端隧道已关闭
                        TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", channelID), new ChannelCloseRequest(channelData1).toBuf());
                    });

                    new Thread(() -> {
                        LongAdder currentSeq = new LongAdder();


                        while (true) {

                            // 如果连接断开，跳出循环
                            if (channelResult.longValue() == 2) {
                                break;
                            }
                            try {
                                sendAndWrite(socket, currentSeq, cache);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }).start();
                })
                .listen(tunnelInfo.getRemotePort(), "0.0.0.0", r -> {
                    if (r.succeeded()) {
                        // 启动成功
                        log.info("启动隧道成功 {}", tunnelInfo.getRemotePort());
                    } else {
                        // 启动失败
                        log.info("启动隧道失败 {}", tunnelInfo.getRemotePort());
                    }
                });
    }

    public void startUdpTunnelServer(AuthResponse.TunnelInfo tunnelInfo) {
        TickConstant.VERTX.createDatagramSocket()
                .listen(tunnelInfo.getRemotePort(), "0.0.0.0", r -> {
                    if (r.succeeded()) {
                        // 启动成功
                    } else {
                        // 启动失败
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
}