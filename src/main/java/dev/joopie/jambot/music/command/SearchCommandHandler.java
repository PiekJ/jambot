package dev.joopie.jambot.music.command;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchCommandHandler extends ListenerAdapter implements CommandHandler {
    private static final String COMMAND_NAME = "search";
    private static final String COMMAND_OPTION_INPUT_ARTIST = "artist";
    private static final String COMMAND_OPTION_INPUT_SONGNAME = "songname";

    private final SearchService searchService;

    private final PlayCommandHandler playCommandHandler;

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
                        "Name of your artist. If more than one, just provide one",
                        true)
                .addOption(
                        OptionType.STRING,
                        COMMAND_OPTION_INPUT_SONGNAME,
                        "Name of your song. But as specific as you can. Include eq. Remix / Radio Edit / Re-Mastered",
                        true);
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

        final var artistName = Objects.requireNonNull(inputArtistOption).getAsString();
        final var trackName = Objects.requireNonNull(inputTrackOption).getAsString();


        final var videoId = searchService.performSpotifyAndYoutubeSearch(artistName, trackName);

        if (videoId.isEmpty()) {
            return event.getHook().sendMessage(String.format("We could not find any results given with your search *%s - %s*",artistName, trackName)).setEphemeral(true);
        } else {
            return playCommandHandler.handlePlay(event, videoId);
        }

    }
}