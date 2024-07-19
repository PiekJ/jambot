package dev.joopie.jambot.api.soundboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.joopie.jambot.api.youtube.JambotYouTubeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiSoundBoardService {
    private final SoundBoardProperties properties;
    private final ObjectMapper objectMapper;

    public List<SoundAuthorDto> fetchSoundBoardSounds() {
        final HttpUriRequest request = RequestBuilder.get(properties.getFilesUrl())
                .build();

        try (final CloseableHttpClient client = HttpClients.createDefault();
             final CloseableHttpResponse response = client.execute(request)) {

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return Optional.ofNullable(response.getEntity())
                        .map(this::mapHttpEntityToFilesResponse)
                        .map(FilesResponse::getFiles)
                        .map(this::mapFilesResponseItemsToSoundAuthors)
                        .orElseThrow(JambotYouTubeException::new);
            } else {
                log.warn("Invalid http response status ({}) returned.", response.getStatusLine().getStatusCode());
            }
        } catch (IOException exception) {
            log.warn("SoundBoard did a oepsie.", exception);
        }

        return Collections.emptyList();
    }

    private FilesResponse mapHttpEntityToFilesResponse(final HttpEntity httpEntity) {
        try {
            return objectMapper.readValue(httpEntity.getContent(), FilesResponse.class);
        } catch (IOException exception) {
            throw new JambotYouTubeException("Whoeps, we couldn't handle that thick search response.", exception);
        }
    }

    private List<SoundAuthorDto> mapFilesResponseItemsToSoundAuthors(final List<FilesResponse.Item> items) {
        final HashMap<String,List<SoundAuthorDto.Sound>> soundMap = new HashMap<>();
        for (final FilesResponse.Item item : items) {
            final SoundAuthorDto.Sound sound = SoundAuthorDto.Sound.builder()
                    .file(formatSoundFile(item.getName()))
                    .title(item.getTitle())
                    .build();

            if (!soundMap.containsKey(item.getAuthor())) {
                soundMap.put(item.getAuthor(), new ArrayList<>());
            }

            soundMap.get(item.getAuthor()).add(sound);
        }

        return soundMap.entrySet().stream()
                .map(x -> SoundAuthorDto.builder()
                        .authorName(x.getKey())
                        .sounds(x.getValue())
                        .build())
                .toList();
    }

    private static final String VALUES = "!#$&'()*+,/:;=?@[] \"%-.<>\\^_`{|}~";

    private static String encode(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        final StringBuilder result = new StringBuilder(input);
        for (int i = input.length() - 1; i >= 0; i--) {
            if (VALUES.indexOf(input.charAt(i)) != -1) {
                result.replace(i, i + 1,
                        "%" + Integer.toHexString(input.charAt(i)).toUpperCase());
            }
        }
        return result.toString();
    }

    private String formatSoundFile(final String file) {
        return URI.create(properties.getSoundBaseUrl())
                .resolve("%s.mp3".formatted(encode(file)))
                .toASCIIString();
    }
}
