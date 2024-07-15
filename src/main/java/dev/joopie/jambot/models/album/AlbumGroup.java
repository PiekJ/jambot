package dev.joopie.jambot.models.album;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum AlbumGroup {
    ALBUM("album"),
    APPEARS_ON("appears_on"),
    COMPILATION("compilation"),
    SINGLE("single");

    private static final Map<String, AlbumGroup> map = new HashMap<>();
    public final String group;

    AlbumGroup(String group) {
        this.group = group;
    }

    public static AlbumGroup keyOf(String type) {
        return map.get(type);
    }

    public String getGroup() {
        return this.group;
    }

    static {
        AlbumGroup[] var0 = values();
        int var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2) {
            AlbumGroup albumGroup = var0[var2];
            map.put(albumGroup.group, albumGroup);
        }
    }
}