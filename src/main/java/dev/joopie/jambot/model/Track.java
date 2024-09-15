package dev.joopie.jambot.model;

import dev.joopie.jambot.model.album.Album;
import dev.joopie.jambot.model.base.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Track extends BaseModel {
    public static final String EXTERNALID_FIELD = "external_id";

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

    private BigInteger duration;
    private LocalDate releaseDate;

    @OneToMany(mappedBy = "track", fetch = FetchType.EAGER)
    private List<TrackSource> trackSources;

    public String getFormattedTrack() {
        return "%s-%s".formatted(
                getArtists().stream()
                        .map(Artist::getName)
                        .collect(Collectors.joining(",")),
                getName());
    }
}
