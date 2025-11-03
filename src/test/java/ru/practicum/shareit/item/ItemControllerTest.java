package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createItem_shouldReturn200AndCreatedItem() throws Exception {
        // Given
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);
        ItemDto responseDto = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);

        when(itemService.createItem(any(ItemDto.class), anyLong())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/items")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Мощная дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createItem_shouldReturn400WhenNameIsBlank() throws Exception {
        // Given
        ItemDto itemDto = new ItemDto(null, "", "Мощная дрель", true, null);

        // When & Then
        mockMvc.perform(post("/items")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_shouldReturn200AndUpdatedItem() throws Exception {
        // Given
        ItemDto updateDto = new ItemDto(null, "Дрель обновленная", null, null, null);
        ItemDto responseDto = new ItemDto(1L, "Дрель обновленная", "Мощная дрель", true, null);

        when(itemService.updateItem(any(ItemDto.class), anyLong(), anyLong())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/items/1")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель обновленная"));
    }

    @Test
    void getItem_shouldReturn200AndItem() throws Exception {
        // Given
        ItemWithBookingsDto itemDto = new ItemWithBookingsDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        when(itemService.getItem(anyLong(), anyLong())).thenReturn(itemDto);

        // When & Then
        mockMvc.perform(get("/items/1")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getItem_shouldReturn404WhenItemNotFound() throws Exception {
        // Given
        when(itemService.getItem(anyLong(), anyLong()))
            .thenThrow(new NoSuchElementException("Вещь не найдена"));

        // When & Then
        mockMvc.perform(get("/items/999")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserItems_shouldReturn200AndListOfItems() throws Exception {
        // Given
        ItemWithBookingsDto item1 = new ItemWithBookingsDto();
        item1.setId(1L);
        item1.setName("Дрель");

        ItemWithBookingsDto item2 = new ItemWithBookingsDto();
        item2.setId(2L);
        item2.setName("Молоток");

        when(itemService.getUserItems(anyLong())).thenReturn(List.of(item1, item2));

        // When & Then
        mockMvc.perform(get("/items")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].name").value("Молоток"));
    }

    @Test
    void searchItems_shouldReturn200AndListOfItems() throws Exception {
        // Given
        ItemDto item = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);
        when(itemService.searchItems(anyString(), anyLong())).thenReturn(List.of(item));

        // When & Then
        mockMvc.perform(get("/items/search")
                .header("X-Sharer-User-Id", 1L)
                .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addComment_shouldReturn200AndCreatedComment() throws Exception {
        // Given
        CommentCreateDto commentDto = new CommentCreateDto("Отличная вещь!");
        CommentDto responseDto = new CommentDto(1L, "Отличная вещь!", "John", LocalDateTime.now());

        when(itemService.addComment(any(CommentCreateDto.class), anyLong(), anyLong()))
            .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/items/1/comment")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"))
                .andExpect(jsonPath("$.authorName").value("John"));
    }

    @Test
    void addComment_shouldReturn400WhenTextIsBlank() throws Exception {
        // Given
        CommentCreateDto commentDto = new CommentCreateDto("");

        // When & Then
        mockMvc.perform(post("/items/1/comment")
                .header("X-Sharer-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem_shouldReturn200() throws Exception {
        // When & Then
        mockMvc.perform(delete("/items/1")
                .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}

