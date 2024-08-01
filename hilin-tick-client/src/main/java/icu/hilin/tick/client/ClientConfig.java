package icu.hilin.tick.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "icu.hilin.tick.client")
public class ClientConfig {

    private String clientId;
    private String clientPassword;

}
