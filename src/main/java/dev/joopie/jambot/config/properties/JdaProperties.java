package dev.joopie.jambot.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "jda")
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class JdaProperties {
    private final String token;
}
