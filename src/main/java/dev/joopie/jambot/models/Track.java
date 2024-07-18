package dev.joopie.jambot.models;

import dev.joopie.jambot.models.album.Album;
import dev.joopie.jambot.models.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Track extends BaseModel<Track> {

    @OneToMany
    @JoinTable(
            name = "track_artist",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private List<Artist> artists;

    private String name;

    @OneToMany(targetEntity = Album.class)
    @JoinTable(name = "album_track",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id"))
    private Set<Album> album;

    private String externalId;

    private Duration duration;

    @OneToOne(mappedBy = "track", fetch = FetchType.EAGER)
    private TrackSource trackSource;

    public String getFormattedTrack() {
        var artists = getArtists().stream()
                .map(Artist::getName)
                .collect(Collectors.joining(","));
        return "%s-%s".formatted(artists, getName());
    }
}
