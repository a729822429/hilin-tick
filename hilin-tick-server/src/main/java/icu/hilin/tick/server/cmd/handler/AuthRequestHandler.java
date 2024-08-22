package icu.hilin.tick.server.cmd.handler;

import cn.hutool.core.util.ObjectUtil;
import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.AuthRequest;
import icu.hilin.tick.core.entity.response.AuthErrorResponse;
import icu.hilin.tick.core.entity.response.AuthResponse;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import icu.hilin.tick.server.tunnel.TunnelServer;
import io.vertx.core.buffer.Buffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthRequestHandler extends BaseCmdHandler<AuthRequest> {

    @Autowired
    private TunnelServer tunnelServer;

    @Override
    public boolean needDeal(Long clientID, Buffer body) {
        return body.getByte(0) == BaseEntity.TYPE_REQUEST_AUTH;
    }

    @Override
    public AuthRequest buffer2Entity(Long clientID, Buffer body) {
        return new AuthRequest(body);
    }

    @Override
    public void doDeal(Long clientID, AuthRequest entity) {
        // 1. 认证
        // todo 暂时只判断不为空
        AuthRequest.ClientInfo clientInfo = entity.toDataEntity();
        if (ObjectUtil.isNotEmpty(clientInfo.getClientId()) && ObjectUtil.isNotEmpty(clientInfo.getClientPassword())) {
            // 2. 认证成功，获取客户端隧道列表
            // todo
            List<AuthResponse.TunnelInfo> list = new ArrayList<>();
            list.add(new AuthResponse.TunnelInfo());
            list.get(0).setType(1);
            list.get(0).setTunnelId(1L);
            list.get(0).setClientID(clientID);
            list.get(0).setRemotePort(9999);
            list.get(0).setTargetHost("www.baidu.com");
            list.get(0).setTargetPort(80);

            tunnelServer.startTcpTunnelServer(list.get(0));

            // 3. 启动隧道，并把隧道发送给客户端
            AuthResponse response = new AuthResponse(list);
            TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", clientID), response.toBuf());
        } else {
            // 2. 认证失败
            AuthErrorResponse response = new AuthErrorResponse();
            TickConstant.EVENT_BUS.publish(String.format(TickConstant.CMD_SERVER, "send", clientID), response.toBuf());
        }
    }
}
