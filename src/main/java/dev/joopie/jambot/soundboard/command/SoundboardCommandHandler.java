package dev.joopie.jambot.soundboard.command;

import dev.joopie.jambot.command.CommandAutocomplete;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.api.soundboard.JambotSoundBoardException;
import dev.joopie.jambot.soundboard.SoundboardService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SoundboardCommandHandler implements CommandHandler, CommandAutocomplete {
    private static final String COMMAND_NAME = "soundboard";
    private static final String COMMAND_OPTION_AUTHOR_NAME = "author";

    private final SoundboardService soundboardService;

    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "Play a random author track from the soundboard")
                .addOption(OptionType.STRING, COMMAND_OPTION_AUTHOR_NAME, "Author of the track", true, true);
    }

    @Override
    public List<Command.Choice> autocomplete(final CommandAutoCompleteInteraction event) {
        return switch (event.getFocusedOption().getName()) {
            case COMMAND_OPTION_AUTHOR_NAME -> autocompleteOptionAuthor(event.getFocusedOption().getValue());
            default -> List.of();
        };
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {
        final var authorOption = event.getOption(COMMAND_OPTION_AUTHOR_NAME);

        if (Objects.isNull(authorOption)) {
            return event.reply("Provide author name known by the soundboard u tit.")
                    .setEphemeral(true);
        }

        final var authorName = authorOption.getAsString();

        try {
            soundboardService.playRandomSoundByAuthor(event.getMember(), authorName);

            return event.reply("WOOOO, we might have queued something of %s ;).".formatted(authorName))
                    .setEphemeral(true);
        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotSoundBoardException exception) {
            return event.reply(exception.getMessage())
                    .setEphemeral(true);
        }
    }

    private List<Command.Choice> autocompleteOptionAuthor(final String authorNameStartWith) {
        return soundboardService.autocompleteAuthorStartWith(authorNameStartWith).stream()
                .map(authorName -> new Command.Choice(authorName, authorName))
                .toList();
    }
}
