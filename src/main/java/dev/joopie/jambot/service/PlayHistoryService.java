package dev.joopie.jambot.service;


import dev.joopie.jambot.model.PlayHistory;
import dev.joopie.jambot.repository.PlayHistoryRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayHistoryService {
    private final PlayHistoryRepository playHistoryRepository;
    public Optional<PlayHistory> save(PlayHistory playHistory) {
        return Optional.of(playHistoryRepository.save(playHistory));
    }

    public void deleteHistoryFromGuild(Guild guild) {
        playHistoryRepository.deletePlayHistoryByGuildId(guild.getId());
    }
}
