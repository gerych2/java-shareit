package ru.practicum.shareit.item.comment;

import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long itemId, CommentCreateDto commentCreateDto, Long userId);
    List<CommentDto> getCommentsByItemId(Long itemId);
}

