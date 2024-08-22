package icu.hilin.tick.server;

import cn.hutool.core.util.IdUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TickerServerApplication {

    // 服务器id
    public static final long SERVER_ID = IdUtil.getSnowflakeNextId();
    public static final String SERVER_ID_STR = SERVER_ID + "";

    public static void main(String[] args) {
        new SpringApplicationBuilder(TickerServerApplication.class)
                .run(args);
    }

}
