package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto requester;
    private UserDto owner;

    @BeforeEach
    void setUp() {
        // Создаем пользователей для тестов
        requester = userService.createUser(new UserDto(null, "Requester", "requester@test.com"));
        owner = userService.createUser(new UserDto(null, "Owner", "owner@test.com"));
    }

    @Test
    void createRequest_shouldCreateRequestSuccessfully() {
        // Given
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");

        // When
        ItemRequestDto result = itemRequestService.createRequest(createDto, requester.getId());

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreateDate());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void createRequest_shouldThrowExceptionWhenUserNotFound() {
        // Given
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");
        Long nonExistentUserId = 999L;

        // When & Then
        assertThrows(NoSuchElementException.class, () -> 
            itemRequestService.createRequest(createDto, nonExistentUserId)
        );
    }

    @Test
    void getOwnRequests_shouldReturnUserRequests() throws InterruptedException {
        // Given
        itemRequestService.createRequest(new ItemRequestCreateDto("Нужна дрель"), requester.getId());
        Thread.sleep(10); // Небольшая задержка для различия в датах
        itemRequestService.createRequest(new ItemRequestCreateDto("Нужен молоток"), requester.getId());

        // When
        List<ItemRequestDto> result = itemRequestService.getOwnRequests(requester.getId());

        // Then
        assertEquals(2, result.size());
        // Проверяем сортировку (от новых к старым)
        assertEquals("Нужен молоток", result.get(0).getDescription());
        assertEquals("Нужна дрель", result.get(1).getDescription());
    }

    @Test
    void getOwnRequests_shouldReturnRequestsWithItems() {
        // Given
        ItemRequestDto request = itemRequestService.createRequest(
            new ItemRequestCreateDto("Нужна дрель"), 
            requester.getId()
        );

        // Создаем вещь в ответ на запрос
        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, request.getId());
        itemService.createItem(itemDto, owner.getId());

        // When
        List<ItemRequestDto> result = itemRequestService.getOwnRequests(requester.getId());

        // Then
        assertEquals(1, result.size());
        ItemRequestDto resultRequest = result.get(0);
        assertEquals(1, resultRequest.getItems().size());
        assertEquals("Дрель", resultRequest.getItems().get(0).getName());
        assertEquals(owner.getId(), resultRequest.getItems().get(0).getOwnerId());
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        // Given
        itemRequestService.createRequest(new ItemRequestCreateDto("Нужна дрель"), requester.getId());
        itemRequestService.createRequest(new ItemRequestCreateDto("Нужен молоток"), owner.getId());

        // When
        List<ItemRequestDto> result = itemRequestService.getAllRequests(requester.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals("Нужен молоток", result.get(0).getDescription());
    }

    @Test
    void getAllRequests_shouldNotReturnOwnRequests() {
        // Given
        itemRequestService.createRequest(new ItemRequestCreateDto("Нужна дрель"), requester.getId());
        itemRequestService.createRequest(new ItemRequestCreateDto("Нужен молоток"), requester.getId());

        // When
        List<ItemRequestDto> result = itemRequestService.getAllRequests(requester.getId());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        // Given
        ItemRequestDto request = itemRequestService.createRequest(
            new ItemRequestCreateDto("Нужна дрель"), 
            requester.getId()
        );

        ItemDto itemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, request.getId());
        itemService.createItem(itemDto, owner.getId());

        // When
        ItemRequestDto result = itemRequestService.getRequestById(request.getId(), owner.getId());

        // Then
        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Дрель", result.getItems().get(0).getName());
    }

    @Test
    void getRequestById_shouldThrowExceptionWhenRequestNotFound() {
        // Given
        Long nonExistentRequestId = 999L;

        // When & Then
        assertThrows(NoSuchElementException.class, () -> 
            itemRequestService.getRequestById(nonExistentRequestId, requester.getId())
        );
    }

    @Test
    void getRequestById_shouldThrowExceptionWhenUserNotFound() {
        // Given
        ItemRequestDto request = itemRequestService.createRequest(
            new ItemRequestCreateDto("Нужна дрель"), 
            requester.getId()
        );
        Long nonExistentUserId = 999L;

        // When & Then
        assertThrows(NoSuchElementException.class, () -> 
            itemRequestService.getRequestById(request.getId(), nonExistentUserId)
        );
    }
}

