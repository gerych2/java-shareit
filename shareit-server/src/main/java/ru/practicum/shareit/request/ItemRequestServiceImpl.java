package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
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
        
        User requester = getUserById(userId);
        ItemRequest request = ItemRequestMapper.toEntity(dto);
        request.setRequester(requester);
        request = requestRepository.save(request);
        
        log.info("Запрос с id={} успешно создан", request.getId());
        ItemRequestDto dtoResult = ItemRequestMapper.toDto(request);
        dtoResult.setItems(List.of());
        return dtoResult;
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        log.info("Получение списка запросов пользователя с id={}", userId);
        
        getUserById(userId);
        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreateDateDesc(userId);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.info("Получение списка запросов других пользователей для пользователя с id={}", userId);
        
        getUserById(userId);
        List<ItemRequest> requests = requestRepository.findByOtherUsers(userId);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.info("Получение запроса с id={} пользователем с id={}", requestId, userId);
        
        getUserById(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос с id=" + requestId + " не найден"));

        List<Item> items = itemRepository.findByRequestId(requestId);
        ItemRequestDto dto = ItemRequestMapper.toDto(request);
        dto.setItems(ItemMapper.toShortDto(items));
        return dto;
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
                .filter(item -> item.getRequest() != null)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = ItemRequestMapper.toDto(request);
                    List<ItemShortDto> itemShortDtos = ItemMapper.toShortDto(
                            itemsByRequest.getOrDefault(request.getId(), List.of())
                    );
                    dto.setItems(itemShortDtos);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id=" + id + " не найден"));
    }
}


