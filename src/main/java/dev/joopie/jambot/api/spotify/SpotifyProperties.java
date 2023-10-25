package dev.joopie.jambot.api.spotify;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "red.jambot.spotify")
@Getter
@RequiredArgsConstructor
public class SpotifyProperties {
    /**
     * Spotify client id.
     */
    private final String clientId;

    /**
     * Spotify client secret.
     */
    private final String clientSecret;
}
