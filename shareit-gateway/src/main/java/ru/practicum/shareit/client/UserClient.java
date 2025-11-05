package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";
    private final String baseUrl;

    public UserClient(@Value("${shareit.server.url}") String serverUrl, RestTemplate rest) {
        super(rest);
        this.baseUrl = serverUrl + API_PREFIX;
    }

    public ResponseEntity<Object> createUser(Object userDto) {
        return post(baseUrl, null, userDto);
    }

    public ResponseEntity<Object> updateUser(Long userId, Object userDto) {
        return patch(baseUrl + "/" + userId, userId, userDto);
    }

    public ResponseEntity<Object> getUserById(Long userId) {
        return get(baseUrl + "/" + userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get(baseUrl);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete(baseUrl + "/" + userId, null);
    }
}

