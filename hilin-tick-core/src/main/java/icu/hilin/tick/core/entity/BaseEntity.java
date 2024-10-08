package icu.hilin.tick.core.entity;

import io.vertx.core.buffer.Buffer;
import lombok.Data;

@Data
public abstract class BaseEntity<T> {

    public static final byte TYPE_REQUEST_AUTH = 1;
    public static final byte TYPE_REQUEST_CHANNEL_CONNECTOR_DATA = 2;
    public static final byte TYPE_REQUEST_CHANNEL_TRANSPORT_DATA = 3;
    public static final byte TYPE_REQUEST_CHANNEL_CLOSE_DATA = 4;

    /**
     * 心跳
     */
    public static final byte TYPE_HEARTBEAT = 0;

    public static final byte TYPE_RESPONSE_AUTH_SUCCESS = -1;
    public static final byte TYPE_RESPONSE_AUTH_ERROR = -2;

    public static final byte TYPE_RESPONSE_CHANNEL_TRANSPORT_DATA = -5;

    /**
     * 隧道更新
     */
    public static final byte TYPE_RESPONSE_TUNNEL_UPDATE = -4;


    /**
     * 类型
     * 1-127 请求
     * 0 心跳
     * (-127)-(-1) 响应
     */
    private byte type;

    /**
     * 数据长度
     */
    private int dataLength;

    /**
     * 消息体
     */
    private Buffer dataBuf;

    /**
     * 数据实体转换为实体
     */
    public BaseEntity(byte type, T data) {
        this.type = type;
        if (data instanceof Buffer) {
            this.dataBuf = (Buffer) data;
        } else {
            this.dataBuf = toDataBuffer(data);
        }
        this.dataLength = this.dataBuf.length();
    }

    /**
     * 二进制数据转换为实体
     */
    public BaseEntity(byte type, Buffer dataBuf) {
        this.type = type;
        this.dataBuf = dataBuf;
        this.dataLength = this.dataBuf.length();
    }

    /**
     * 全部二进制数据转换为实体
     */
    public BaseEntity(Buffer allBuf) {
        this.type = allBuf.getByte(0);
        this.dataLength = allBuf.getInt(1);
        this.dataBuf = allBuf.getBuffer(5, allBuf.length());
    }

    /**
     * 收到消息体转换为实体
     */
    abstract public T toDataEntity();

    abstract public Buffer toDataBuffer(T data);

    /**
     * 转换为数据通道实体
     */
    public Buffer toBuf() {
        return Buffer.buffer().appendByte(type).appendInt(dataLength).appendBuffer(dataBuf);
    }

}
