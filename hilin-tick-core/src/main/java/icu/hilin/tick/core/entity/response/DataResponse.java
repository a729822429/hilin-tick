package icu.hilin.tick.core.entity.response;

import io.vertx.core.buffer.Buffer;
import lombok.Data;

public class DataResponse {

    @Data
    public static class ChannelData {
        private String channelID;
        private Buffer requestData;
    }
}
