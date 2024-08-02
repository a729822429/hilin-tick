package icu.hilin.tick.core.entity.request;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DataRequest extends BaseEntity<DataRequest.ChannelData> {

    public DataRequest(ChannelData dataBuf) {
        super(BaseEntity.TYPE_REQUEST_CHANNEL_DATA, dataBuf);
    }

    public DataRequest(Buffer allBuf) {
        super(allBuf);
    }

    @Override
    public Buffer toDataBuffer(DataRequest.ChannelData data) {
        Buffer buffer = Buffer.buffer(data.getType());

        buffer
                .appendLong(data.getConnectorID())
                // 由于channelID是字符串，因此要先写入channelID长度，后续才能解析
                .appendShort((short) data.getChannelID().length())
                // 写入channelID
                .appendString(data.getChannelID())

                // 写入responseData长度，后续才能解析
                .appendInt(data.getRequestData().length())
                // 写入responseData
                .appendBuffer(data.getRequestData());

        return buffer;
    }

    @Override
    public DataRequest.ChannelData toDataEntity() {
        long connectorID = getDataBuf().getLong(0);
        byte type = getDataBuf().getByte(8);
        short channelIDLength = getDataBuf().getShort(8 + 1);
        String channelID = getDataBuf().getString(8 + 3, 8 + 3 + channelIDLength);

        int dataLength = getDataBuf().getInt(8 + 3 + channelIDLength);
        Buffer data = dataLength > 0
                ? getDataBuf().getBuffer(8 + 3 + channelIDLength + 4, 8 + 3 + channelIDLength + 4 + dataLength)
                : Buffer.buffer();

        DataRequest.ChannelData channelData = new ChannelData();
        channelData.setConnectorID(connectorID);
        channelData.setType(type);
        channelData.setChannelID(channelID);
        channelData.setRequestData(data);
        return channelData;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelData {

        public static byte TYPE_CONNECT = 1;
        public static byte TYPE_DATA = 2;
        public static byte TYPE_CLOSE = 3;

        /**
         * 请求id
         */
        private long connectorID;

        /**
         * 1. 有连接进入
         * 2. 收到数据
         * 3. 连接关闭
         */
        private byte type;
        /**
         * 
         */
        private String channelID;
        private Buffer requestData;
    }
}
