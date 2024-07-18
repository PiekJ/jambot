package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.model.album.Album;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

import java.util.List;

import static dev.joopie.jambot.model.album.Album.EXTERNALID;
import static dev.joopie.jambot.model.album.Album.NAME;

public class AlbumFinder extends BaseFinder<Album> {

    AlbumFinder() {
        super(Album.class);
    }

    public List<Album> byName(String albumName) {
        // We can have results for multiple album names
        return DB.find(Album.class).where().eq(NAME, albumName).findList();
    }

    public Album byExternalId(String externalId) {
        return DB.find(Album.class).where().eq(EXTERNALID, externalId).findOne();
    }

}
