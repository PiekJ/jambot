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

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class LeaveCommandHandler implements CommandHandler {
    private static final Pattern SHOULD_CHECK_PATTERN = Pattern.compile("^-(l|leave)$");

    private final GuildMusicService musicService;

    @Override
    public boolean shouldHandle(GuildMessageReceivedEvent event) {
        return SHOULD_CHECK_PATTERN.matcher(event.getMessage().getContentRaw()).matches();
    }

    @Override
    public RestAction<?> handle(GuildMessageReceivedEvent event) {
        try {
            musicService.leave(event.getGuild(), event.getAuthor());
        } catch (JambotMusicServiceException | JambotMusicPlayerException exception) {
            return MessageResponse.reply(event.getMessage(), exception.getMessage());
        }

        return ReactionResponse.ok(event.getMessage());
    }
}
