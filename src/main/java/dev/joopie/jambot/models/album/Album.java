package dev.joopie.jambot.models.album;

import dev.joopie.jambot.models.Artist;
import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.models.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Album extends BaseModel<Album> {
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
