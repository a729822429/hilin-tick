package icu.hilin.tick.client.handler;

import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.response.AuthErrorResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthErrorResponseHandler extends BaseCmdHandler<AuthErrorResponse> {
    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return BaseEntity.TYPE_RESPONSE_AUTH_ERROR == body.getByte(0);
    }

    @Override
    public AuthErrorResponse buffer2Entity(Long clientID, Buffer body) {
        return new AuthErrorResponse();
    }

    @Override
    public void doDeal(Long clientID, AuthErrorResponse entity) {
        log.info("\nAuth Error!!! \nProgram Exit!!");
        System.exit(0);
    }
}
