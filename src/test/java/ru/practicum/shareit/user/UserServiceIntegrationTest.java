package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@test.com");

        // When
        UserDto result = userService.createUser(userDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@test.com", result.getEmail());
    }

    @Test
    void createUser_shouldThrowExceptionForDuplicateEmail() {
        // Given
        UserDto userDto1 = new UserDto(null, "John Doe", "john@test.com");
        UserDto userDto2 = new UserDto(null, "Jane Doe", "john@test.com");

        userService.createUser(userDto1);

        // When & Then
        assertThrows(ConflictException.class, () -> 
            userService.createUser(userDto2)
        );
    }

    @Test
    void updateUser_shouldUpdateUserSuccessfully() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@test.com");
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = new UserDto(null, "John Updated", null);

        // When
        UserDto result = userService.updateUser(updateDto, created.getId());

        // Then
        assertEquals("John Updated", result.getName());
        assertEquals("john@test.com", result.getEmail()); // Не изменился
    }

    @Test
    void updateUser_shouldUpdateEmailSuccessfully() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@test.com");
        UserDto created = userService.createUser(userDto);

        UserDto updateDto = new UserDto(null, null, "newemail@test.com");

        // When
        UserDto result = userService.updateUser(updateDto, created.getId());

        // Then
        assertEquals("John Doe", result.getName()); // Не изменился
        assertEquals("newemail@test.com", result.getEmail());
    }

    @Test
    void updateUser_shouldThrowExceptionForDuplicateEmail() {
        // Given
        UserDto user1 = userService.createUser(new UserDto(null, "User 1", "user1@test.com"));
        UserDto user2 = userService.createUser(new UserDto(null, "User 2", "user2@test.com"));

        UserDto updateDto = new UserDto(null, null, "user1@test.com");

        // When & Then
        assertThrows(ConflictException.class, () -> 
            userService.updateUser(updateDto, user2.getId())
        );
    }

    @Test
    void getUser_shouldReturnUser() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@test.com");
        UserDto created = userService.createUser(userDto);

        // When
        UserDto result = userService.getUser(created.getId());

        // Then
        assertEquals(created.getId(), result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@test.com", result.getEmail());
    }

    @Test
    void getUser_shouldThrowExceptionWhenNotFound() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        assertThrows(NoSuchElementException.class, () -> 
            userService.getUser(nonExistentId)
        );
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Given
        userService.createUser(new UserDto(null, "User 1", "user1@test.com"));
        userService.createUser(new UserDto(null, "User 2", "user2@test.com"));

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
    }

    @Test
    void deleteUser_shouldDeleteUserSuccessfully() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@test.com");
        UserDto created = userService.createUser(userDto);

        // When
        userService.deleteUser(created.getId());

        // Then
        assertThrows(NoSuchElementException.class, () -> 
            userService.getUser(created.getId())
        );
    }
}

