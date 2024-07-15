package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.models.TrackSource;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

public final class TrackSourceFinder extends BaseFinder<TrackSource> {
    private static String YOUTUBEID = "youtubeId";
    private static String SPOTIFYID = "spotifyId";

    TrackSourceFinder() {
        super(TrackSource.class);
    }

    public TrackSource byYoutubeId(String youtubeId) {
        return DB.find(TrackSource.class).where().eq(YOUTUBEID, youtubeId).findOne();
    }

    public TrackSource bySpotifyId(String spotifyId) {
        return DB.find(TrackSource.class).where().eq(SPOTIFYID, spotifyId).findOne();
    }
}
