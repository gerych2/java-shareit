package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit.server.url}") String serverUrl, RestTemplate rest) {
        super(
                rest == null ? new RestTemplate(new HttpComponentsClientHttpRequestFactory()) : rest
        );
        this.rest.setUriTemplateHandler(new org.springframework.web.util.DefaultUriBuilderFactory(serverUrl + API_PREFIX));
    }

    public ResponseEntity<Object> createUser(Object userDto) {
        return post("", null, userDto);
    }

    public ResponseEntity<Object> updateUser(Long userId, Object userDto) {
        return patch("/" + userId, null, userDto);
    }

    public ResponseEntity<Object> getUserById(Long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete("/" + userId, null);
    }
}

