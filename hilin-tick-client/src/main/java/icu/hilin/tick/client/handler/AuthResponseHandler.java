package icu.hilin.tick.client.handler;

import java.util.HashMap;
import java.util.Map;

import cn.hutool.json.JSONUtil;
import icu.hilin.tick.core.TickContant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthResponseHandler extends BaseCmdHandler<AuthResponse> {

    public static final Map<String, AuthResponse.ChannelInfo> CHANNELS = new HashMap<>();

    @Override
    public boolean needDeal(String clientID, Buffer body) {
        return BaseEntity.TYPE_RESPONSE_AUTH == body.getByte(0);
    }

    @Override
    public AuthResponse buffer2Entity(String clientID, Buffer body) {
        return new AuthResponse(body);
    }

    @Override
    public void doDeal(String clientID, AuthResponse entity) {
        // 清空所有通道缓存
        CHANNELS.clear();
        for (AuthResponse.ChannelInfo channelInfo : entity.toDataEntity()) {
            CHANNELS.put(channelInfo.getChannelId(), channelInfo);
            log.info("收到通道信息 : {}", JSONUtil.toJsonStr(channelInfo));
        }
    }
}
