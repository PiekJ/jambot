package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

public class TrackFinder extends BaseFinder<Track> {
    private static final String EXTERNALID = "external_id";

    TrackFinder() {
        super(Track.class);
    }

    public Track byExternalId(String externalId) {
        return DB.find(Track.class).where().eq(EXTERNALID, externalId).findOne();
    }
}
