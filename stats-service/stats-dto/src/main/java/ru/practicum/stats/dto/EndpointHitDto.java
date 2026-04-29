package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record EndpointHitDto(Long id, String app, String uri, String ip,
                             @JsonFormat(pattern = StatsConstants.DATE_TIME_PATTERN) LocalDateTime timestamp) {
}