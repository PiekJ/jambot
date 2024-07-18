package dev.joopie.jambot.model.album;

import dev.joopie.jambot.model.Artist;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Album extends BaseModel {
    public static final String NAME = "name";
    public static final String EXTERNALID = "external_id";

    private String name;
    private AlbumGroup albumGroup;
    private AlbumType albumType;
    private String externalId;

    @ManyToMany(targetEntity = Track.class)
    private Set<Track> trackList;

    @OneToMany
    @JoinTable(
            name = "album_artist",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private List<Artist> artists;
}
