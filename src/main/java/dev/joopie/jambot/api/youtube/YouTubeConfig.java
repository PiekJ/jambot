package dev.joopie.jambot.api.youtube;

import dev.joopie.jambot.api.youtube.YouTubeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(YouTubeProperties.class)
public class YouTubeConfig {
}
