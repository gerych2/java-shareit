package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";
    private final String baseUrl;

    public BookingClient(@Value("${shareit.server.url}") String serverUrl, RestTemplate rest) {
        super(rest);
        this.baseUrl = serverUrl + API_PREFIX;
    }

    public ResponseEntity<Object> createBooking(Object bookingCreateDto, Long bookerId) {
        return post(baseUrl, bookerId, bookingCreateDto);
    }

    public ResponseEntity<Object> approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch(baseUrl + "/" + bookingId + "?approved={approved}", ownerId, new java.util.HashMap<>(), parameters);
    }

    public ResponseEntity<Object> getBookingById(Long bookingId, Long userId) {
        return get(baseUrl + "/" + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(String state, Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of("state", state, "from", from, "size", size);
        return get(baseUrl + "?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(String state, Long ownerId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of("state", state, "from", from, "size", size);
        return get(baseUrl + "/owner?state={state}&from={from}&size={size}", ownerId, parameters);
    }
}

