package ru.practicum.shareit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return get(path, null, null);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return get(path, userId, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Object> shareitServerResponse;
        try {
            if (parameters != null) {
                shareitServerResponse = rest.exchange(path, HttpMethod.GET, requestEntity, Object.class, parameters);
            } else {
                shareitServerResponse = rest.exchange(path, HttpMethod.GET, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(parseErrorResponse(e));
        }
        return prepareGatewayResponse(shareitServerResponse);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> shareitServerResponse;
        try {
            shareitServerResponse = rest.exchange(path, HttpMethod.POST, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(parseErrorResponse(e));
        }
        return prepareGatewayResponse(shareitServerResponse);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId, T body) {
        return patch(path, userId, body, null);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId, T body, @Nullable Map<String, Object> parameters) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> shareitServerResponse;
        try {
            if (parameters != null) {
                shareitServerResponse = rest.exchange(path, HttpMethod.PATCH, requestEntity, Object.class, parameters);
            } else {
                shareitServerResponse = rest.exchange(path, HttpMethod.PATCH, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(parseErrorResponse(e));
        }
        return prepareGatewayResponse(shareitServerResponse);
    }

    protected ResponseEntity<Object> delete(String path, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Object> shareitServerResponse;
        try {
            shareitServerResponse = rest.exchange(path, HttpMethod.DELETE, requestEntity, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(parseErrorResponse(e));
        }
        return prepareGatewayResponse(shareitServerResponse);
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }

    private Object parseErrorResponse(HttpStatusCodeException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isEmpty()) {
                return objectMapper.readValue(responseBody, Object.class);
            }
        } catch (Exception ignored) {
            // Если не удалось распарсить JSON, возвращаем как строку
        }
        return e.getResponseBodyAsString();
    }
}

