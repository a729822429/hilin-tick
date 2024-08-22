package icu.hilin.tick.client.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import icu.hilin.tick.core.BufferUtils;
import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.ChannelCloseRequest;
import icu.hilin.tick.core.entity.request.ChannelConnectedRequest;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
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
        BufferUtils.printBuffer("收到隧道连接信息", body);
        return new ChannelConnectedRequest(body);
    }

    @Override
    public void doDeal(Long clientID, ChannelConnectedRequest entity) {
        log.info("开始启动通道");
        ChannelConnectedRequest.ChannelData channelData = entity.toDataEntity();
        AuthResponse.TunnelInfo tunnel = AuthResponseHandler.getTUNNEL(channelData.getTunnelID());

        List<MessageConsumer<?>> consumers = new ArrayList<>();

        // 存放通道启动结果 1成功 2失败 0正在启动
        LongAdder channelResult = new LongAdder();

        // 创建一个队列存放将要发送的数据
        List<Buffer> queue = new ArrayList<>();

        consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.TUNNEL_CLIENT, "receive", channelData.getTunnelID()), (Handler<Message<Buffer>>) event -> {
            // 如果通道启动成功，将发送数据
            if (channelResult.intValue() == 1) {
                // 先判断消息队列中是否有数据
                if (ObjectUtil.isNotEmpty(queue)) {
                    // 优先发送队列数据，因为队列数据先到
                    queue.forEach(buf -> {
                        TickConstant.EVENT_BUS.publish(String.format(TickConstant.TUNNEL_CLIENT, "send", channelData.getTunnelID()), buf);
                    });
                    queue.clear();
                }
                // 消息队列中的数据发送完毕，再发送当前数据
                TickConstant.EVENT_BUS.publish(String.format(TickConstant.TUNNEL_CLIENT, "send", channelData.getTunnelID()), event.body());
            }
            // 如果通道正在启动，消息存放到队列中
            else if (channelResult.intValue() == 0) {
                queue.add(event.body());
            }
            // 如果通道启动失败，则抛弃（这个事件监听会被删除，也就实现了抛弃）
            else {

            }
        }));


        // 连接内网服务器
        TickConstant.VERTX.createNetClient().connect(tunnel.getTargetPort(), tunnel.getTargetHost(), r -> {
            if (r.succeeded()) {
                log.info("通道连接成功");
                // 连接成功
                final NetSocket socket = r.result();

                socket.handler(buf -> {
                    // 收到目的服务器的数据

                });

                // 关闭通道
                consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "close", entity.toDataEntity().getChannelID()), event -> {
                    socket.close();
                }));

                // 监听需要发送的数据
                consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CHANNEL_CLIENT, "send", entity.toDataEntity().getChannelID()), (Handler<Message<Buffer>>) event -> {
                    socket.write(event.body());
                }));
                socket.closeHandler(v -> {
                    consumers.forEach(MessageConsumer::unregister);
                    ChannelCloseRequest.ChannelData closeChannel = new ChannelCloseRequest.ChannelData();
                    closeChannel.setClientID(clientID);
                    closeChannel.setTunnelID(tunnel.getTunnelId());
                    closeChannel.setChannelID(channelData.getChannelID());
                    ChannelCloseRequest request = new ChannelCloseRequest(closeChannel);
                    TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_CLIENT, "send", clientID), request.toBuf());
                });
                channelResult.add(1);
            } else {
                log.info("通道连接失败 {}", JSONUtil.toJsonStr(tunnel), r.cause());
                consumers.forEach(MessageConsumer::unregister);
                channelResult.add(2);
            }
        });

    }
}
