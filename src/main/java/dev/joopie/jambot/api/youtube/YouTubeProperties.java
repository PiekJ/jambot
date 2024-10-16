package dev.joopie.jambot.api.youtube;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "red.jambot.youtube")
@Getter
@RequiredArgsConstructor
public class YouTubeProperties {
    /**
     * YouTube API token from Google Cloud Platform.
     */
    private final String token;
    private final int videoDurationOffset;
    private final int minimalViewCount;
}
