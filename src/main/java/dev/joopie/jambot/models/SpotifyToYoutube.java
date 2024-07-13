package dev.joopie.jambot.models;

import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "spotify_to_youtube")
public class SpotifyToYoutube extends BaseModel<SpotifyToYoutube> {
    private String youtubeId;
    private String spotifyId;

    @Override
    public boolean validateSave() {
        return !spotifyId.isEmpty() && !youtubeId.isEmpty();
    }
}
