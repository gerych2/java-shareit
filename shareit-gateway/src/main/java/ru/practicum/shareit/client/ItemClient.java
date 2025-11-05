package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";
    private final String baseUrl;

    public ItemClient(@Value("${shareit.server.url}") String serverUrl, RestTemplate rest) {
        super(rest);
        this.baseUrl = serverUrl + API_PREFIX;
    }

    public ResponseEntity<Object> createItem(Object itemDto, Long ownerId) {
        return post(baseUrl, ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long itemId, Object itemDto, Long ownerId) {
        return patch(baseUrl + "/" + itemId, ownerId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long userId) {
        return get(baseUrl + "/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByOwner(Long ownerId) {
        return get(baseUrl, ownerId);
    }

    public ResponseEntity<Object> searchItems(String text) {
        Map<String, Object> parameters = Map.of("text", text);
        return get(baseUrl + "/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> addComment(Long itemId, Object commentCreateDto, Long userId) {
        return post(baseUrl + "/" + itemId + "/comment", userId, commentCreateDto);
    }

    public ResponseEntity<Object> deleteItem(Long itemId, Long userId) {
        return delete(baseUrl + "/" + itemId, userId);
    }
}

