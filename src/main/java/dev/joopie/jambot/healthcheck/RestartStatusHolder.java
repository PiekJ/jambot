package dev.joopie.jambot.healthcheck;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RestartStatusHolder {
    private volatile RestartStatus restartStatus;

    public Optional<RestartStatus> getRestartStatus() {
        return Optional.ofNullable(restartStatus);
    }

    public void setRestartStatus(boolean newStatus, String initiatedBy) {
        restartStatus = new RestartStatus(newStatus, initiatedBy);
    }
}
