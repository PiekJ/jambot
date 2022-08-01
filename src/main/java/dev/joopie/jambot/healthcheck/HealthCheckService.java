package dev.joopie.jambot.healthcheck;

import dev.joopie.jambot.exceptions.JambotHealthcheckServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {
    private final RestartStatusHolder restartStatusHolder;

    public void markAsUnhealthy(final Guild guild, final User user) {
        var discordUserId = "%s:%s".formatted(user.getName(), user.getDiscriminator());

        var userMember = guild.retrieveMember(user).complete();

        if (Objects.isNull(userMember)) {
            throw new JambotHealthcheckServiceException(
                    "Can't find member %s in %s.".formatted(discordUserId, guild.getName()));
        }

        if (!userMember.hasPermission(Permission.MANAGE_SERVER)) {
            throw new JambotHealthcheckServiceException(
                    "Be warned, permission not granted! Are you being naughty, %s?".formatted(
                            userMember.getEffectiveName()));
        }

        log.warn("{} from {} marked Jambot as unhealthy causing a restart!", discordUserId, guild.getName());

        restartStatusHolder.setRestartStatus(true, discordUserId);
    }
}
