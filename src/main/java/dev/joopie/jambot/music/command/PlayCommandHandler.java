package dev.joopie.jambot.music.command;

import dev.joopie.jambot.api.spotify.ApiSpotifyService;
import dev.joopie.jambot.api.youtube.ApiYouTubeService;
import dev.joopie.jambot.api.youtube.SearchResultDto;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.api.youtube.JambotYouTubeException;
import dev.joopie.jambot.music.GuildMusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayCommandHandler implements CommandHandler {
    private static final String COMMAND_NAME = "play";
    private static final String COMMAND_OPTION_INPUT_NAME = "input";
    private static final String SPOTIFY_URL = "spotify";
    private static final Pattern URL_OR_ID_PATTERN = Pattern.compile("^(http(|s)://.*|[\\w\\-]{11})$");
    private final GuildMusicService musicService;
    private final ApiYouTubeService apiYouTubeService;
    private final ApiSpotifyService apiSpotifyService;
    private final boolean isAvailable = false;

    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "play a track url or search for one")
                .addOption(
                        OptionType.STRING,
                        COMMAND_OPTION_INPUT_NAME,
                        "YouTube, Soundcloud, Spotify url or search term",
                        true);
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {
        final var inputOption = event.getOption(COMMAND_OPTION_INPUT_NAME);

        if (Objects.isNull(inputOption)) {
            return event.reply("U whut m8, provide an track url or search term.")
                    .setEphemeral(true);
        }

        event.deferReply().queue();

        try {
            final var input = inputOption.getAsString();

            if (input.contains(SPOTIFY_URL)) {
                return handleSpotifyLink(event, input);
            }

            if (URL_OR_ID_PATTERN.matcher(input).matches()) {
                return handleNormalLink(event, input);
            } else {
                return handleYoutubeSearch(event, input);
            }

        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotYouTubeException exception) {
            return event.getHook().sendMessage(exception.getMessage())
                    .setEphemeral(true);
        }
    }

    private WebhookMessageCreateAction<Message> handleYoutubeSearch(CommandInteraction event, String input) {
        final var dto = apiYouTubeService.searchForSong(input);
        if (dto.isFound()) {
            musicService.play(event.getGuild(), event.getUser(), dto.getVideoId());

            return event.getHook()
                    .sendMessageEmbeds(createMessageEmbedOfSearchTrack(event.getGuild().getName(), dto));
        }
        return event.getHook()
                .sendMessage("Or the internet is broken, or we did not have Dora the Explorer to help to find the track that you are looking for. No results found for the given search result")
                .setEphemeral(true);
    }


    private WebhookMessageCreateAction<Message> handleNormalLink(CommandInteraction event, String input) {
        musicService.play(event.getGuild(), event.getUser(), input);
        return event.getHook()
                .sendMessage("OK, we added the track to the queue! %s".formatted(input));
    }


    private WebhookMessageCreateAction<Message> handleSpotifyLink(CommandInteraction event, String input) {
        if (input.contains("track")) {
            final var spotifyResult = apiSpotifyService.getTrack(input);
            if (spotifyResult.isEmpty()) {
                return event.getHook()
                        .sendMessage("That's bad! We could not find anything for the given Spotify link. Did you copy the correct link?")
                        .setEphemeral(true);
            }
            return handleYoutubeSearch(event, spotifyResult.get());

        } else if (input.contains("playlist")) {
            if (isAvailable) {
                final var counter = new AtomicInteger(0);
                apiSpotifyService.getTracksFromPlaylist(input)
                        .forEach(result -> {
                            var playlistresult = apiYouTubeService.searchForSong(result);

                            if (playlistresult.isFound()) {
                                musicService.play(event.getGuild(), event.getUser(), playlistresult.getVideoId());
                                counter.getAndIncrement();
                            }
                        });

                return event.getHook()
                        .sendMessage("Imported playlist. Queued %s items!".formatted(counter));
            } else {
                return event.getHook()
                        .sendMessage("The use of Spotify Playlist links is currently not available")
                        .setEphemeral(true);
            }
        }
        return event.getHook()
                .sendMessage("Well this is akward. But I do not know what to do with this Spotify Link")
                .setEphemeral(true);
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
