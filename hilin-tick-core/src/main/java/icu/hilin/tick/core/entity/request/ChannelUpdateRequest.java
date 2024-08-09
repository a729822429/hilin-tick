package icu.hilin.tick.core.entity.request;

import io.vertx.core.buffer.Buffer;
import lombok.Data;

public class ChannelUpdateRequest {

    @Data
    public static class ChannelData {
        private String channelID;
        private Buffer requestData;
    }
}
