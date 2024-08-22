package icu.hilin.tick.server.cmd;

import cn.hutool.core.util.IdUtil;
import icu.hilin.tick.core.TickConstant;
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

import java.util.*;

@Slf4j
@Component
public class CmdServer implements ApplicationRunner {

    private final Set<BaseCmdHandler<BaseEntity<Object>>> handlers = new HashSet<>();

    @Autowired
    public void setHandlers(ApplicationContext context) {
        context.getBeansOfType(BaseCmdHandler.class).forEach((s, baseCmdHandler) -> handlers.add(baseCmdHandler));
    }

    @Value("${server.cmdPort:8081}")
    private int cmdPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TickConstant.VERTX.createHttpServer()
                .webSocketHandler(wsSocket -> {
                    Long id = IdUtil.getSnowflakeNextId();
                    List<MessageConsumer<Buffer>> consumers = new ArrayList<>();

                    // 关闭客户端与ws服务器的连接
                    consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CMD_SERVER, "close", id),
                            event -> wsSocket.close()));

                    // 服务端发送数据到客户端
                    consumers.add(TickConstant.EVENT_BUS.consumer(String.format(TickConstant.CMD_SERVER, "send", id),
                            event ->
                                    wsSocket.write(event.body())
                    ));

                    // 收到客户端数据
                    wsSocket.handler(buf -> {
                        handlers
                                .forEach(baseCmdHandler -> {
                                    if (baseCmdHandler.needDeal(id, buf)) {
                                        baseCmdHandler.doDeal(id, baseCmdHandler.buffer2Entity(id, buf));
                                    }
                                });
                    });

                    wsSocket.closeHandler(v -> {
                        // 取消所有事件监听
                        consumers.forEach(MessageConsumer::unregister);
                    });

                })
                .listen(cmdPort, "0.0.0.0", r -> {
                    if (r.succeeded()) {
                        log.info("Cmd Started!!! port: {}", cmdPort);
                    } else {
                        log.warn("Cmd Failed!!! port: {}", cmdPort, r.cause());
                    }
                });
    }
}
