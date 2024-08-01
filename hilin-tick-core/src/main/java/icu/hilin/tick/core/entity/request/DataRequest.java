package icu.hilin.tick.core.entity.request;


import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

public class DataRequest extends BaseEntity<Buffer> {
    public DataRequest(Buffer allBuf) {
        super(allBuf);
    }

    @Override
    public Buffer toDataEntity() {
        return getDataBuf();
    }

    @Override
    public Buffer toDataBuffer(Buffer data) {
        return data;
    }

    @Data
    public static class DataInfo {
        private String channelID;
    }
}
