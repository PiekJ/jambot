package dev.joopie.jambot.model;

import dev.joopie.jambot.model.album.Album;
import dev.joopie.jambot.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Track extends BaseModel {
    public static final String EXTERNALID = "external_id";

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
        return String.format("%s-%s",
                getArtists().stream()
                        .map(Artist::getName)
                        .collect(Collectors.joining(",")),
                getName());

    }
}
