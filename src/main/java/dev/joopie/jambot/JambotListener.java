package dev.joopie.jambot;

import dev.joopie.jambot.command.CommandAutocomplete;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.config.ApplicationProperties;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.service.PlayHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JambotListener extends ListenerAdapter {
    private final GuildMusicService guildMusicService;
    private final ApplicationProperties applicationProperties;
    private final List<? extends CommandHandler> commandHandlers;
    private final PlayHistoryService playHistoryService;

    @Override
    public void onGuildReady(@NotNull final GuildReadyEvent event) {
        guildMusicService.initializeGuildMusicService(event.getGuild());
        log.info("Guild `{}` is ready!", event.getGuild().getName());
    }

    @Override
    public void onGuildJoin(@NotNull final GuildJoinEvent event) {
        guildMusicService.initializeGuildMusicService(event.getGuild());
        log.info("Joined guild `{}`.", event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(@NotNull final GuildLeaveEvent event) {
        playHistoryService.deleteHistoryFromGuild(event.getGuild());
        guildMusicService.destroyGuildMusicService(event.getGuild());
        log.info("Left guild `{}`.", event.getGuild().getName());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull final GuildVoiceUpdateEvent event) {
        if (Objects.isNull(event.getChannelJoined())) {
            guildMusicService.leaveWhenLeftAlone(event.getGuild());
        }
    }

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) {
            return;
        }

        if (event.isFromGuild()) {
            log.warn(
                    "[{}:{}] Received message: {}",
                    event.getGuild().getName(),
                    event.getAuthor().getName(),
                    event.getMessage().getContentRaw());

            return;
        }

        log.info(
                "[PRIVATE:{}] Received message: {}",
                event.getAuthor().getName(),
                event.getMessage().getContentRaw());

        if (applicationProperties.getAdminUserId().equals(event.getAuthor().getId())
                && applicationProperties.getUpdateCommandsSecret().equals(event.getMessage().getContentRaw())) {
            event.getJDA().updateCommands()
                    .addCommands(
                            commandHandlers.stream()
                                    .map(CommandHandler::registerCommand)
                                    .toList())
                    .queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull final SlashCommandInteractionEvent event) {
        if (event.isFromGuild()) {
            if (event.getOptions().isEmpty()) {
                log.info("[{}:{}] Requested command `{}`",
                        event.getGuild().getName(),
                        event.getUser().getName(),
                        event.getName());
            } else {
                log.info("[{}:{}] Requested command `{}` with options: {}",
                        event.getGuild().getName(),
                        event.getUser().getName(),
                        event.getName(),
                        event.getOptions());
            }

            commandHandlers.stream()
                    .filter(x -> x.shouldHandle(event))
                    .findFirst()
                    .map(x -> x.handle(event))
                    .ifPresent(RestAction::queue);
        } else {
            event.reply("Command not available outside guild.")
                    .queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.isFromGuild()) {
            commandHandlers.stream()
                    .filter(x -> x.shouldHandle(event))
                    .filter(CommandAutocomplete.class::isInstance)
                    .findFirst()
                    .map(CommandAutocomplete.class::cast)
                    .map(x -> x.autocomplete(event))
                    .ifPresent(x -> event.replyChoices(x).queue());
        }
    }
}
