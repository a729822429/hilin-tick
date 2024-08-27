package icu.hilin.tick.server.cmd.handler;

import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.ChannelDataRequest;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import org.springframework.stereotype.Component;

@Component
public class ChannelDataRequestHandler extends BaseCmdHandler<ChannelDataRequest> {
    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return body.getByte(0) == BaseEntity.TYPE_REQUEST_CHANNEL_TRANSPORT_DATA;
    }

    @Override
    public ChannelDataRequest buffer2Entity(Long clientID, Buffer body) {
        return new ChannelDataRequest(body);
    }

    @Override
    public void doDeal(Long clientID, ChannelDataRequest entity) {
        ChannelDataRequest.ChannelData channelData = entity.toDataEntity();
        TickConstant.EVENT_BUS.publish(String.format(TickConstant.CHANNEL_SERVER, "send", channelData.getChannelID()),
                Buffer.buffer().appendLong(channelData.getSeq()).appendBuffer(channelData.getRequestData()));
    }
}
