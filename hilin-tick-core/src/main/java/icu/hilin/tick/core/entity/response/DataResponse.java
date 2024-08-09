package icu.hilin.tick.core.entity.response;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

public class DataResponse extends BaseEntity<DataResponse.ChannelData> {

    public DataResponse(Buffer dataBuf) {
        super(BaseEntity.TYPE_RESPONSE_CHANNEL_DATA, dataBuf);
    }

    public DataResponse(DataResponse.ChannelData data) {
        super(BaseEntity.TYPE_RESPONSE_CHANNEL_DATA, data);
    }

    @Override
    public DataResponse.ChannelData toDataEntity() {
        return null;
    }

    @Override
    public Buffer toDataBuffer(DataResponse.ChannelData data) {
        return null;
    }

    @Data
    public static class ChannelData {
        private long connectorId;
        private Buffer requestData;
    }

}
