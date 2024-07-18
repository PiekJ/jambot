package dev.joopie.jambot.model.album;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum AlbumGroup {
    ALBUM("album"),
    APPEARS_ON("appears_on"),
    COMPILATION("compilation"),
    SINGLE("single");

    private static final Map<String, AlbumGroup> MAP;
    public final String group;

    public static AlbumGroup keyOf(String type) {
        return MAP.get(type);
    }

    static {
        MAP = Arrays.stream(values())
                .collect(Collectors.toMap(x -> x.group, Function.identity()));
    }
}