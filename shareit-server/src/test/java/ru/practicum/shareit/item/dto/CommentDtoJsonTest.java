package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testSerialize() throws Exception {
        // Given
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        CommentDto dto = new CommentDto(1L, "Отличная вещь!", "John Doe", created);

        // When
        JsonContent<CommentDto> result = json.write(dto);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличная вещь!");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.created")
            .isEqualTo(created.format(FORMATTER));
    }

    @Test
    void testDeserialize() throws Exception {
        // Given
        String content = "{\"id\":1,\"text\":\"Отличная вещь!\",\"authorName\":\"John Doe\"," +
                        "\"created\":\"2024-01-01T10:00:00\"}";

        // When
        CommentDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Отличная вещь!");
        assertThat(result.getAuthorName()).isEqualTo("John Doe");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }
}

