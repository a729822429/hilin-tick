package icu.hilin.tick.server.cmd.handler;

import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.response.ChannelDataResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import org.springframework.stereotype.Component;

@Component
public class ChannelDataResponseHandler extends BaseCmdHandler<ChannelDataResponse> {
    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return body.getByte(0) == BaseEntity.TYPE_RESPONSE_CHANNEL_TRANSPORT_DATA;
    }

    @Override
    public ChannelDataResponse buffer2Entity(Long clientID, Buffer body) {
        return new ChannelDataResponse(body);
    }

    @Override
    public void doDeal(Long clientID, ChannelDataResponse entity) {
        ChannelDataResponse.ChannelData channelData = entity.toDataEntity();
        TickConstant.EVENT_BUS.publish(String.format(TickConstant.CHANNEL_SERVER, "send", channelData.getChannelID()), channelData.getResponseData());
    }
}
