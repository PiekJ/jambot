package dev.joopie.jambot.music.command;

import dev.joopie.jambot.api.youtube.JambotYouTubeException;
import dev.joopie.jambot.command.CommandAutocomplete;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.repository.artist.ArtistRepository;
import dev.joopie.jambot.repository.track.TrackRepository;
import dev.joopie.jambot.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchCommandHandler extends ListenerAdapter implements CommandHandler, CommandAutocomplete {
    private static final String COMMAND_NAME = "search";
    private static final String COMMAND_OPTION_INPUT_ARTIST = "artist";
    private static final String COMMAND_OPTION_INPUT_SONGNAME = "songname";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    private final SearchService searchService;

    private final GuildMusicService musicService;

    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;

    @Override
    public Command.Type type() {
        return null;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "Search for a track")
                .addOption(
                        OptionType.STRING,
                        COMMAND_OPTION_INPUT_ARTIST,
                        "Name of your artist. If more than one, just provide one. If auto-complete does not give your option just type the name.",
                        true,true)
                .addOption(
                        OptionType.STRING,
                        COMMAND_OPTION_INPUT_SONGNAME,
                        "Name of your song. If auto-complete does not give your option just type in the name. Be as specific as you can. Include eq. Remix / Radio Edit / Re-Mastered etc.",
                        true,true);
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {

        final var inputArtistOption = event.getOption(COMMAND_OPTION_INPUT_ARTIST);
        final var inputTrackOption = event.getOption(COMMAND_OPTION_INPUT_SONGNAME);

        if (Objects.isNull(inputArtistOption)) {
            return event.reply("Please provide me an artist name")
                    .setEphemeral(true);
        }

        if (Objects.isNull(inputTrackOption)) {
            return event.reply("Please provide me an artist name")
                    .setEphemeral(true);
        }

        event.deferReply().queue();

        try {
            final var artistName = Objects.requireNonNull(inputArtistOption).getAsString();
            final var trackName = Objects.requireNonNull(inputTrackOption).getAsString();


            final var videoId = searchService.performSpotifyAndYoutubeSearch(artistName, trackName);

            if (videoId.isEmpty()) {
                return event.reply("We could not find any results with your search **%s - %s**".formatted(artistName, trackName)).setEphemeral(true);
            } else {
                musicService.play(event.getMember(), videoId);
                final var parsedVideoId = YOUTUBE_URL + videoId;

                // Create and send the message with buttons
                return event.getHook().sendMessage(
                                "**Track added!**%n%n OK, we added the track to the queue! In order to improve our service we would like%n to ask you to rate our search result.%n%n Please let us know :thumbsup_tone3: or :thumbsdown_tone3: if we got the correct result for you.%n%n %s".formatted(parsedVideoId))
                        .addActionRow(
                                Button.success("accept", "Accept").withEmoji(Emoji.fromUnicode("U+1F44D U+1F3FD")),
                                Button.danger("reject", "Reject").withEmoji(Emoji.fromUnicode("U+1F44E U+1F3FD"))
                        );
            }

        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotYouTubeException exception) {
            return event.getHook().sendMessage(exception.getMessage())
                    .setEphemeral(true);
        }
    }

    @Transactional
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals(COMMAND_NAME) && event.getFocusedOption().getName().equals(COMMAND_OPTION_INPUT_ARTIST)) {
            var options = artistRepository.findAll().stream()
                    .filter(artist -> artist.getName().startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(artist -> new Command.Choice(artist.getName(), artist.getName())) // map the words to choices
                    .toList();
            event.replyChoices(options).queue();
        }

        if (event.getName().equals(COMMAND_NAME) && event.getFocusedOption().getName().equals(COMMAND_OPTION_INPUT_SONGNAME)) {
            var options = trackRepository.findAll().stream()
                    .filter(artistTrack -> artistTrack.getArtists().stream()
                            .anyMatch(artist -> artist.getName().contains(event.getOptions().getFirst().getAsString())))
                    .filter(track -> track.getName().startsWith(event.getFocusedOption().getValue()))
                    .map(track -> new Command.Choice(track.getName(), track.getName()))
                    .toList();
            event.replyChoices(options).queue();
        }
    }

    @Override
    public List<Command.Choice> autocomplete(CommandAutoCompleteInteraction event) {
        return List.of();
    }
}