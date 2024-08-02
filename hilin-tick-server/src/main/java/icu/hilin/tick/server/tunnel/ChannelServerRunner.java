package icu.hilin.tick.server.tunnel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import icu.hilin.tick.core.TickContant;
import icu.hilin.tick.core.entity.request.DataRequest;
import icu.hilin.tick.core.entity.request.DataRequest.ChannelData;
import icu.hilin.tick.core.entity.response.AuthResponse.ChannelInfo;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChannelServerRunner {

    public void start(final ChannelInfo channel) {
        switch (channel.getType()) {
            case 1:
                // tcp
                TickContant.VERTX.createNetServer()
                        .connectHandler(netSocket -> {

                            // 用户连接id
                            Long connectorID = IdUtil.getSnowflakeNextId();

                            List<MessageConsumer<?>> consumers = new ArrayList<>();

                            // 通道关闭事件监听
                            consumers.add(TickContant.EVENT_BUS
                                    .consumer("channel-close-" + connectorID, event -> netSocket.close()));

                            // channel消息发送给请求者，告知连接进入，需要内网channel连接到服务器
                            ChannelData connectChannelData = new ChannelData();
                            connectChannelData.setType(ChannelData.TYPE_CONNECT);
                            connectChannelData.setConnectorID(connectorID);
                            connectChannelData.setChannelID(channel.getChannelId());
                            TickContant.EVENT_BUS.publish("send-cmd-" + channel.getChannelId(),
                                    new DataRequest(connectChannelData));

                            // 发送数据给请求者
                            consumers.add(TickContant.EVENT_BUS
                                    .consumer("channel-response-" + connectorID,
                                            new Handler<Message<Buffer>>() {
                                                @Override
                                                public void handle(Message<Buffer> event) {
                                                    netSocket.write(event.body());
                                                }
                                            }));

                            netSocket.handler(buf -> {
                                // 收到
                                DataRequest.ChannelData channelData = new DataRequest.ChannelData();
                                channelData.setType(ChannelData.TYPE_DATA);
                                channelData.setChannelID(channel.getChannelId());
                                channelData.setRequestData(buf);
                                channelData.setConnectorID(connectorID);
                                DataRequest dataRequest = new DataRequest(channelData);
                                // 发送通道数据消息
                                TickContant.EVENT_BUS.publish("send-cmd-" + channel.getClientID(), dataRequest.toBuf());
                            }).closeHandler(v -> {
                                // 删除本通道所有事件监听
                                consumers.forEach(MessageConsumer::unregister);

                                // 如果是外网用户断开
                                DataRequest.ChannelData channelData = new DataRequest.ChannelData();
                                channelData.setType(ChannelData.TYPE_CLOSE);
                                channelData.setChannelID(channel.getChannelId());
                                channelData.setConnectorID(connectorID);

                                // 发送通道数据消息
                                TickContant.EVENT_BUS.publish("send-cmd-" + channel.getClientID(),
                                        new DataRequest(channelData).toBuf());
                            });

                        })
                        .listen(channel.getRemotePort(), "0.0.0.0", r -> {
                            if (r.succeeded()) {
                                log.info("channel 启动成功 {}", JSONUtil.toJsonStr(channel));
                            } else {
                                log.info("channel 启动失败 {}", JSONUtil.toJsonStr(channel));
                            }
                        });
                break;
            case 2:
                // udp
                break;
            default:
                break;
        }
    }

}
