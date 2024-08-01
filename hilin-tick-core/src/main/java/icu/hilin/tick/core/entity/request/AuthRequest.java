package icu.hilin.tick.core.entity.request;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

import java.nio.charset.StandardCharsets;

public class AuthRequest extends BaseEntity<AuthRequest.ClientInfo> {
    public AuthRequest(byte type, Buffer dataBuf) {
        super(type, dataBuf);
    }

    public AuthRequest(byte type, ClientInfo data) {
        super(type, data);
    }

    public AuthRequest(Buffer allBuf) {
        super(allBuf);
    }

    @Override
    public ClientInfo toDataEntity() {
        ClientInfo clientInfo = new ClientInfo();
        int clientIdLength = getDataBuf().getInt(0);
        String clientId = getDataBuf().getString(4, 4 + clientIdLength);
        int clientPasswordLength = getDataBuf().getInt(4 + clientIdLength);
        String clientPassword = getDataBuf().getString(4 + clientIdLength + 4, 4 + clientIdLength + 4 + clientPasswordLength);

        clientInfo.setClientId(clientId);
        clientInfo.setClientPassword(clientPassword);

        return clientInfo;
    }

    @Override
    public Buffer toDataBuffer(ClientInfo client) {
        return Buffer.buffer().appendInt(client.getClientId().length()).appendBytes(client.getClientId().getBytes(StandardCharsets.UTF_8))
                .appendInt(client.getClientPassword().length()).appendBytes(client.getClientPassword().getBytes(StandardCharsets.UTF_8));
    }

    @Data
    public static class ClientInfo {
        private String clientId;
        private String clientPassword;
    }
}
