package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // ==================== Вспомогательные методы маппинга ====================

    private EventFullDto toFullDto(Event event) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(new ru.practicum.category.dto.CategoryDto() {{
            setId(event.getCategory().getId());
            setName(event.getCategory().getName());
        }});
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(toUserShort(event.getInitiator()));
        dto.setLocation(event.getLocation());
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState().name());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews() != null ? event.getViews() : 0);
        return dto;
    }

    private EventShortDto toShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(new ru.practicum.category.dto.CategoryDto() {{
            setId(event.getCategory().getId());
            setName(event.getCategory().getName());
        }});
        dto.setConfirmedRequests(event.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(toUserShort(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews() != null ? event.getViews() : 0);
        return dto;
    }

    private UserShortDto toUserShort(User user) {
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }

    // ==================== PRIVATE API (для пользователей) ====================

    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        // Проверка: дата события не ранее, чем за 2 часа от текущего момента
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена"));

        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategory(category);
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setLocation(dto.getLocation());
        event.setPaid(dto.getPaid() != null ? dto.getPaid() : false);
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);
        event.setTitle(dto.getTitle());
        event.setInitiator(initiator);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        return toFullDto(eventRepository.save(event));
    }

    public List<EventShortDto> getByUser(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable)
                .stream()
                .map(this::toShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getByUserAndEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено у пользователя с id=" + userId);
        }
        return toFullDto(event);
    }

    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено у пользователя с id=" + userId);
        }

        // Можно изменять только отменённые или ожидающие модерацию события
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Редактировать можно только отменённые или ожидающие модерацию события");
        }

        // Обновление полей, если они не null
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());

        // Обработка изменения состояния
        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals("SEND_TO_REVIEW")) {
                event.setState(EventState.PENDING);
            } else if (dto.getStateAction().equals("CANCEL_REVIEW")) {
                event.setState(EventState.CANCELED);
            }
        }

        return toFullDto(eventRepository.save(event));
    }

    // ==================== PUBLIC API ====================

    public List<EventShortDto> searchPublic(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                            Boolean onlyAvailable, String sort,
                                            int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        // Если диапазон дат не указан, берём события, которые произойдут позже текущего момента
        if (rangeStart == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);

        // Валидация дат
        if (rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Дата начала диапазона не может быть позже даты конца");
        }

        return eventRepository.searchPublic(text, categories, paid, rangeStart, rangeEnd, pageable)
                .stream()
                .map(this::toShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto getPublic(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        // Публично доступны только опубликованные события
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }

        return toFullDto(event);
    }

    // ==================== ADMIN API ====================

    public List<EventFullDto> searchAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                          int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        if (rangeStart == null) rangeStart = LocalDateTime.now().minusYears(100);
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(100);

        return eventRepository.searchAdmin(users, states, categories, rangeStart, rangeEnd, pageable)
                .stream()
                .map(this::toFullDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        // Обновление полей, если они не null
        if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена"));
            event.setCategory(category);
        }
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getEventDate() != null) {
            // При публикации дата события должна быть не ранее чем за 1 час
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата начала события должна быть не ранее чем за час от даты публикации");
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getPaid() != null) event.setPaid(dto.getPaid());
        if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit());
        if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
        if (dto.getTitle() != null) event.setTitle(dto.getTitle());

        // Обработка изменения состояния (публикация/отклонение)
        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals("PUBLISH_EVENT")) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Опубликовать можно только событие в статусе PENDING");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (dto.getStateAction().equals("REJECT_EVENT")) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            }
        }

        return toFullDto(eventRepository.save(event));
    }
}