package dev.joopie.jambot.command;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;

public interface CommandHandler {
    Command.Type type();

    CommandData registerCommand();

    boolean shouldHandle(final CommandInteractionPayload event);

    RestAction<?> handle(final CommandInteraction event);
}
