package icu.hilin.tick.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TickerServerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TickerServerApplication.class)
                .run(args);
    }

}
