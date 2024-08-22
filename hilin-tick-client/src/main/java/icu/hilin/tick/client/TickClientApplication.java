package icu.hilin.tick.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@Slf4j
public class TickClientApplication   {

    public static void main(String[] args) {
        new SpringApplicationBuilder(TickClientApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}
