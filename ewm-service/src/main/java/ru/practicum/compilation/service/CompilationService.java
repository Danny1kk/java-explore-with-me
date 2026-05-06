package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserShortDto;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(Boolean.TRUE.equals(dto.getPinned()));
        compilation.setEvents(resolveEvents(dto.getEvents()));

        Compilation savedCompilation = compilationRepository.save(compilation);
        return toDto(getDetailedCompilation(savedCompilation.getId()));
    }

    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (dto.getEvents() != null) {
            compilation.setEvents(resolveEvents(dto.getEvents()));
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }

        compilationRepository.save(compilation);
        return toDto(getDetailedCompilation(compilation.getId()));
    }

    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Long> compilationIds = compilationRepository.findIdsByPinned(pinned, PageRequest.of(from / size, size))
                .getContent();
        if (compilationIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> orderById = new LinkedHashMap<>();
        for (int index = 0; index < compilationIds.size(); index++) {
            orderById.put(compilationIds.get(index), index);
        }

        return compilationRepository.findAllDetailedByIdIn(compilationIds).stream()
                .sorted(Comparator.comparingInt(compilation -> orderById.getOrDefault(compilation.getId(), Integer.MAX_VALUE)))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CompilationDto getCompilation(Long compId) {
        return toDto(getDetailedCompilation(compId));
    }

    private Compilation getDetailedCompilation(Long compId) {
        return compilationRepository.findDetailedById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private Set<Event> resolveEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            Set<Long> foundIds = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            Long missingEventId = eventIds.stream()
                    .filter(eventId -> !foundIds.contains(eventId))
                    .findFirst()
                    .orElse(null);
            throw new NotFoundException("Event with id=" + missingEventId + " was not found");
        }

        return events.stream()
                .sorted(Comparator.comparing(Event::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private CompilationDto toDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.getPinned());
        dto.setTitle(compilation.getTitle());
        dto.setEvents(compilation.getEvents().stream()
                .sorted(Comparator.comparing(Event::getId))
                .map(this::toShortDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private EventShortDto toShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(toUserShort(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews() != null ? event.getViews() : 0);
        return dto;
    }

    private UserShortDto toUserShort(ru.practicum.user.model.User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }

    private CategoryDto toCategoryDto(ru.practicum.category.model.Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
