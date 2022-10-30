package dev.joopie.jambot.api.soundboard;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FilesResponse {
    @Data
    @NoArgsConstructor
    public static class Item {
        private String name;
        private String title;
        private String author;
    }

    private List<Item> files;
}
