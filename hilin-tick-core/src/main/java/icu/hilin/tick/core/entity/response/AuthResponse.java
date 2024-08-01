package icu.hilin.tick.core.entity.response;

import cn.hutool.json.JSONUtil;
import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AuthResponse extends BaseEntity<List<AuthResponse.ChannelInfo>> {


    public AuthResponse(byte type, List<AuthResponse.ChannelInfo> data) {
        super(type, data);
    }

    public AuthResponse(byte type, Buffer dataBuf) {
        super(type, dataBuf);
    }

    public AuthResponse(Buffer allBuf) {
        super(allBuf);
    }

    @Override
    public List<AuthResponse.ChannelInfo> toDataEntity() {
        return JSONUtil.toList(getDataBuf().toString(StandardCharsets.UTF_8), AuthResponse.ChannelInfo.class);
    }

    @Override
    public Buffer toDataBuffer(List<AuthResponse.ChannelInfo> data) {
        return Buffer.buffer(JSONUtil.toJsonStr(data));
    }

    @Data
    public static class ChannelInfo {

        /**
         * 1 tcp
         * 2 udp
         */
        private int type;

        /**
         * 转发到的目的ip或域名
         */
        private String targetHost;

        /**
         * 转发的目的端口
         */
        private int targetPort;

        /**
         * 远程（服务器接收）端口
         */
        private int remotePort;

        /**
         * 远程访问绑定的域名
         * 这个仅仅针对http/https/ws/wss协议
         */
        private String bindDomain;

        /**
         * 穿透通道id
         */
        private String channelId;

        /**
         * 信令通道id
         */
        private String clientID;

        /**
         * 最后更改时间
         * 用户客户端判断是否需要更新
         */
        private Long updateTime;
    }

    public static void main(String[] args) {
        new ScheduledThreadPoolExecutor(1)
                .scheduleAtFixedRate(()->{
                    File file = new File("d:\\111.txt");
                    System.out.println(file.lastModified());
                },1,1, TimeUnit.SECONDS);
    }
}
