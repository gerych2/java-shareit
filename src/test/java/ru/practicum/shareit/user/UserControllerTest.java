package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturn200AndCreatedUser() throws Exception {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@test.com");
        UserDto responseDto = new UserDto(1L, "John Doe", "john@test.com");

        when(userService.createUser(any(UserDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void createUser_shouldReturn400WhenNameIsBlank() throws Exception {
        // Given
        UserDto userDto = new UserDto(null, "", "john@test.com");

        // When & Then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400WhenEmailIsInvalid() throws Exception {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "invalid-email");

        // When & Then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn409WhenEmailAlreadyExists() throws Exception {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@test.com");

        when(userService.createUser(any(UserDto.class)))
            .thenThrow(new ConflictException("Email уже используется"));

        // When & Then
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_shouldReturn200AndUpdatedUser() throws Exception {
        // Given
        UserDto updateDto = new UserDto(null, "John Updated", null);
        UserDto responseDto = new UserDto(1L, "John Updated", "john@test.com");

        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
    }

    @Test
    void getUser_shouldReturn200AndUser() throws Exception {
        // Given
        UserDto userDto = new UserDto(1L, "John Doe", "john@test.com");

        when(userService.getUserById(anyLong())).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void getUser_shouldReturn404WhenUserNotFound() throws Exception {
        // Given
        when(userService.getUserById(anyLong()))
            .thenThrow(new NoSuchElementException("Пользователь не найден"));

        // When & Then
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_shouldReturn200AndListOfUsers() throws Exception {
        // Given
        List<UserDto> users = List.of(
            new UserDto(1L, "User 1", "user1@test.com"),
            new UserDto(2L, "User 2", "user2@test.com")
        );

        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("User 1"))
                .andExpect(jsonPath("$[1].name").value("User 2"));
    }

    @Test
    void deleteUser_shouldReturn200() throws Exception {
        // When & Then
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}

