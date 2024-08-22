package icu.hilin.tick.server.tunnel;

import cn.hutool.core.util.IdUtil;
import icu.hilin.tick.core.BufferUtils;
import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.request.ChannelCloseRequest;
import icu.hilin.tick.core.entity.request.ChannelConnectedRequest;
import icu.hilin.tick.core.entity.request.ChannelDataRequest;
import icu.hilin.tick.core.entity.response.AuthResponse;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                    // 连接进入，创建连接id
                    final long channelID = IdUtil.getSnowflakeNextId();
                    CHANNEL_TUNNEL_INFO.put(channelID, tunnelInfo);

                    // 通知客户端连接进入
                    ChannelConnectedRequest.ChannelData channelData = new ChannelConnectedRequest.ChannelData();
                    channelData.setClientID(tunnelInfo.getClientID());
                    channelData.setTunnelID(tunnelInfo.getTunnelId());
                    channelData.setChannelID(channelID);
                    Buffer connectorBuf = new ChannelConnectedRequest(channelData).toBuf();
                    BufferUtils.printBuffer("发送隧道连接信息", connectorBuf);
                    TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", tunnelInfo.getClientID()), connectorBuf);

                    // 连接进入
                    List<MessageConsumer<Buffer>> consumers = new ArrayList<>();

                    // 收到数据
                    socket.handler(buf -> {
                        ChannelDataRequest.ChannelData channelData1 = new ChannelDataRequest.ChannelData();
                        channelData1.setChannelID(channelID);
                        channelData1.setRequestData(buf);
                        ChannelDataRequest request = new ChannelDataRequest(channelData1);
                        TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", tunnelInfo.getClientID()), request.toBuf());
                    });

                    // 发送数据到外网客户
                    consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_SERVER, "send", channelID), event -> socket.write(event.body())));

                    socket.closeHandler(v -> {
                        CHANNEL_TUNNEL_INFO.remove(channelID);

                        consumers.forEach(MessageConsumer::unregister);

                        ChannelCloseRequest.ChannelData channelData1 = new ChannelCloseRequest.ChannelData();
                        channelData.setClientID(tunnelInfo.getClientID());
                        channelData.setTunnelID(channelID);
                        // 通知客户端隧道已关闭
                        TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", tunnelInfo.getClientID()), new ChannelCloseRequest(channelData1).toBuf());
                    });
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
}