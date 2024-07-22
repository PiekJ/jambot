package dev.joopie.jambot.model.album;

import java.util.HashMap;
import java.util.Map;

public enum AlbumType {
    ALBUM("album"),
    COMPILATION("compilation"),
    SINGLE("single");

    private static final Map<String, AlbumType> map = new HashMap<>();
    public final String type;

    AlbumType(String type) {
        this.type = type;
    }

    public static AlbumType keyOf(String type) {
        return map.get(type);
    }

    public String getType() {
        return this.type;
    }

    static {
        var var0 = values();
        var var1 = var0.length;

        for (AlbumType albumType : var0) {
            map.put(albumType.type, albumType);
        }

    }
}
