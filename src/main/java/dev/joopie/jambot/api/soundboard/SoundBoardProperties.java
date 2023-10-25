package dev.joopie.jambot.api.soundboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "red.jambot.soundboard")
@Getter
@RequiredArgsConstructor
public class SoundBoardProperties {
    /**
     * Url of JSON file containing list of soundboard tracks.
     */
    private final String filesUrl;

    /**
     * Base url where soundboard tracks are located.
     */
    private final String soundBaseUrl;
}
