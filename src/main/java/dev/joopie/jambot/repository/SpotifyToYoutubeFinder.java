package dev.joopie.jambot.repository;

import dev.joopie.jambot.models.SpotifyToYoutube;
import dev.joopie.jambot.repository.base.BaseFinder;
import io.ebean.DB;

public class SpotifyToYoutubeFinder extends BaseFinder<SpotifyToYoutube> {
    private static String YOUTUBEID = "youtubeId";
    private static String SPOTIFYID = "spotifyId";

    public SpotifyToYoutubeFinder() {
        super(SpotifyToYoutube.class);
    }

    public SpotifyToYoutube byYoutubeId(String youtubeId) {
        return DB.find(SpotifyToYoutube.class).where().eq(YOUTUBEID, youtubeId).findOne();
    }

    public SpotifyToYoutube bySpotifyId(String spotifyId) {
        return DB.find(SpotifyToYoutube.class).where().eq(SPOTIFYID, spotifyId).findOne();
    }
}
