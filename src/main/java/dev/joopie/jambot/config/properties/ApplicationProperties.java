package dev.joopie.jambot.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class ApplicationProperties {
    private final String adminUserId;
    private final String updateCommandsSecret;
}
