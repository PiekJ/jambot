package dev.joopie.jambot.repository.artist;

import dev.joopie.jambot.model.Artist;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends ListCrudRepository<Artist, Long> {
    Optional<Artist> findByExternalId(String id);
}
