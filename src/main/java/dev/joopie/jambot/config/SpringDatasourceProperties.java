package dev.joopie.jambot.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@RequiredArgsConstructor
public class SpringDatasourceProperties {
    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;
}
