package dev.joopie.jambot.healthcheck;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShouldRestartHealthIndicator implements HealthIndicator {
    private final RestartStatusHolder restartStatusHolder;

    @Override
    public Health health() {
        return restartStatusHolder.getRestartStatus()
                .filter(RestartStatus::status)
                .map(x -> Health.down().withDetail("initiated-by", x.initiatedBy()))
                .orElseGet(Health::up)
                .build();
    }
}
