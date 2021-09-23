package dev.joopie.jambot.config;

import dev.joopie.jambot.config.properties.YouTubeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(YouTubeProperties.class)
public class YouTubeConfig {
}
