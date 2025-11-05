package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testSerialize() throws Exception {
        // Given
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0);
        BookingCreateDto dto = new BookingCreateDto(1L, start, end);

        // When
        JsonContent<BookingCreateDto> result = json.write(dto);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
            .isEqualTo(start.format(FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.end")
            .isEqualTo(end.format(FORMATTER));
    }

    @Test
    void testDeserialize() throws Exception {
        // Given
        String content = "{\"itemId\":1,\"start\":\"2024-01-01T10:00:00\",\"end\":\"2024-01-02T10:00:00\"}";

        // When
        BookingCreateDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));
    }

    @Test
    void testDeserializeWithNullItemId() throws Exception {
        // Given
        String content = "{\"itemId\":null,\"start\":\"2024-01-01T10:00:00\",\"end\":\"2024-01-02T10:00:00\"}";

        // When
        BookingCreateDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getItemId()).isNull();
    }
}

