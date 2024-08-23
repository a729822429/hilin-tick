package icu.hilin.tick.client;

import icu.hilin.tick.core.TickConstant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.entity.request.AuthRequest;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.netty.buffer.ByteBufUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class CmdClient implements ApplicationRunner {
    private static final Set<BaseCmdHandler<?>> HANDLERS = new HashSet<>();

    @Autowired
    public void init(ApplicationContext context) {
        context.getBeansOfType(BaseCmdHandler.class).forEach((name, handler) -> HANDLERS.add(handler));
    }

    @Autowired
    private ClientConfig clientConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<MessageConsumer<Buffer>> consumers = new ArrayList<>();
        TickConstant.VERTX.createWebSocketClient()
                .connect(clientConfig.getServerPort(), clientConfig.getServerHost(), "/", r -> {
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

                        consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CMD_CLIENT_ALL, "send"), event ->
                                socket.write(event.body())));

                        AuthRequest.ClientInfo clientInfo = new AuthRequest.ClientInfo();

                        clientInfo.setClientId(clientConfig.getClientId());
                        clientInfo.setClientPassword(clientConfig.getClientPassword());
                        AuthRequest authRequest = new AuthRequest(clientInfo);

                        System.out.println(ByteBufUtil.prettyHexDump(authRequest.getDataBuf().getByteBuf()));

                        socket.write(authRequest.toBuf());

                        socket.closeHandler(v -> {
                            System.out.println("closed");
                            consumers.forEach(MessageConsumer::unregister);
                        });
                    } else {
                        log.error("客户端启动失败", r.cause());
                        consumers.forEach(MessageConsumer::unregister);
                    }
                });
    }
}
