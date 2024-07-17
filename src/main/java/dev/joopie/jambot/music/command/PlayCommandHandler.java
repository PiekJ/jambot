package dev.joopie.jambot.music.command;

import dev.joopie.jambot.api.spotify.ApiSpotifyService;
import dev.joopie.jambot.api.youtube.ApiYouTubeService;
import dev.joopie.jambot.api.youtube.JambotYouTubeException;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.models.Artist;
import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.models.TrackSource;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.service.TrackService;
import dev.joopie.jambot.service.TrackSourceService;
import dev.joopie.jambot.util.SpotifyLinkParser;
import dev.joopie.jambot.util.YouTubeLinkParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayCommandHandler implements CommandHandler {
    private static final String COMMAND_NAME = "play";
    private static final String COMMAND_OPTION_INPUT_NAME = "input";
    private static final String SPOTIFY_URL = "spotify";
    private static final Pattern URL_OR_ID_PATTERN = Pattern.compile("^(http(|s)://.*|[\\w\\-]{11})$");
    public static final Pattern URL_PATTERN = Pattern.compile("^(http(|s)://.*)$");

    @Autowired
    private final GuildMusicService musicService;

    @Autowired
    private final ApiYouTubeService apiYouTubeService;

    @Autowired
    private final ApiSpotifyService apiSpotifyService;

    @Autowired
    private final TrackSourceService trackSourceService;

    @Autowired
    private final TrackService trackService;


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
            return event.reply("U whut m8, provide a track url or search term.")
                    .setEphemeral(true);
        }

        event.deferReply().queue();

        try {
            final var input = inputOption.getAsString();
            String videoId = Strings.EMPTY;

            if (!URL_OR_ID_PATTERN.matcher(input).matches()) {
               videoId = performSpotifyAndYoutubeSearch(input);;
            }

            if (input.contains(SPOTIFY_URL)) {
                videoId = handleSpotifyLink(input);
            } else if (URL_OR_ID_PATTERN.matcher(input).matches()) {
                videoId = input;
            }

            if (Strings.isEmpty(videoId)) {
                return event.getHook().sendMessage("Unfortunately we did not find any results!");
            }
            return handlePlay(event, videoId);

        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotYouTubeException exception) {
            return event.getHook().sendMessage(exception.getMessage())
                    .setEphemeral(true);
        }
    }

    public WebhookMessageCreateAction<Message> handlePlay(CommandInteraction event, String videoId) {
        musicService.play(event.getMember(), videoId);

        String parsedVideoId;
        if (URL_PATTERN.matcher(videoId).matches()) {
            parsedVideoId = videoId;
        } else {
            parsedVideoId = YouTubeLinkParser.parseIdToYouTubeWatchUrl(videoId);
        }

        WebhookMessageCreateAction<Message> action = event.getHook()
                .sendMessage("OK, we added the track to the queue! %s. Please let us know ✅ or ❌ if we got the correct result for you.".formatted(parsedVideoId));
        action.queue(message -> {
            message.addReaction(Emoji.fromUnicode("U+2705")).queue();
            message.addReaction(Emoji.fromUnicode("U+274C")).queue();
        });
        return action;
    }

    private String performYoutubeSearch(Track track) {
            TrackSource trackSource = new TrackSource();
            trackSource.setYoutubeId(apiYouTubeService.searchForSong(track.getFormattedTrack(), track.getDuration().minusSeconds(10), track.getDuration().plusSeconds(10), track.getArtists().stream().map(Artist::getName).collect(Collectors.toList())).getVideoId());
            trackSource.setSpotifyId(track.getExternalId());
            trackSource.setTrack(track);
            trackSourceService.save(trackSource);
            return trackSource.getYoutubeId();
    }

    private String performSpotifyAndYoutubeSearch(String input) {
        Optional<Track> track = apiSpotifyService.searchForTrack(input);

        if (track.isPresent() && track.get().getTrackSource() != null) {
            return track.get().getTrackSource().getYoutubeId();
        } else if (track.isPresent()) {
           return performYoutubeSearch(track.get());
        } else {
            return Strings.EMPTY;
        }
    }

    private String handleSpotifyLink(String input) {
        // First check if we have records in the DB
        Track track = trackService.findByExternalId(SpotifyLinkParser.extractSpotifyId(input));

        if (track != null && track.getTrackSource() != null && track.getTrackSource().getYoutubeId() != null) {
            return track.getTrackSource().getYoutubeId();
        }

        if (input.contains("track")) {

            final Optional<Track> spotifyResult = apiSpotifyService.getTrack(input);
            return spotifyResult.map(this::performYoutubeSearch).orElse(null);

        }

        return Strings.EMPTY;
    }
}
