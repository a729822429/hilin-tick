package icu.hilin.tick.core.entity.request;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 隧道关闭
 */
public class ChannelCloseRequest extends BaseEntity<ChannelCloseRequest.ChannelData> {

    public ChannelCloseRequest(ChannelData dataBuf) {
        super(BaseEntity.TYPE_REQUEST_CHANNEL_CONNECTOR_DATA, dataBuf);
    }

    public ChannelCloseRequest(Buffer allBuf) {
        super(allBuf);
    }

    @Override
    public Buffer toDataBuffer(ChannelCloseRequest.ChannelData data) {

        return Buffer.buffer()
                .appendLong(data.getClientID())
                .appendLong(data.getTunnelID())
                .appendLong(data.getChannelID());
    }

    @Override
    public ChannelCloseRequest.ChannelData toDataEntity() {
        long clientID = getDataBuf().getLong(0);
        long tunnelID = getDataBuf().getLong(8);
        long channelID = getDataBuf().getLong(16);
        ChannelCloseRequest.ChannelData channelData = new ChannelData();
        channelData.setClientID(clientID);
        channelData.setTunnelID(tunnelID);
        channelData.setChannelID(channelID);
        return channelData;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelData {

        /**
         * 客户端id
         */
        private long clientID;
        /**
         * 隧道id
         */
        private long tunnelID;
        /**
         * 通道id
         */
        private long channelID;
    }
}
