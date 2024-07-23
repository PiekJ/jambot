package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.model.album.Album;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlbumRepository extends ListCrudRepository<Album, Long> {
    Optional<Album> findByExternalId(String externalId);
}
