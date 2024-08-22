package icu.hilin.tick.client.handler;

import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthResponseHandler extends BaseCmdHandler<AuthResponse> {

    private static final Map<Long, AuthResponse.TunnelInfo> TUNNELS = new ConcurrentHashMap<>();

    public static AuthResponse.TunnelInfo getTUNNEL(long tunnelID) {
        return TUNNELS.get(tunnelID);
    }

    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return BaseEntity.TYPE_RESPONSE_AUTH_SUCCESS == body.getByte(0);
    }

    @Override
    public AuthResponse buffer2Entity(Long clientID, Buffer body) {
        return new AuthResponse(body);
    }

    @Override
    public void doDeal(Long clientID, AuthResponse response) {
        List<AuthResponse.TunnelInfo> tunnels = response.toDataEntity();
        TUNNELS.clear();
        for (AuthResponse.TunnelInfo tunnel : tunnels) {
            TUNNELS.put(tunnel.getTunnelId(), tunnel);
        }

    }
}
