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
        AlbumType[] var0 = values();
        int var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2) {
            AlbumType albumType = var0[var2];
            map.put(albumType.type, albumType);
        }

    }
}
