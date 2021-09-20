package dev.joopie.jambot;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.GuildMusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JambotListener extends ListenerAdapter {
    private final GuildMusicService guildMusicService;
    private final List<? extends CommandHandler> commandHandlers;

    @Override
    public void onGuildReady(@NotNull final GuildReadyEvent event) {
        guildMusicService.initializeGuildMusicService(event.getGuild());
        log.info("Guild `%s` is ready!".formatted(event.getGuild().getName()));
    }

    @Override
    public void onGuildJoin(@NotNull final GuildJoinEvent event) {
        guildMusicService.initializeGuildMusicService(event.getGuild());
        log.info("Joined guild `%s`.".formatted(event.getGuild().getName()));
    }

    @Override
    public void onGuildLeave(@NotNull final GuildLeaveEvent event) {
        guildMusicService.destroyGuildMusicService(event.getGuild());
        log.info("Left guild `%s`.".formatted(event.getGuild().getName()));
    }

    @Override
    public void onPrivateMessageReceived(@NotNull final PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }

        log.info("[PRIVATE] Received message: %s"
                .formatted(event.getMessage().getContentRaw()));
    }

    @Override
    public void onGuildMessageReceived(@NotNull final GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }

        log.info("[%s] Received message: %s"
                .formatted(event.getGuild().getName(), event.getMessage().getContentRaw()));

        commandHandlers.stream()
                .filter(x -> x.shouldHandle(event))
                .findFirst()
                .map(x -> x.handle(event))
                .ifPresent(RestAction::queue);
    }
}
