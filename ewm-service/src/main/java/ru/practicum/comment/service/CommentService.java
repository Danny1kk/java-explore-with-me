package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentShortDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto commentNewDto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentNewDto);

    void deletePrivateComment(Long userId, Long commentId);

    List<CommentShortDto> getCommentsByUserId(String rangeStart, String rangeEnd, Long userId, Integer from, Integer size);

    List<CommentDto> getComments(String rangeStart, String rangeEnd, Integer from, Integer size);

    void deleteAdminComment(Long commentId);

    List<CommentShortDto> getCommentsByEventId(String rangeStart, String rangeEnd, Long eventId, Integer from, Integer size);
}