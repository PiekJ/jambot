package dev.joopie.jambot.healthcheck.commands;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.exceptions.JambotHealthcheckServiceException;
import dev.joopie.jambot.healthcheck.HealthCheckService;
import dev.joopie.jambot.response.MessageResponse;
import dev.joopie.jambot.response.ReactionResponse;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RestartBotCommandHandler implements CommandHandler {
    private static final Pattern SHOULD_HANDLE_PATTERN = Pattern.compile("^-restartbot$");

    private final HealthCheckService healthCheckService;

    @Override
    public boolean shouldHandle(GuildMessageReceivedEvent event) {
        return SHOULD_HANDLE_PATTERN.matcher(event.getMessage().getContentRaw()).matches();
    }

    @Override
    public RestAction<?> handle(GuildMessageReceivedEvent event) {
        try {
            healthCheckService.markAsUnhealthy(event.getGuild(), event.getAuthor());
        } catch (JambotHealthcheckServiceException exception) {
            return MessageResponse.reply(event.getMessage(), exception.getMessage());
        }

        return ReactionResponse.ok(event.getMessage());
    }
}
