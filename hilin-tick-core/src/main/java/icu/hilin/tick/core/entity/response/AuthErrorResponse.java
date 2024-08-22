package icu.hilin.tick.core.entity.response;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;

public class AuthErrorResponse extends BaseEntity<Void> {


    public AuthErrorResponse() {
        super(BaseEntity.TYPE_RESPONSE_AUTH_ERROR, Buffer.buffer());
    }

    @Override
    public Void toDataEntity() {
        return null;
    }

    @Override
    public Buffer toDataBuffer(Void data) {
        return Buffer.buffer();
    }
}
