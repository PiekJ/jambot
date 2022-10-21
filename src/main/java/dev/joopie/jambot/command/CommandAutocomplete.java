package dev.joopie.jambot.command;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;

import java.util.List;

public interface CommandAutocomplete {
    List<Command.Choice> autocomplete(final CommandAutoCompleteInteraction event);
}
