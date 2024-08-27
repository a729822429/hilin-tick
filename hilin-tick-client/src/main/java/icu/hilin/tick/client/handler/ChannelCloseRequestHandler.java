package icu.hilin.tick.client.handler;

import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.ChannelCloseRequest;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import org.springframework.stereotype.Component;

@Component
public class ChannelCloseRequestHandler extends BaseCmdHandler<ChannelCloseRequest> {
    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return body.getByte(0) == BaseEntity.TYPE_REQUEST_CHANNEL_CLOSE_DATA;
    }

    @Override
    public ChannelCloseRequest buffer2Entity(Long clientID, Buffer body) {
        return new ChannelCloseRequest(body);
    }

    @Override
    public void doDeal(Long clientID, ChannelCloseRequest entity) {
        ChannelCloseRequest.ChannelData channelData = entity.toDataEntity();
        TickConstant.EVENT_BUS.publish(String.format(TickConstant.CHANNEL_CLIENT, "close", channelData.getChannelID()), Buffer.buffer());
    }
}
