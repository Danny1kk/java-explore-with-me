package ru.practicum.stats.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class StatsConstants {
    private LocalDateTime start;
    private LocalDateTime end;
    private String[] uris;
    private Boolean unique;
}