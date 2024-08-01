package icu.hilin.tick.server.cmd;

import cn.hutool.core.util.IdUtil;
import icu.hilin.core.Contant;
import icu.hilin.tick.core.entity.BaseEntity;
import icu.hilin.tick.core.handler.BaseCmdHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class CmdServer implements ApplicationRunner {
    private static final Set<BaseCmdHandler> HANDLERS = new HashSet<>();
    @Value("${server.cmdPort:8081}")
    private int cmdPort;

    @Autowired
    public void init(ApplicationContext context) {
        context.getBeansOfType(BaseCmdHandler.class).forEach((name, handler) -> HANDLERS.add(handler));
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Contant.VERTX.createHttpServer()
                .webSocketHandler(wsSocket -> {
                    final StringBuilder clientID = new StringBuilder();

                    // 添加初始化客户端ID，认证成功后覆盖
                    clientID.append(IdUtil.getSnowflakeNextIdStr());

                    log.info("{}:{}", wsSocket.remoteAddress().host(), wsSocket.remoteAddress().port());

                    wsSocket.binaryMessageHandler(body -> handler(clientID.toString(), body));

                    List<MessageConsumer<Buffer>> consumers = new ArrayList<>();

                    // 收到auth消息
                    consumers.add(
                            Contant.EVENT_BUS.consumer("auth-" + clientID, authEvent -> {

                                consumers.forEach(MessageConsumer::unregister);
                                consumers.clear();

                                // 重写clientID
                                clientID.delete(0, clientID.length());
                                clientID.append(authEvent.body().toString(StandardCharsets.UTF_8));

                                log.info("send consumer {}", clientID);
                                // 添加发送消息监听
                                consumers.add(Contant.EVENT_BUS.consumer("send-" + clientID, event -> {
                                    wsSocket.write(event.body());
                                }));

                                // 添加主动断开监听
                                consumers.add(Contant.EVENT_BUS.consumer("close-" + clientID, event -> {
                                    wsSocket.close();
                                }));
                                authEvent.reply("");
                            }));

                    // 添加主动断开监听
                    consumers.add(Contant.EVENT_BUS.consumer("close-" + clientID, event -> {
                        wsSocket.close();
                    }));
                    wsSocket.closeHandler(v -> consumers.forEach(MessageConsumer::unregister));
                })
                .listen(cmdPort, "0.0.0.0", r -> {
                    if (r.succeeded()) {
                        log.info("CMD Server启动成功 cmdPort:{}", cmdPort);
                    } else {
                        log.warn("CMD Server启动失败 cmdPort:{}", cmdPort, r.cause());
                    }
                });
    }

    public static void handler(String clientID, Buffer body) {
        HANDLERS.forEach(handler -> {
            if (handler.needDeal(clientID, body)) {
                BaseEntity<?> entity = handler.buffer2Entity(clientID, body);
                handler.doDeal(clientID, entity);
            }
        });
    }
}
