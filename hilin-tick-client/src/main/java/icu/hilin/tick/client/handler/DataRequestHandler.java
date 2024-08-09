package icu.hilin.tick.client.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import icu.hilin.tick.core.TickContant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.DataRequest;
import icu.hilin.tick.core.entity.request.DataRequest.ChannelData;
import icu.hilin.tick.core.entity.response.AuthResponse.ChannelInfo;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;

public class DataRequestHandler extends BaseCmdHandler<DataRequest> {

    private static final Map<Long, ReentrantLock> LOCKS = new HashMap<>();

    @Override
    public boolean needDeal(String clientID, Buffer body) {
        return body.getByte(0) == BaseEntity.TYPE_RESPONSE_CHANNEL_DATA;
    }

    @Override
    public DataRequest buffer2Entity(String clientID, Buffer body) {
        return new DataRequest(body);
    }

    @Override
    public void doDeal(String clientID, DataRequest entity) {
        final ChannelData channelData = entity.toDataEntity();
        if (channelData.getType() == ChannelData.TYPE_CONNECT) {
            // 连接开始
            if (LOCKS.containsKey(channelData.getConnectorID())) {
                // 已经连接了，通知断开

                return;
            }
            final ChannelInfo channelInfo = AuthResponseHandler.CHANNELS.get(channelData.getChannelID());
            TickContant.VERTX.createNetClient().connect(channelInfo.getTargetPort(), channelInfo.getTargetHost(), r -> {

                if (r.succeeded()) {
                    List<MessageConsumer<?>> consumers = new ArrayList<>();
                    // 连接成功
                    // 心跳，用户其它事件判断该连接是否正常
                    consumers.add(TickContant.EVENT_BUS.consumer("channel-inside-pp-" + channelData.getConnectorID(),
                            event -> event.reply(Buffer.buffer())));

                    // 数据发送到内网服务器
                    consumers.add(TickContant.EVENT_BUS.consumer("channel-inside-pp-" + channelData.getConnectorID(),
                            event -> {
                                event.reply(Buffer.buffer());
                            }));

                    r.result().closeHandler(v -> {
                        consumers.forEach(MessageConsumer::unregister);
                        // todo 通知cmd关闭远端通道
                    });
                } else {
                    // todo 连接失败，通知到cmd，发送数据到外网连接
                }
            });
        } else if (channelData.getType() == ChannelData.TYPE_DATA) {
            // 数据传输
            // todo 先加入队列，需要连接成功之后再发送，否则会丢数据
            TickContant.EVENT_BUS.request("channel-inside-pp-" + channelData.getConnectorID(), Buffer.buffer(),
                    new DeliveryOptions().setSendTimeout(5 * 1000),
                    event -> {
                        if (event.succeeded()) {
                            // 收到响应，发送数据
                        }
                    });
        } else if (channelData.getType() == ChannelData.TYPE_CLOSE) {
            // 数据传输
            TickContant.EVENT_BUS.request("channel-inside-pp-" + channelData.getConnectorID(), Buffer.buffer(),
                    event -> {
                        // 收到响应，发送关闭请求
                    });
        }
    }

}
