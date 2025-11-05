package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";
    private final String baseUrl;

    public ItemRequestClient(@Value("${shareit.server.url}") String serverUrl, RestTemplate rest) {
        super(rest);
        this.baseUrl = serverUrl + API_PREFIX;
    }

    public ResponseEntity<Object> createRequest(Object itemRequestCreateDto, Long userId) {
        return post(baseUrl, userId, itemRequestCreateDto);
    }

    public ResponseEntity<Object> getOwnRequests(Long userId) {
        return get(baseUrl, userId);
    }

    public ResponseEntity<Object> getAllRequests(Long userId) {
        return get(baseUrl + "/all", userId);
    }

    public ResponseEntity<Object> getRequestById(Long requestId, Long userId) {
        return get(baseUrl + "/" + requestId, userId);
    }
}

