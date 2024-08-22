package icu.hilin.tick.core.entity.request;

import icu.hilin.tick.core.entity.BaseEntity;
import io.netty.buffer.ByteBufUtil;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

public class AuthRequest extends BaseEntity<AuthRequest.ClientInfo> {
    public AuthRequest(byte type, Buffer dataBuf) {
        super(BaseEntity.TYPE_REQUEST_AUTH, dataBuf);
    }

    public AuthRequest(ClientInfo data) {
        super(BaseEntity.TYPE_REQUEST_AUTH, data);
    }

    public AuthRequest(Buffer allBuf) {
        super(allBuf);
    }


    @Override
    public ClientInfo toDataEntity() {
        ClientInfo clientInfo = new ClientInfo();
        System.out.println(ByteBufUtil.prettyHexDump(getDataBuf().getByteBuf()));
        Long clientId = getDataBuf().getLong(0);
        int clientPasswordLength = getDataBuf().getInt(8);
        String clientPassword = getDataBuf().getString(12, 12 + clientPasswordLength);

        clientInfo.setClientId(clientId);
        clientInfo.setClientPassword(clientPassword);

        return clientInfo;
    }

    @Override
    public Buffer toDataBuffer(ClientInfo client) {
        return Buffer.buffer()
                .appendLong(client.getClientId())
                .appendInt(client.getClientPassword().getBytes().length)
                .appendBytes(client.getClientPassword().getBytes());
    }

    @Data
    public static class ClientInfo {
        private Long clientId;
        private String clientPassword;
    }
}
