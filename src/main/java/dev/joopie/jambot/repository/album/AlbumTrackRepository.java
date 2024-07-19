package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.model.AlbumTrack;
import org.springframework.data.repository.ListCrudRepository;

public interface AlbumTrackRepository extends ListCrudRepository<AlbumTrack, Long> {

}
