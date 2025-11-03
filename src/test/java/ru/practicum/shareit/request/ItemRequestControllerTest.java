package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest_shouldReturn200AndCreatedRequest() throws Exception {
        // Given
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");
        ItemRequestDto responseDto = new ItemRequestDto(
            1L, 
            "Нужна дрель", 
            LocalDateTime.now(), 
            List.of()
        );

        when(itemRequestService.createRequest(any(ItemRequestCreateDto.class), anyLong()))
            .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/requests")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void createRequest_shouldReturn400WhenDescriptionIsBlank() throws Exception {
        // Given
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("");

        // When & Then
        mockMvc.perform(post("/requests")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");

        when(itemRequestService.createRequest(any(ItemRequestCreateDto.class), anyLong()))
            .thenThrow(new NoSuchElementException("Пользователь не найден"));

        // When & Then
        mockMvc.perform(post("/requests")
                .header("X-Sharer-User-Id", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnRequests_shouldReturn200AndListOfRequests() throws Exception {
        // Given
        List<ItemRequestDto> requests = List.of(
            new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of()),
            new ItemRequestDto(2L, "Нужен молоток", LocalDateTime.now(), List.of())
        );

        when(itemRequestService.getOwnRequests(anyLong())).thenReturn(requests);

        // When & Then
        mockMvc.perform(get("/requests")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"))
                .andExpect(jsonPath("$[1].description").value("Нужен молоток"));
    }

    @Test
    void getAllRequests_shouldReturn200AndListOfRequests() throws Exception {
        // Given
        List<ItemRequestDto> requests = List.of(
            new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of())
        );

        when(itemRequestService.getAllRequests(anyLong())).thenReturn(requests);

        // When & Then
        mockMvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"));
    }

    @Test
    void getRequestById_shouldReturn200AndRequest() throws Exception {
        // Given
        ItemRequestDto requestDto = new ItemRequestDto(
            1L, 
            "Нужна дрель", 
            LocalDateTime.now(), 
            List.of()
        );

        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(requestDto);

        // When & Then
        mockMvc.perform(get("/requests/1")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void getRequestById_shouldReturn404WhenRequestNotFound() throws Exception {
        // Given
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
            .thenThrow(new NoSuchElementException("Запрос не найден"));

        // When & Then
        mockMvc.perform(get("/requests/999")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}

