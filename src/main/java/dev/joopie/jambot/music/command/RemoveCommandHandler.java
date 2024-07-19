package dev.joopie.jambot.music.command;

import dev.joopie.jambot.command.CommandAutocomplete;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RemoveCommandHandler implements CommandHandler, CommandAutocomplete {
    private static final int COMMAND_OPTION_NAME_MAX_LENGTH = 100;
    private static final int SKIP_CURRENT_PLAYING_FROM_QUEUE = 1;
    private static final int COMMAND_OPTION_MAX_OPTIONS = 25;

    private static final String COMMAND_NAME = "remove";
    private static final String COMMAND_OPTION_TRACK_NUMBER = "tracknumber";

    private final GuildMusicService musicService;

    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "Remove track from the queue by its index")
                .addOption(
                        OptionType.INTEGER,
                        COMMAND_OPTION_TRACK_NUMBER,
                        "Track number (see queue list)",
                        true,
                        true);
    }

    @Override
    public List<Command.Choice> autocomplete(CommandAutoCompleteInteraction event) {
        return switch (event.getFocusedOption().getName()) {
            case COMMAND_OPTION_TRACK_NUMBER ->
                    autocompleteOptionTrackNumber(event.getGuild(), event.getFocusedOption().getValue());
            default -> List.of();
        };
    }

    @Override
    public boolean shouldHandle(CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(CommandInteraction event) {
        final OptionMapping trackNumberOption = event.getOption(COMMAND_OPTION_TRACK_NUMBER);
        if (Objects.isNull(trackNumberOption)) {
            return event.reply("How did you manage to not provide a track number?!")
                    .setEphemeral(true);
        }

        try {
            final int trackIndex = trackNumberOption.getAsInt() - 1;

            musicService.remove(event.getMember(), trackIndex);

            return event.reply("Ok, I did some cleanup and removed the track");
        } catch (JambotMusicServiceException | JambotMusicPlayerException exception) {
            return event.reply(exception.getMessage())
                    .setEphemeral(true);
        } catch (IllegalStateException | ArithmeticException exception) {
            return event.reply("Invalid track number given. Enter numeric value.")
                    .setEphemeral(true);
        }
    }

    private List<Command.Choice> autocompleteOptionTrackNumber(final Guild guild, final String searchText) {
        final String searchTextLowerCase = searchText.toLowerCase();
        final int possibleTrackNumber = tryParseInt(searchText);
        return musicService.getQueuedAudioTracks(guild).stream()
                .skip(SKIP_CURRENT_PLAYING_FROM_QUEUE)
                .filter(dto -> dto.getIndex() == possibleTrackNumber
                        || dto.getAuthor().toLowerCase().contains(searchTextLowerCase)
                        || dto.getTitle().toLowerCase().contains(searchTextLowerCase))
                .limit(COMMAND_OPTION_MAX_OPTIONS)
                .map(dto -> new Command.Choice(
                        stringWithMaxLength(
                                "%02d) %s : %s".formatted(dto.getIndex(), dto.getAuthor(), dto.getTitle()),
                                COMMAND_OPTION_NAME_MAX_LENGTH),
                        dto.getIndex()))
                .toList();
    }

    private static int tryParseInt(final String songNumber) {
        try {
            return Integer.parseInt(songNumber);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static String stringWithMaxLength(final String string, final int maxLength) {
        if (string.length() <= maxLength) {
            return string;
        }

        return string.substring(0, maxLength - 1).concat("…");
    }
}
