package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_shouldReturn200AndCreatedBooking() throws Exception {
        // Given
        BookingCreateDto createDto = new BookingCreateDto(
            1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2)
        );

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStart(createDto.getStart());
        responseDto.setEnd(createDto.getEnd());
        responseDto.setStatus("WAITING");
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Описание дрели");
        item.setAvailable(true);
        responseDto.setItem(item);
        UserDto booker = new UserDto();
        booker.setId(1L);
        booker.setName("John");
        booker.setEmail("john@example.com");
        responseDto.setBooker(booker);

        when(bookingService.createBooking(any(BookingCreateDto.class), anyLong()))
            .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/bookings")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking_shouldReturn200AndApprovedBooking() throws Exception {
        // Given
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus("APPROVED");

        when(bookingService.approveBooking(anyLong(), anyBoolean(), anyLong()))
            .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/bookings/1")
                .header("X-Sharer-User-Id", 1L)
                .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking_shouldReturn200AndBooking() throws Exception {
        // Given
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus("WAITING");

        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/bookings/1")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBooking_shouldReturn404WhenBookingNotFound() throws Exception {
        // Given
        when(bookingService.getBookingById(anyLong(), anyLong()))
            .thenThrow(new NoSuchElementException("Бронирование не найдено"));

        // When & Then
        mockMvc.perform(get("/bookings/999")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBookings_shouldReturn200AndListOfBookings() throws Exception {
        // Given
        BookingResponseDto booking = new BookingResponseDto();
        booking.setId(1L);
        booking.setStatus("WAITING");

        when(bookingService.getUserBookings(anyString(), anyLong(), anyInt(), anyInt()))
            .thenReturn(List.of(booking));

        // When & Then
        mockMvc.perform(get("/bookings")
                .header("X-Sharer-User-Id", 1L)
                .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUserBookings_shouldReturn400WhenStateIsInvalid() throws Exception {
        // Given
        when(bookingService.getUserBookings(anyString(), anyLong(), anyInt(), anyInt()))
            .thenThrow(new IllegalArgumentException("Unknown state: INVALID"));

        // When & Then
        mockMvc.perform(get("/bookings")
                .header("X-Sharer-User-Id", 1L)
                .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnerBookings_shouldReturn200AndListOfBookings() throws Exception {
        // Given
        BookingResponseDto booking = new BookingResponseDto();
        booking.setId(1L);
        booking.setStatus("WAITING");

        when(bookingService.getOwnerBookings(anyString(), anyLong(), anyInt(), anyInt()))
            .thenReturn(List.of(booking));

        // When & Then
        mockMvc.perform(get("/bookings/owner")
                .header("X-Sharer-User-Id", 1L)
                .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}

