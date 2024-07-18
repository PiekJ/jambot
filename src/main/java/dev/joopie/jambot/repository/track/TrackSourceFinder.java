package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.model.TrackSource;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

import static dev.joopie.jambot.model.TrackSource.YOUTUBEID;

public final class TrackSourceFinder extends BaseFinder<TrackSource> {


    TrackSourceFinder() {
        super(TrackSource.class);
    }

    public TrackSource byYoutubeId(String youtubeId) {
        return DB.find(TrackSource.class).where().eq(YOUTUBEID, youtubeId).and().eq("rejected", false).findOne();
    }

    public TrackSource bySpotifyId(String youtubeId) {
        return DB.find(TrackSource.class).where().eq(YOUTUBEID, youtubeId).and().eq("rejected", false).findOne();
    }

    public TrackSource byRejectedYoutubeId(String youtubeId) {
        return DB.find(TrackSource.class).where().eq(YOUTUBEID, youtubeId).findOne();
    }
}
