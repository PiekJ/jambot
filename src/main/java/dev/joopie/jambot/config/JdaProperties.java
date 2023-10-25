package dev.joopie.jambot.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "red.jambot.jda")
@Getter
@RequiredArgsConstructor
public class JdaProperties {

    /**
     * Discord developer application token.
     */
    private final String token;
}
