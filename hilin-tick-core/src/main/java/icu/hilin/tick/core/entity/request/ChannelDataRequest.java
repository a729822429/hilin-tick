package icu.hilin.tick.core.entity.request;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据传输
 */
public class ChannelDataRequest extends BaseEntity<ChannelDataRequest.ChannelData> {

    public ChannelDataRequest(ChannelData dataBuf) {
        super(BaseEntity.TYPE_REQUEST_CHANNEL_TRANSPORT_DATA, dataBuf);
    }

    public ChannelDataRequest(Buffer allBuf) {
        super(allBuf);
    }

    @Override
    public Buffer toDataBuffer(ChannelDataRequest.ChannelData data) {

        return Buffer.buffer()
                .appendLong(data.getChannelID())
                // 写入responseData
                .appendBuffer(data.getRequestData());
    }

    @Override
    public ChannelDataRequest.ChannelData toDataEntity() {
        long connectorID = getDataBuf().getLong(0);

        ChannelDataRequest.ChannelData channelData = new ChannelData();
        channelData.setChannelID(connectorID);
        if (getDataLength() > 8) {
            channelData.setRequestData(getDataBuf().getBuffer(8, getDataBuf().length()));
        } else {
            channelData.setRequestData(Buffer.buffer());
        }
        return channelData;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelData {

        /**
         * 请求连接id
         */
        private long channelID;

        private Buffer requestData;
    }
}
