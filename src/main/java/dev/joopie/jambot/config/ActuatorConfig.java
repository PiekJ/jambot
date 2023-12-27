package dev.joopie.jambot.config;

import dev.joopie.jambot.music.GuildMusicService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {
    @Bean
    public Gauge countGuildConnectedToVoiceGauge(MeterRegistry registry, GuildMusicService guildMusicService) {
        return Gauge.builder(
                        "red.jambot.guilds.voice.connected",
                        guildMusicService,
                        GuildMusicService::countGuildsConnectedToVoice)
                .register(registry);
    }

    @Bean
    public Gauge countGuilds(MeterRegistry registry, GuildMusicService guildMusicService) {
        return Gauge.builder(
                        "red.jambot.guilds.connected",
                        guildMusicService,
                        GuildMusicService::countGuilds)
                .register(registry);
    }
}
