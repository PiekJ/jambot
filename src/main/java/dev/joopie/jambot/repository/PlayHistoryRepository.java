package dev.joopie.jambot.repository;

import dev.joopie.jambot.model.PlayHistory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayHistoryRepository extends ListCrudRepository<PlayHistory, Long> {
    void deleteByGuildId(String guildId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM play_history WHERE user_id = :userId AND track_id = :trackId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    void deleteLatestByUserIdAndTrackId(@Param("userId") String userId, @Param("trackId") Long trackId);
}
