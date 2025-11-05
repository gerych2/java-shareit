package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.comment.CommentService;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
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
    private final CommentService commentService;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        User owner = getUserById(ownerId);
        Item item = ItemMapper.toItem(itemDto, owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NoSuchElementException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
            item.setRequest(request);
        }
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = findItemById(itemId);

        if (!existingItem.getOwner().getId().equals(ownerId)) {
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
        Item item = findItemById(itemId);

        ItemWithBookingsDto result = new ItemWithBookingsDto();
        result.setId(item.getId());
        result.setName(item.getName());
        result.setDescription(item.getDescription());
        result.setAvailable(item.getAvailable());
        result.setOwnerId(item.getOwner().getId());

        // Добавляем комментарии
        List<CommentDto> comments = commentService.getCommentsByItemId(itemId);
        result.setComments(comments);

        // Если пользователь - владелец, добавляем информацию о бронированиях
        if (item.getOwner().getId().equals(userId)) {
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

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + id + " не найден"));
    }

    private Item findItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + id + " не найдена"));
    }
}

