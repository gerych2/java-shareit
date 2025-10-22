package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@Valid @RequestBody BookingCreateDto bookingCreateDto,
                                           @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return bookingService.createBooking(bookingCreateDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@PathVariable Long bookingId,
                                            @RequestParam Boolean approved,
                                            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId,
                                            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(@RequestParam(defaultValue = "ALL") String state,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                    @Positive @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.getUserBookings(state, userId, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestParam(defaultValue = "ALL") String state,
                                                     @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                     @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.getOwnerBookings(state, ownerId, from, size);
    }
}