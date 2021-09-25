package dev.joopie.jambot.config.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "soundboard")
@ConstructorBinding
@Getter
@RequiredArgsConstructor
public class SoundBoardProperties {
    private final String filesUrl;
    private final String soundBaseUrl;
}
