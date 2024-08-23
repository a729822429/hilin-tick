package icu.hilin.tick.core.entity.response;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

/**
 * 客户端发送到服务端的数据
 */
public class ChannelDataResponse extends BaseEntity<ChannelDataResponse.ChannelData> {

    public ChannelDataResponse(byte b, Buffer dataBuf) {
        super(BaseEntity.TYPE_RESPONSE_CHANNEL_TRANSPORT_DATA, dataBuf);
    }

    public ChannelDataResponse(Buffer allBuf) {
        super(allBuf);
    }

    public ChannelDataResponse(ChannelDataResponse.ChannelData data) {
        super(BaseEntity.TYPE_RESPONSE_CHANNEL_TRANSPORT_DATA, data);
    }

    @Override
    public ChannelDataResponse.ChannelData toDataEntity() {
        ChannelDataResponse.ChannelData channelData = new ChannelData();
        channelData.setChannelID(getDataBuf().getLong(0));
        int responseDataLength = getDataBuf().getInt(8);
        channelData.setResponseData(getDataBuf().getBuffer(12, 12 + responseDataLength));
        return channelData;
    }

    @Override
    public Buffer toDataBuffer(ChannelDataResponse.ChannelData data) {
        return Buffer.buffer()
                .appendLong(data.getChannelID())
                .appendInt(data.getResponseData().length())
                .appendBuffer(data.getResponseData());
    }

    @Data
    public static class ChannelData {
        private long channelID;
        private Buffer responseData;
    }

}
