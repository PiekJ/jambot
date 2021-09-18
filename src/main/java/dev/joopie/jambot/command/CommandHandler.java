package dev.joopie.jambot.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

public interface CommandHandler{
    boolean shouldHandle(final GuildMessageReceivedEvent event);

    RestAction<?> handle(final GuildMessageReceivedEvent event);
}
