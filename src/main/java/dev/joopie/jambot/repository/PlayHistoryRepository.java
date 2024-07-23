package dev.joopie.jambot.repository;

import dev.joopie.jambot.model.PlayHistory;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayHistoryRepository extends ListCrudRepository<PlayHistory, Long> {
    void deleteByGuildId(String guildId);
}
