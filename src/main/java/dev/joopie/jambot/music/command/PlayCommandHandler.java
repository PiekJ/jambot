package dev.joopie.jambot.music.command;

import dev.joopie.jambot.api.youtube.JambotYouTubeException;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayCommandHandler extends ListenerAdapter implements CommandHandler {
    private static final String COMMAND_NAME = "play";
    private static final String COMMAND_OPTION_INPUT_NAME = "input";
    private static final String SPOTIFY_URL = "spotify";
    private static final Pattern URL_OR_ID_PATTERN = Pattern.compile("^(http(|s)://.*|[\\w\\-]{11})$");
    public static final Pattern URL_PATTERN = Pattern.compile("^(http(|s)://.*)$");
    public static final int SECONDS_OFFSET = 15;
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    private final GuildMusicService musicService;

    private final SearchService searchService;


    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "Play a track url")
                .addOption(
                        OptionType.STRING,
                        COMMAND_OPTION_INPUT_NAME,
                        "YouTube, Soundcloud, Spotify url",
                        true);
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {
        final OptionMapping inputOption = event.getOption(COMMAND_OPTION_INPUT_NAME);

        if (Objects.isNull(inputOption)) {
            return event.reply("U whut m8, provide a track url or search term.")
                    .setEphemeral(true);
        }


        event.deferReply().queue();

        try {
            final String input = inputOption.getAsString();

            if (!URL_PATTERN.matcher(input).matches()) {
                return event.getHook().sendMessage("In order to use this command, a link must be provided. If you want to search for music please use our `/search` command")
                        .setEphemeral(true);
            }
            String videoId = Strings.EMPTY;

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

    public RestAction<?> handlePlay(CommandInteraction event, String videoId) {
        musicService.play(event.getMember(), videoId);
        String parsedVideoId;
        if (URL_PATTERN.matcher(videoId).matches()) {
            parsedVideoId = videoId;
        } else {
            parsedVideoId = YOUTUBE_URL + videoId;
        }

        // Create and send the message with buttons
        return event.getHook().sendMessage(
                    "**Track added!**%n%n OK, we added the track to the queue! In order to improve our service we would like%n to ask you to rate our search result.%n%n Please let us know :thumbsup_tone3: or :thumbsdown_tone3: if we got the correct result for you.%n%n %s".formatted(parsedVideoId))
                .addActionRow(
                        Button.success("accept", "Accept").withEmoji(Emoji.fromUnicode("U+1F44D U+1F3FD")),
                        Button.danger("reject", "Reject").withEmoji(Emoji.fromUnicode("U+1F44E U+1F3FD"))
                );
        
    }

    private String handleSpotifyLink(String input) {
        if (input.contains("track")) {
            final Optional<Track> spotifyResult = searchService.getTrack(input);
            return spotifyResult.map(searchService::performYoutubeSearch).orElse(null);
        }

        return Strings.EMPTY;
    }
}
