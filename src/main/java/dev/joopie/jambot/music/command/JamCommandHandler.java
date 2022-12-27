package dev.joopie.jambot.music.command;

import dev.joopie.jambot.command.CommandHandler;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JamCommandHandler implements CommandHandler {
    private static final String COMMAND_NAME = "jam";

    private final PlayCommandHandler playCommandHandler;

    @Override
    public Command.Type type() {
        return playCommandHandler.type();
    }

    @Override
    public CommandData registerCommand() {
        return playCommandHandler.registerCommand()
                .setName(COMMAND_NAME);
    }

    @Override
    public boolean shouldHandle(CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(CommandInteraction event) {
        return playCommandHandler.handle(event);
    }
}
