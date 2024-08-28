package icu.hilin.tick.core.entity.response;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChannelConnectedSuccessResponse extends BaseEntity<ChannelConnectedSuccessResponse.ChannelData> {

    public ChannelConnectedSuccessResponse(byte type, ChannelData data) {
        super(BaseEntity.TYPE_RESPONSE_AUTH_ERROR, data);
    }

    public ChannelConnectedSuccessResponse(byte type, Buffer dataBuf) {
        super(type, dataBuf);
    }

    public ChannelConnectedSuccessResponse(Buffer allBuf) {
        super(allBuf);
    }

    @Override
    public ChannelData toDataEntity() {
        return null;
    }

    @Override
    public Buffer toDataBuffer(ChannelData data) {
        return null;
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
        /**
         * 序列，用以判断数据的顺序，从1开始，依次叠加
         */
        private long seq;
        private Buffer requestData;
    }

}
