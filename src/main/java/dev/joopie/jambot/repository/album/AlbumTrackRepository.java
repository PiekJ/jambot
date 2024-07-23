package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.model.AlbumTrack;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumTrackRepository extends ListCrudRepository<AlbumTrack, Long> {

}
