package icu.hilin.tick.client;

import icu.hilin.tick.core.TickContant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.AuthRequest;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.http.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class TickClientApplication implements ApplicationRunner {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TickClientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    private static final Set<BaseCmdHandler> HANDLERS = new HashSet<>();

    @Autowired
    public void init(ApplicationContext context) {
        context.getBeansOfType(BaseCmdHandler.class).forEach((name, handler) -> HANDLERS.add(handler));
    }

    @Autowired
    private ClientConfig clientConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TickContant.VERTX.createWebSocketClient().connect(8081, "localhost", "/", r -> {
            if (r.succeeded()) {
                log.info("客户端启动成功");
                WebSocket socket = r.result();
                socket.handler(buf -> {
                    for (BaseCmdHandler handler : HANDLERS) {
                        if (handler.needDeal(clientConfig.getClientId(), buf)) {
                            BaseEntity entity = handler.buffer2Entity(clientConfig.getClientId(), buf);
                            handler.doDeal(clientConfig.getClientId(), entity);
                        }
                    }
                });
                AuthRequest.ClientInfo clientInfo = new AuthRequest.ClientInfo();

                clientInfo.setClientId("root");
                clientInfo.setClientPassword("jsfj1209");
                AuthRequest authRequest = new AuthRequest(BaseEntity.TYPE_REQUEST_AUTH, clientInfo);
                socket.write(authRequest.toBuf());

                socket.closeHandler(v -> {
                    System.out.println("closed");

                });

                // todo 启动穿透端口监听
                // todo 现在模拟，监听12345端口

            } else {
                log.error("客户端启动失败", r.cause());
            }
        });
    }

}
