package dev.joopie.jambot.soundboard.commands;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.exceptions.JambotMusicPlayerException;
import dev.joopie.jambot.exceptions.JambotMusicServiceException;
import dev.joopie.jambot.exceptions.JambotSoundBoardException;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.response.MessageResponse;
import dev.joopie.jambot.response.ReactionResponse;
import dev.joopie.jambot.soundboard.SoundBoardService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SoundBoardCommandHandler implements CommandHandler {
    private static final Pattern SHOULD_HANDLE_PATTERN = Pattern.compile("^-(sb|soundboard).*");
    private static final Pattern INPUT_PATTERN = Pattern.compile("^-(sb|soundboard) (?<input>.*)$");

    private final SoundBoardService soundBoardService;

    @Override
    public boolean shouldHandle(final GuildMessageReceivedEvent event) {
        return SHOULD_HANDLE_PATTERN.matcher(event.getMessage().getContentRaw()).find();
    }

    @Override
    public RestAction<?> handle(GuildMessageReceivedEvent event) {
        final Matcher matcher = INPUT_PATTERN.matcher(event.getMessage().getContentRaw());
        if (!matcher.matches()) {
            return MessageResponse.reply(event.getMessage(), "Provide author name known by the soundboard. Syntax `-sb <author name>`.");
        }

        try {
            final String authorName = matcher.group("input");
            soundBoardService.playRandomSoundByAuthor(event.getGuild(), event.getAuthor(), authorName);
        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotSoundBoardException exception) {
            return MessageResponse.reply(event.getMessage(), exception.getMessage());
        }

        return ReactionResponse.ok(event.getMessage());
    }
}
