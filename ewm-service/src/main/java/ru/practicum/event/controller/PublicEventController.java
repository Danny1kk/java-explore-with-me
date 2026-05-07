package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        List<EventShortDto> events = eventService.searchPublic(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);

        // Отправка статистики просмотров списка
        eventService.saveHitForEvents(events, request.getRemoteAddr());

        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto get(@PathVariable Long id, HttpServletRequest request) {
        EventFullDto event = eventService.getPublic(id);

        // Отправка статистики просмотра одного события
        eventService.saveHit(id, request.getRemoteAddr());

        return event;
    }
}