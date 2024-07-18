package dev.joopie.jambot.repository.artist;

import dev.joopie.jambot.model.Artist;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

import java.util.List;

import static dev.joopie.jambot.model.Artist.EXTERNALID;
import static dev.joopie.jambot.model.Artist.NAME;

public class ArtistFinder extends BaseFinder<Artist> {
    ArtistFinder() {
        super(Artist.class);
    }

    public List<Artist> byName(String artistName) {
        // List because by name can of course result in multiple artists
        return DB.find(Artist.class).where().eq(NAME, artistName).findList();
    }

    public Artist byExternalId(String externalId) {
        return DB.find(Artist.class).where().eq(EXTERNALID, externalId).findOne();
    }
}
