package dev.joopie.jambot.api.spotify;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "spotify")
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class SpotifyProperties {
    private final String clientId;
    private final String clientSecret;
}
