package icu.hilin.tick.server;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TickerServerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TickerServerApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
