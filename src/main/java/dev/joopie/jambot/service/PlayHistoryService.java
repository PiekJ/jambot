package dev.joopie.jambot.service;


import dev.joopie.jambot.model.PlayHistory;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.repository.PlayHistoryRepository;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayHistoryService {
    private final PlayHistoryRepository playHistoryRepository;
    public PlayHistory save(PlayHistory playHistory) {
        return playHistoryRepository.save(playHistory);
    }
    @Transactional
    public void deleteLatestTrackEntry(final String userId, final Track track) {
         playHistoryRepository.deleteLatestByUserIdAndTrackId(userId, track.getId());
    }

    @Transactional
    public void deleteHistoryFromGuild(Guild guild) {
        playHistoryRepository.deleteByGuildId(guild.getId());
    }
}
