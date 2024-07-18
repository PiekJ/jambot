package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

import static dev.joopie.jambot.model.Track.EXTERNALID;

public class TrackFinder extends BaseFinder<Track> {

    TrackFinder() {
        super(Track.class);
    }

    public Track byExternalId(String externalId) {
        return DB.find(Track.class).where().eq(EXTERNALID, externalId).findOne();
    }
}
