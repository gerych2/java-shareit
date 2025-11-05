package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingCreateDto bookingCreateDto, Long bookerId);
    BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId);
    BookingResponseDto getBookingById(Long bookingId, Long userId);
    List<BookingResponseDto> getUserBookings(String state, Long userId, Integer from, Integer size);
    List<BookingResponseDto> getOwnerBookings(String state, Long ownerId, Integer from, Integer size);
}

