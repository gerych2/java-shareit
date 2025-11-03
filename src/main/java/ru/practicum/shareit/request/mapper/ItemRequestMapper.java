package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestCreateDto dto, Long requesterId) {
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequesterId(requesterId);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        List<ItemRequestDto.ItemInfo> itemInfos = items.stream()
                .map(item -> new ItemRequestDto.ItemInfo(
                        item.getId(),
                        item.getName(),
                        item.getOwnerId()
                ))
                .collect(Collectors.toList());

        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                itemInfos
        );
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return toItemRequestDto(request, List.of());
    }
}

