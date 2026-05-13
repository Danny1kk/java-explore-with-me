package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.service.CommentService;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.CommentShortDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/comments/{eventId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CommentDto addComment(@Valid @RequestBody NewCommentDto commentNewDto,
                                     @PathVariable Long userId,
                                     @PathVariable Long eventId) {

        return commentService.addComment(userId, eventId, commentNewDto);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CommentDto updateComment(@Valid @RequestBody NewCommentDto commentNewDto,
                                        @PathVariable Long userId,
                                        @PathVariable Long commentId) {

        return commentService.updateComment(userId, commentId, commentNewDto);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {

        commentService.deletePrivateComment(userId, commentId);
    }

    @GetMapping("/users/{userId}/comments")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentShortDto> getCommentsByUserId(@PathVariable Long userId,
                                                     @RequestParam(required = false, name = "rangeStart") String rangeStart,
                                                     @RequestParam(required = false, name = "rangeEnd") String rangeEnd,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return commentService.getCommentsByUserId(rangeStart, rangeEnd, userId, from, size);
    }

    @GetMapping("/comments/{eventId}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentShortDto> getCommentsByEventId(@PathVariable Long eventId,
                                                      @RequestParam(required = false, name = "rangeStart") String rangeStart,
                                                      @RequestParam(required = false, name = "rangeEnd") String rangeEnd,
                                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return commentService.getCommentsByEventId(rangeStart, rangeEnd, eventId, from, size);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {

        commentService.deleteAdminComment(commentId);
    }

    @GetMapping("/admin/comments")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentDto> getComments(@RequestParam(required = false, name = "rangeStart") String rangeStart,
                                            @RequestParam(required = false, name = "rangeEnd") String rangeEnd,
                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return commentService.getComments(rangeStart, rangeEnd, from, size);
    }
}