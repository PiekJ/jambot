package dev.joopie.jambot.music.command;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.JambotMusicPlayerException;
import dev.joopie.jambot.music.JambotMusicServiceException;
import dev.joopie.jambot.music.GuildMusicService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaveCommandHandler implements CommandHandler {
    private static final String COMMAND_NAME = "leave";

    private final GuildMusicService musicService;

    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "Disconnect the bot from the audio channel");
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {
        try {
            musicService.leave(event.getMember());

            return event.reply("Ok, I'll leave now ;(");
        } catch (JambotMusicServiceException | JambotMusicPlayerException exception) {
            return event.reply(exception.getMessage())
                    .setEphemeral(true);
        }
    }
}
