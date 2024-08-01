package icu.hilin.tick.client.handler;

import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;

public class AuthResponseHandler extends BaseCmdHandler<AuthResponse> {
    @Override
    public boolean needDeal(String clientID, Buffer body) {
        return BaseEntity.TYPE_RESPONSE_AUTH == body.getByte(0);
    }

    @Override
    public AuthResponse buffer2Entity(String clientID, Buffer body) {
        return new AuthResponse(body);
    }

    @Override
    public void doDeal(String clientID, AuthResponse entity) {
        for (AuthResponse.ChannelInfo channelInfo : entity.toDataEntity()) {
        }
    }
}
