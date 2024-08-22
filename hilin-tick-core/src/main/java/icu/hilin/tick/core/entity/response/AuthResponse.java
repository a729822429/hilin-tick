package icu.hilin.tick.core.entity.response;

import cn.hutool.json.JSONUtil;
import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthResponse extends BaseEntity<List<AuthResponse.TunnelInfo>> {


    public AuthResponse(List<AuthResponse.TunnelInfo> data) {
        super(TYPE_RESPONSE_AUTH_SUCCESS, data);
    }

    public AuthResponse(int b,Buffer dataBuf) {
        super(TYPE_RESPONSE_AUTH_SUCCESS, dataBuf);
    }

    public AuthResponse(Buffer buf) {
        super(buf);
    }

    @Override
    public List<AuthResponse.TunnelInfo> toDataEntity() {
        return JSONUtil.toList(getDataBuf().toString(StandardCharsets.UTF_8), AuthResponse.TunnelInfo.class);
    }

    @Override
    public Buffer toDataBuffer(List<AuthResponse.TunnelInfo> data) {
        return Buffer.buffer(JSONUtil.toJsonStr(Optional.ofNullable(data).orElse(new ArrayList<>())));
    }

    @Data
    public static class TunnelInfo {

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
         * 信令通道id
         */
        private Long clientID;
        /**
         * 穿透隧道id
         */
        private Long tunnelId;
        /**
         * 最后更改时间
         * 用户客户端判断是否需要更新
         */
        private Long updateTime;
    }
}
