package icu.hilin.tick.core.entity.request;

import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.response.AuthResponse;
import io.vertx.core.buffer.Buffer;

public class TunnelUpdateRequest extends BaseEntity<AuthResponse.TunnelInfo> {

    public TunnelUpdateRequest(AuthResponse.TunnelInfo data) {
        super(BaseEntity.TYPE_RESPONSE_TUNNEL_UPDATE, data);
    }

    public TunnelUpdateRequest(Buffer dataBuf) {
        super(BaseEntity.TYPE_RESPONSE_TUNNEL_UPDATE, dataBuf);
    }

    @Override
    public AuthResponse.TunnelInfo toDataEntity() {
        return null;
    }

    @Override
    public Buffer toDataBuffer(AuthResponse.TunnelInfo data) {
        return null;
    }
}
