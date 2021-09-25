package dev.joopie.jambot.music.commands;

import dev.joopie.jambot.api.youtube.ApiYouTubeService;
import dev.joopie.jambot.api.youtube.dto.SearchResultDto;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.exceptions.JambotMusicPlayerException;
import dev.joopie.jambot.exceptions.JambotMusicServiceException;
import dev.joopie.jambot.exceptions.JambotYouTubeException;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.response.MessageResponse;
import dev.joopie.jambot.response.ReactionResponse;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PlayCommandHandler implements CommandHandler {
    private static final Pattern SHOULD_HANDLE_PATTERN = Pattern.compile("^-(p|play).*$");
    private static final Pattern INPUT_PATTERN = Pattern.compile("^-(p|play) (?<input>.*)$");
    private static final Pattern URL_PATTERN = Pattern.compile("^http(|s)://.*$");

    private final GuildMusicService musicService;
    private final ApiYouTubeService apiYouTubeService;

    @Override
    public boolean shouldHandle(final GuildMessageReceivedEvent event) {
        return SHOULD_HANDLE_PATTERN.matcher(event.getMessage().getContentRaw()).matches();
    }

    @Override
    public RestAction<?> handle(final GuildMessageReceivedEvent event) {
        final Matcher matcher = INPUT_PATTERN.matcher(event.getMessage().getContentRaw());
        if (!matcher.matches()) {
            return MessageResponse.reply(event.getMessage(), "Provide YouTube url. Syntax `-p <youtube-url, search term>`.");
        }

        try {
            final String input = matcher.group("input");

            if (URL_PATTERN.matcher(input).matches()) {
                musicService.play(event.getGuild(), event.getAuthor(), input);
            }
            else {
                final SearchResultDto dto = apiYouTubeService.searchForSong(input);
                if (dto.isFound()) {
                    musicService.play(event.getGuild(), event.getAuthor(), dto.getVideoId());
                    return MessageResponse.reply(
                            event.getMessage(),
                            createMessageEmbedOfSearchTrack(event.getGuild().getName(), dto));
                } else {
                    return ReactionResponse.fail(event.getMessage());
                }
            }
        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotYouTubeException exception) {
            return MessageResponse.reply(event.getMessage(), exception.getMessage());
        }

        return ReactionResponse.ok(event.getMessage());
    }

    private static MessageEmbed createMessageEmbedOfSearchTrack(final String guildName, final SearchResultDto dto) {
        return new EmbedBuilder()
                .setColor(new Color(0x0099FF))
                .setTitle("%s Track Queued".formatted(guildName))
                .setDescription(dto.getTitle())
                .setImage(dto.getThumbnailUrl())
                .build();
    }
}
