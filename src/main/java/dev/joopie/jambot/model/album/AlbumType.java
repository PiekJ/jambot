package dev.joopie.jambot.model.album;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum AlbumType {
    ALBUM("album"),
    COMPILATION("compilation"),
    SINGLE("single");

    private static Map<String, AlbumType> map;
    private final String type;

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
        map = Arrays.stream(values())
                .collect(Collectors.toMap(x -> x.type, Function.identity()));
    }
}
