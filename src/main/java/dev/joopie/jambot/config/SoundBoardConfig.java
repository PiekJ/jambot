package dev.joopie.jambot.config;

import dev.joopie.jambot.config.properties.SoundBoardProperties;
import dev.joopie.jambot.soundboard.SoundBoardService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SoundBoardProperties.class)
public class SoundBoardConfig {
}
