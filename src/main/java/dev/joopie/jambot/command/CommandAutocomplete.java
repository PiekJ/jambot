package dev.joopie.jambot.command;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;

import java.util.List;

public interface CommandAutocomplete {
    int COMMAND_OPTION_MAX_OPTIONS = 25;

    List<Command.Choice> autocomplete(final CommandAutoCompleteInteraction event);
}
