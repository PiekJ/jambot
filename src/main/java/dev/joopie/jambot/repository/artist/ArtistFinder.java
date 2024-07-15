package dev.joopie.jambot.repository.artist;

import dev.joopie.jambot.models.Artist;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

import java.util.List;

public class ArtistFinder extends BaseFinder<Artist> {
    private static final String NAME = "name";
    private static final String EXTERNALID = "external_id";

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
