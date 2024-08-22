package icu.hilin.tick.client.handler;

import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.ChannelDataRequest;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import org.springframework.stereotype.Component;

@Component
public class ChannelDataHandler extends BaseCmdHandler<ChannelDataRequest> {
    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return BaseEntity.TYPE_REQUEST_CHANNEL_TRANSPORT_DATA == body.getByte(0);
    }

    @Override
    public ChannelDataRequest buffer2Entity(Long clientID, Buffer body) {
        return new ChannelDataRequest(body);
    }

    @Override
    public void doDeal(Long clientID, ChannelDataRequest entity) {

    }
}
