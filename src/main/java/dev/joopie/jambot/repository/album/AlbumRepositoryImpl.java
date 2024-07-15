package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.models.album.Album;
import dev.joopie.jambot.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AlbumRepositoryImpl extends BaseRepository<Album> implements AlbumRepository {
}
