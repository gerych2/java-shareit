package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto createBooking(BookingCreateDto bookingCreateDto, Long bookerId) {
        User booker = getUserById(bookerId);
        Item item = getItemById(bookingCreateDto.getItemId());

        if (item.getOwnerId().equals(bookerId)) {
            throw new NoSuchElementException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (bookingCreateDto.getStart().isAfter(bookingCreateDto.getEnd()) ||
            bookingCreateDto.getStart().isEqual(bookingCreateDto.getEnd())) {
            throw new IllegalArgumentException("Дата начала бронирования не может быть позже или равна дате окончания");
        }

        Booking booking = BookingMapper.toBooking(bookingCreateDto, bookerId);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может подтверждать бронирование");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new IllegalArgumentException("Бронирование уже имеет статус " + booking.getStatus());
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwnerId().equals(userId)) {
            throw new NoSuchElementException("Пользователь не является ни автором бронирования, ни владельцем вещи");
        }
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(String state, Long userId, Integer from, Integer size) {
        getUserById(userId);

        if (size <= 0) {
            throw new IllegalArgumentException("Размер страницы должен быть больше 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now, pageable);
                    break;
                case PAST:
                    bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                    break;
                case WAITING:
                    bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, pageable);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, pageable);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown state: " + state);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
        return BookingMapper.toResponseDto(bookings);
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(String state, Long ownerId, Integer from, Integer size) {
        getUserById(ownerId);

        if (size <= 0) {
            throw new IllegalArgumentException("Размер страницы должен быть больше 0");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        try {
            switch (BookingState.valueOf(state)) {
                case ALL:
                    bookings = bookingRepository.findByItem_OwnerIdOrderByStartDesc(ownerId, pageable);
                    break;
                case CURRENT:
                    bookings = bookingRepository.findByItem_OwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now, pageable);
                    break;
                case PAST:
                    bookings = bookingRepository.findByItem_OwnerIdAndEndBeforeOrderByStartDesc(ownerId, now, pageable);
                    break;
                case FUTURE:
                    bookings = bookingRepository.findByItem_OwnerIdAndStartAfterOrderByStartDesc(ownerId, now, pageable);
                    break;
                case WAITING:
                    bookings = bookingRepository.findByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING, pageable);
                    break;
                case REJECTED:
                    bookings = bookingRepository.findByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED, pageable);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown state: " + state);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
        return BookingMapper.toResponseDto(bookings);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с ID " + itemId + " не найдена"));
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с ID " + bookingId + " не найдено"));
    }
}
