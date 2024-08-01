package icu.hilin.tick.server.cmd.handler;

import cn.hutool.core.util.ObjectUtil;
import icu.hilin.core.Contant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.AuthRequest;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AuthRequestHandler extends BaseCmdHandler<AuthRequest> {
    @Override
    public boolean needDeal(String clientID, Buffer body) {
        return body.getByte(0) == BaseEntity.TYPE_REQUEST_AUTH;
    }

    @Override
    public AuthRequest buffer2Entity(String clientID, Buffer body) {
        return new AuthRequest(body);
    }

    @Override
    public void doDeal(String clientID, AuthRequest entity) {
        AuthRequest.ClientInfo clientInfo = entity.toDataEntity();
        if (ObjectUtil.isNotEmpty(clientInfo.getClientId()) && ObjectUtil.isNotEmpty(clientInfo.getClientPassword())) {
            log.info("CMD Server认证成功 {}:{}", clientInfo.getClientId(), clientInfo.getClientPassword());

            // 通知cmd server修改clientID
            Contant.EVENT_BUS.request("auth-" + clientID, Buffer.buffer(clientInfo.getClientId()), r -> {
                // 查询通知客户端登录成功
                // todo 查询隧道列表
                List<AuthResponse.ChannelInfo> channels = new ArrayList<>();
                log.info("auth publish {}", clientInfo.getClientId());

                Contant.EVENT_BUS.publish("send-" + clientInfo.getClientId(), new AuthResponse(BaseEntity.TYPE_RESPONSE_AUTH, channels).toBuf());
            });
        } else {
            log.warn("CMD Server认证失败 {}:{}", clientInfo.getClientId(), clientInfo.getClientPassword());
            // 通知服务关闭连接
            Contant.EVENT_BUS.publish("close-" + clientID, Buffer.buffer());
        }
    }

}
