package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestCreateDto dto, Long userId) {
        log.info("Создание запроса вещи от пользователя с id={}", userId);
        
        // Проверяем, что пользователь существует
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id=" + userId + " не найден");
        }

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, userId);
        request = requestRepository.save(request);
        
        log.info("Запрос с id={} успешно создан", request.getId());
        return ItemRequestMapper.toItemRequestDto(request);
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        log.info("Получение списка запросов пользователя с id={}", userId);
        
        // Проверяем, что пользователь существует
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id=" + userId + " не найден");
        }

        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.info("Получение списка запросов других пользователей для пользователя с id={}", userId);
        
        // Проверяем, что пользователь существует
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id=" + userId + " не найден");
        }

        List<ItemRequest> requests = requestRepository.findByOtherUsers(userId);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.info("Получение запроса с id={} пользователем с id={}", requestId, userId);
        
        // Проверяем, что пользователь существует
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("Пользователь с id=" + userId + " не найден");
        }

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос с id=" + requestId + " не найден"));

        List<Item> items = itemRepository.findByRequestId(requestId);
        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private List<ItemRequestDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        
        Map<Long, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }
}

