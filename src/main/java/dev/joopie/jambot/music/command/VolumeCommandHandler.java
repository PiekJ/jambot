package dev.joopie.jambot.music.command;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.music.GuildMusicService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class VolumeCommandHandler implements CommandHandler {
    private static final String COMMAND_NAME = "volume";
    private static final String COMMAND_OPTION_VOLUME_NAME = "volume";

    private final GuildMusicService musicService;

    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "Set playback volume")
                .addOption(
                        OptionType.INTEGER,
                        COMMAND_OPTION_VOLUME_NAME,
                        "Volume between 0 - 200%",
                        true);
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {
        final var volumeOption = event.getOption(COMMAND_OPTION_VOLUME_NAME);
        if (Objects.isNull(volumeOption)) {
            return event.reply("How did you manage to not provide the volume?!")
                    .setEphemeral(true);
        }

        try {
            final var volume = volumeOption.getAsInt();

            musicService.volume(event.getMember(), volume);

            return event.reply("Ok, volume changed to %s%%, thank you very much!".formatted(volume));
        } catch (JambotMusicServiceException | JambotMusicPlayerException exception) {
            return event.reply(exception.getMessage())
                    .setEphemeral(true);
        } catch (IllegalStateException | ArithmeticException exception) {
            return event.reply("Invalid volume given. Enter numeric value.")
                    .setEphemeral(true);
        }
    }
}
