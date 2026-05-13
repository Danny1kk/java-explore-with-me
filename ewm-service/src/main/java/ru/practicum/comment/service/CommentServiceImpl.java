package ru.practicum.comment.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.*;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.enums.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== PRIVATE API ====================

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено."));

        // Проверка, что событие опубликовано
        if (event.getState() != EventState.PUBLISHED) {
            throw new BadRequestException("Нельзя комментировать неопубликованное событие.");
        }

        // Проверка, что текст не пустой
        if (dto.getText() == null || dto.getText().isBlank()) {
            throw new BadRequestException("Текст комментария не может быть пустым.");
        }

        Comment comment = CommentMapper.returnComment(dto, user, event);
        return CommentMapper.returnCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден."));

        // Проверка, что пользователь — автор комментария
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Редактировать можно только свой комментарий.");
        }

        // Проверка, что текст не пустой
        if (dto.getText() == null || dto.getText().isBlank()) {
            throw new BadRequestException("Текст комментария не может быть пустым.");
        }

        comment.setText(dto.getText());
        // После редактирования статус сбрасывается на PENDING (если нужна модерация)
        comment.setStatus(CommentStatus.PENDING);

        return CommentMapper.returnCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deletePrivateComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден."));

        // Проверка, что пользователь — автор комментария
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Удалить можно только свой комментарий.");
        }

        commentRepository.delete(comment);
    }

    @Override
    public List<CommentShortDto> getCommentsByUserId(String rangeStart, String rangeEnd, Long userId,
                                                     Integer from, Integer size) {
        // Проверка существования пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));

        PageRequest pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageable);

        return comments.stream()
                .map(this::toCommentShortDto)
                .collect(Collectors.toList());
    }

    // ==================== PUBLIC API ====================

    @Override
    public List<CommentShortDto> getCommentsByEventId(String rangeStart, String rangeEnd, Long eventId,
                                                      Integer from, Integer size) {
        // Проверка существования события
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено."));

        PageRequest pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageable);

        // Публично видны только PUBLISHED комментарии
        return comments.stream()
                .filter(comment -> comment.getStatus() == CommentStatus.PUBLISHED)
                .map(this::toCommentShortDto)
                .collect(Collectors.toList());
    }

    // ==================== ADMIN API (заглушки для будущего этапа) ====================

    @Override
    public List<CommentDto> getComments(String rangeStart, String rangeEnd, Integer from, Integer size) {
        return List.of(); // Будет реализовано на этапе III
    }

    @Override
    @Transactional
    public void deleteAdminComment(Long commentId) {
        // Будет реализовано на этапе III
        throw new UnsupportedOperationException("Метод ещё не реализован.");
    }

    // ==================== Вспомогательные методы ====================

    private CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .userName(comment.getAuthor().getName())
                .eventTitle(comment.getEvent().getTitle())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateStr, FORMATTER);
    }
}