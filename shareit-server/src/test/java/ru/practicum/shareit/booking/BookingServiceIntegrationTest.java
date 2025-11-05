package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        owner = userService.createUser(new UserDto(null, "Owner", "owner@test.com"));
        booker = userService.createUser(new UserDto(null, "Booker", "booker@test.com"));
        item = itemService.createItem(
            new ItemDto(null, "Дрель", "Мощная дрель", true, null),
            owner.getId()
        );
    }

    @Test
    void createBooking_shouldCreateBookingSuccessfully() {
        // Given
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        // When
        BookingResponseDto result = bookingService.createBooking(bookingDto, booker.getId());

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("WAITING", result.getStatus());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
    }

    @Test
    void createBooking_shouldThrowExceptionWhenOwnerTriesToBook() {
        // Given
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        // When & Then
        assertThrows(NoSuchElementException.class, () ->
            bookingService.createBooking(bookingDto, owner.getId())
        );
    }

    @Test
    void approveBooking_shouldApproveBookingSuccessfully() {
        // Given
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(bookingDto, booker.getId());

        // When
        BookingResponseDto result = bookingService.approveBooking(created.getId(), true, owner.getId());

        // Then
        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    void approveBooking_shouldRejectBookingSuccessfully() {
        // Given
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(bookingDto, booker.getId());

        // When
        BookingResponseDto result = bookingService.approveBooking(created.getId(), false, owner.getId());

        // Then
        assertEquals("REJECTED", result.getStatus());
    }

    @Test
    void getBooking_shouldReturnBookingForOwner() {
        // Given
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(bookingDto, booker.getId());

        // When
        BookingResponseDto result = bookingService.getBookingById(created.getId(), owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getBooking_shouldReturnBookingForBooker() {
        // Given
        BookingCreateDto bookingDto = new BookingCreateDto(
            item.getId(),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );
        BookingResponseDto created = bookingService.createBooking(bookingDto, booker.getId());

        // When
        BookingResponseDto result = bookingService.getBookingById(created.getId(), booker.getId());

        // Then
        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
    }

    @Test
    void getUserBookings_shouldReturnAllBookings() {
        // Given
        bookingService.createBooking(
            new BookingCreateDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
            booker.getId()
        );

        // When
        List<BookingResponseDto> result = bookingService.getUserBookings("ALL", booker.getId(), 0, 10);

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_shouldReturnWaitingBookings() {
        // Given
        bookingService.createBooking(
            new BookingCreateDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
            booker.getId()
        );

        // When
        List<BookingResponseDto> result = bookingService.getUserBookings("WAITING", booker.getId(), 0, 10);

        // Then
        assertEquals(1, result.size());
        assertEquals("WAITING", result.get(0).getStatus());
    }

    @Test
    void getOwnerBookings_shouldReturnAllBookings() {
        // Given
        bookingService.createBooking(
            new BookingCreateDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
            booker.getId()
        );

        // When
        List<BookingResponseDto> result = bookingService.getOwnerBookings("ALL", owner.getId(), 0, 10);

        // Then
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_shouldReturnFutureBookings() {
        // Given
        bookingService.createBooking(
            new BookingCreateDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
            booker.getId()
        );

        // When
        List<BookingResponseDto> result = bookingService.getOwnerBookings("FUTURE", owner.getId(), 0, 10);

        // Then
        assertEquals(1, result.size());
    }
}

