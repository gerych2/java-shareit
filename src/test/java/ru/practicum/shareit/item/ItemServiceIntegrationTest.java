package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    private UserDto owner;
    private UserDto booker;

    @BeforeEach
    void setUp() {
        owner = userService.createUser(new UserDto(null, "Owner", "owner@test.com"));
        booker = userService.createUser(new UserDto(null, "Booker", "booker@test.com"));
    }

    @Test
    void getUserItems_shouldReturnItemsWithBookings() {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);
        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        // Создаем бронирование
        BookingCreateDto bookingDto = new BookingCreateDto(
            createdItem.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        bookingService.createBooking(bookingDto, booker.getId());

        // When
        List<ItemWithBookingsDto> result = itemService.getUserItems(owner.getId());

        // Then
        assertEquals(1, result.size());
        ItemWithBookingsDto item = result.get(0);
        assertEquals("Дрель", item.getName());
        assertNotNull(item.getNextBooking());
    }

    @Test
    void getUserItems_shouldReturnEmptyListWhenUserHasNoItems() {
        // When
        List<ItemWithBookingsDto> result = itemService.getUserItems(owner.getId());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);

        // When
        ItemDto result = itemService.createItem(itemDto, owner.getId());

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Дрель", result.getName());
        assertEquals("Мощная дрель", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void createItem_shouldCreateItemWithRequestId() {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, 1L);

        // When
        ItemDto result = itemService.createItem(itemDto, owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRequestId());
    }

    @Test
    void updateItem_shouldUpdateItemSuccessfully() {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);
        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        ItemDto updateDto = new ItemDto(null, "Дрель обновленная", null, null, null);

        // When
        ItemDto result = itemService.updateItem(updateDto, createdItem.getId(), owner.getId());

        // Then
        assertEquals("Дрель обновленная", result.getName());
        assertEquals("Мощная дрель", result.getDescription()); // Не изменилось
        assertTrue(result.getAvailable()); // Не изменилось
    }

    @Test
    void getItem_shouldReturnItemWithBookings() {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);
        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        // When
        ItemWithBookingsDto result = itemService.getItem(createdItem.getId(), owner.getId());

        // Then
        assertNotNull(result);
        assertEquals("Дрель", result.getName());
    }

    @Test
    void searchItems_shouldReturnMatchingItems() {
        // Given
        itemService.createItem(new ItemDto(null, "Дрель", "Мощная дрель", true, null), owner.getId());
        itemService.createItem(new ItemDto(null, "Молоток", "Тяжелый молоток", true, null), owner.getId());

        // When
        List<ItemDto> result = itemService.searchItems("дрель", booker.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals("Дрель", result.get(0).getName());
    }

    @Test
    void searchItems_shouldReturnEmptyListForEmptyQuery() {
        // Given
        itemService.createItem(new ItemDto(null, "Дрель", "Мощная дрель", true, null), owner.getId());

        // When
        List<ItemDto> result = itemService.searchItems("", booker.getId());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_shouldAddCommentSuccessfully() throws InterruptedException {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);
        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        // Создаем бронирование в прошлом
        BookingCreateDto bookingDto = new BookingCreateDto(
            createdItem.getId(),
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusDays(1)
        );
        bookingService.createBooking(bookingDto, booker.getId());
        
        // Одобряем бронирование
        Long bookingId = bookingService.getUserBookings(booker.getId(), "ALL").get(0).getId();
        bookingService.approveBooking(bookingId, owner.getId(), true);

        // Wait a bit to ensure booking is in the past
        Thread.sleep(100);

        CommentCreateDto commentDto = new CommentCreateDto("Отличная дрель!");

        // When
        CommentDto result = itemService.addComment(commentDto, createdItem.getId(), booker.getId());

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Отличная дрель!", result.getText());
        assertEquals("Booker", result.getAuthorName());
        assertNotNull(result.getCreated());
    }

    @Test
    void deleteItem_shouldDeleteItemSuccessfully() {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);
        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        // When
        itemService.deleteItem(createdItem.getId(), owner.getId());

        // Then
        assertThrows(NoSuchElementException.class, () -> 
            itemService.getItem(createdItem.getId(), owner.getId())
        );
    }
}

