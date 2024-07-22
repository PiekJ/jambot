package dev.joopie.jambot.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LavaPlayerConfig {
    @Bean
    public AudioPlayerManager audioPlayerManager() {
        final var playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        playerManager.registerSourceManager(new dev.lavalink.youtube.YoutubeAudioSourceManager(false, true, false));
        return playerManager;
    }
}
