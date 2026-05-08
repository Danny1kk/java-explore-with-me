package ru.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class StatsClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(RestTemplate restTemplate,
                       @Value("${stats-server.url:http://localhost:9090}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void hit(EndpointHitDto hitDto) {
        try {
            restTemplate.postForEntity(baseUrl + "/hit", hitDto, Object.class);
        } catch (Exception e) {
            log.error("Не удалось отправить hit: app={}, uri={}", hitDto.app(), hitDto.uri(), e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/stats")
                    .queryParam("start", start.format(FORMATTER))
                    .queryParam("end", end.format(FORMATTER));

            if (uris != null && !uris.isEmpty()) {
                builder.queryParam("uris", String.join(",", uris));
            }

            if (unique != null) {
                builder.queryParam("unique", unique);
            }

            String url = builder.build().toUriString();

            ResponseEntity<ViewStatsDto[]> response =
                    restTemplate.getForEntity(url, ViewStatsDto[].class);

            return Arrays.asList(Objects.requireNonNull(response.getBody()));

        } catch (Exception e) {
            log.error("Не удалось получить статистику просмотров", e);
            return Collections.emptyList();
        }
    }
}