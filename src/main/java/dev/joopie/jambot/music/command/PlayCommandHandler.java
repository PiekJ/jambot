package dev.joopie.jambot.music.command;

import dev.joopie.jambot.api.youtube.JambotYouTubeException;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayCommandHandler extends ListenerAdapter implements CommandHandler {
    private static final String COMMAND_NAME = "play";
    private static final String COMMAND_OPTION_INPUT_NAME = "input";
    private static final String SPOTIFY = "spotify";
    private static final Pattern URL_OR_ID_PATTERN = Pattern.compile("^(http(|s)://.*|[\\w\\-]{11})$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(http(|s)://.*)$");
    private static final Pattern YOU_TUBE_URL_PATTERN = Pattern.compile("^(https?)?(://)?(www\\.)?(m\\.)?((youtube\\.com)|(youtu\\.be))/");
    private static final Pattern[] VIDEO_ID_PATTERNS = {
            Pattern.compile("\\?vi?=([^&]*)"),
            Pattern.compile("watch\\?.*v=([^&]*)"),
            Pattern.compile("(?:embed|vi?)/([^/?]*)"),
            Pattern.compile("^([A-Za-z0-9\\-]*)")
    };

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

    @Transactional
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

            if (!URL_PATTERN.matcher(input).matches()) {
                return event.getHook().sendMessage("In order to use this command, a **link** must be provided. If you want to search for music please use our `/search` command")
                        .setEphemeral(true);
            }

            if (input.contains(SPOTIFY)) {
                var spotifySearchResult = handleSpotifyLink(input);

                if (spotifySearchResult.isPresent()) {
                    return handlePlayWithInteractions(event, spotifySearchResult.get());
                } else {
                    return event.getHook().sendMessage("Hey wizard! :man_mage::skin-tone-1: You have a magic Spotify link over there. We could not find any results!").setEphemeral(true);
                }
            } else if (input.contains("youtube") || input.contains("youtu.be")) {
                var extractedId = extractYouTubeVideoId(input);

                if (extractedId == null) {
                    return event.getHook().sendMessage("Couldn't extract youtube video ID from your link.").setEphemeral(true);
                }

                return handlePlay(event, extractedId);
            } else {
                return handlePlay(event, input);
            }


        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotYouTubeException exception) {
            return event.getHook().sendMessage(exception.getMessage())
                    .setEphemeral(true);
        }
    }

    public WebhookMessageCreateAction<Message> handlePlayWithInteractions(final CommandInteraction event, final String videoId) {
        if (Strings.isBlank(videoId)) {
            return event.getHook().sendMessage("Whoops! This video id got lost in the abyss.").setEphemeral(true);
        }

        musicService.play(event.getMember(), videoId);
        var parsedVideoId = GuildMusicService.YOUTUBE_URL + videoId;
            // Create and send the message with buttons
            return event.getHook().sendMessage(
                            "**Track added!**%n%n OK, we added the track to the queue! In order to improve our service we would like%n to ask you to rate our search result.%n%n Please let us know :thumbsup_tone3: or :thumbsdown_tone3: if we got the correct result for you.%n%n %s".formatted(parsedVideoId))
                    .addActionRow(
                            Button.success("accept", "Accept").withEmoji(Emoji.fromUnicode("U+1F44D U+1F3FD")),
                            Button.danger("reject", "Reject").withEmoji(Emoji.fromUnicode("U+1F44E U+1F3FD"))
                    );
    }

    public WebhookMessageCreateAction<Message> handlePlay(final CommandInteraction event, final String videoId) {
        var mediaUrl = videoId;
        if (videoId.length() == 11) {
            mediaUrl = GuildMusicService.YOUTUBE_URL + videoId;
        }
        musicService.play(event.getMember(), videoId);
        return event.getHook().sendMessage(
                        "**Track added!**%n%n OK, we added the track to the queue! %s".formatted(mediaUrl));

    }

    private Optional<String> handleSpotifyLink(final String input) {
        if (input.contains("track")) {
            final var spotifyResult = searchService.getTrack(input);
            return spotifyResult.isPresent() ? searchService.performYoutubeSearch(spotifyResult.get()) : Optional.empty();
        }

        return Optional.empty();
    }

    private String extractYouTubeVideoId(final String url) {
        var youTubeLinkWithoutProtocolAndDomain = removeProtocolAndDomain(url);

        for (var pattern : VIDEO_ID_PATTERNS) {
            var matcher = pattern.matcher(youTubeLinkWithoutProtocolAndDomain);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    private String removeProtocolAndDomain(final String url) {
        var matcher = YOU_TUBE_URL_PATTERN.matcher(url);

        if (matcher.find()) {
            return url.replace(matcher.group(), "");
        }
        return url;
    }
}
