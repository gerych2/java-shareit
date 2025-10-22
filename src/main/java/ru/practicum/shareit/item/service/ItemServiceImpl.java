package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + ownerId + " не найден"));

        Item item = ItemMapper.toItem(itemDto, ownerId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new NoSuchElementException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        ItemWithBookingsDto result = new ItemWithBookingsDto();
        result.setId(item.getId());
        result.setName(item.getName());
        result.setDescription(item.getDescription());
        result.setAvailable(item.getAvailable());
        result.setOwnerId(item.getOwnerId());

        // Добавляем комментарии
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        result.setComments(ItemMapper.toCommentDto(comments));

        // Если пользователь - владелец, добавляем информацию о бронированиях
        if (item.getOwnerId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            // Последнее бронирование
            List<ru.practicum.shareit.booking.Booking> lastBookings = bookingRepository.findPastByItemOwnerIdOrderByStartDesc(itemId, now, org.springframework.data.domain.PageRequest.of(0, 1));
            if (!lastBookings.isEmpty()) {
                ru.practicum.shareit.booking.Booking lastBooking = lastBookings.get(0);
                ItemWithBookingsDto.BookingInfo lastBookingInfo = new ItemWithBookingsDto.BookingInfo();
                lastBookingInfo.setId(lastBooking.getId());
                lastBookingInfo.setBookerId(lastBooking.getBooker().getId());
                lastBookingInfo.setStart(lastBooking.getStart());
                lastBookingInfo.setEnd(lastBooking.getEnd());
                result.setLastBooking(lastBookingInfo);
            }

            // Следующее бронирование
            List<ru.practicum.shareit.booking.Booking> nextBookings = bookingRepository.findFutureByItemOwnerIdOrderByStartDesc(itemId, now, org.springframework.data.domain.PageRequest.of(0, 1));
            if (!nextBookings.isEmpty()) {
                ru.practicum.shareit.booking.Booking nextBooking = nextBookings.get(0);
                ItemWithBookingsDto.BookingInfo nextBookingInfo = new ItemWithBookingsDto.BookingInfo();
                nextBookingInfo.setId(nextBooking.getId());
                nextBookingInfo.setBookerId(nextBooking.getBooker().getId());
                nextBookingInfo.setStart(nextBooking.getStart());
                nextBookingInfo.setEnd(nextBooking.getEnd());
                result.setNextBooking(nextBookingInfo);
            }
        }

        return result;
    }

    @Override
    public List<ItemWithBookingsDto> getItemsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                .map(item -> getItemById(item.getId(), ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<Item> allItems = itemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                         item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long itemId, CommentCreateDto commentCreateDto, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));

        // Проверяем, что пользователь бронировал эту вещь и бронирование завершено
        List<ru.practicum.shareit.booking.Booking> pastBookings = bookingRepository.findPastApprovedBookingsByItemIdAndBookerId(itemId, userId, LocalDateTime.now());
        if (pastBookings.isEmpty()) {
            throw new IllegalArgumentException("Пользователь не может оставить комментарий к вещи, которую не бронировал");
        }

        Comment comment = new Comment();
        comment.setText(commentCreateDto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return ItemMapper.toCommentDto(savedComment);
    }
}
