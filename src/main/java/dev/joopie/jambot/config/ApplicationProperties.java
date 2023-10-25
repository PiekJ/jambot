package dev.joopie.jambot.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "red.jambot.app")
@Getter
@RequiredArgsConstructor
public class ApplicationProperties {
    /**
     * Discord user id of who can do special admin commands.
     */
    private final String adminUserId;

    /**
     * Secret name to update Discord slash commands.
     */
    private final String updateCommandsSecret;
}
