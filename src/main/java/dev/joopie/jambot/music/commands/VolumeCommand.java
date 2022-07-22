package dev.joopie.jambot.music.commands;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.exceptions.JambotMusicPlayerException;
import dev.joopie.jambot.exceptions.JambotMusicServiceException;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.response.MessageResponse;
import dev.joopie.jambot.response.ReactionResponse;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class VolumeCommand implements CommandHandler {
    private static final Pattern SHOULD_HANDLE_PATTERN = Pattern.compile("^-(v|volume)");
    private static final Pattern INPUT_PATTERN = Pattern.compile("^-(v|volume) (?<input>.*)$");

    private final GuildMusicService musicService;

    @Override
    public boolean shouldHandle(final GuildMessageReceivedEvent event) {
        return SHOULD_HANDLE_PATTERN.matcher(event.getMessage().getContentRaw()).find();
    }

    @Override
    public RestAction<?> handle(final GuildMessageReceivedEvent event) {
        final Matcher matcher = INPUT_PATTERN.matcher(event.getMessage().getContentRaw());
        if (!matcher.matches()) {
            return MessageResponse.reply(event.getMessage(), "Provide volume (0%-200%). Syntax `-v <volume>`.");
        }

        try {
            final int volume = Integer.parseInt(matcher.group("input"));
            musicService.volume(event.getGuild(), event.getAuthor(), volume);
        } catch (JambotMusicServiceException | JambotMusicPlayerException exception) {
            return MessageResponse.reply(event.getMessage(), exception.getMessage());
        } catch (NumberFormatException exception) {
            return MessageResponse.reply(event.getMessage(), "Invalid volume given. Enter numeric value.");
        }

        return ReactionResponse.ok(event.getMessage());
    }
}
