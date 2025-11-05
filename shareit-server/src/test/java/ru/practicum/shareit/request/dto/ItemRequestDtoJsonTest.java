package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testSerialize() throws Exception {
        // Given
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        ru.practicum.shareit.item.dto.ItemShortDto itemInfo = new ru.practicum.shareit.item.dto.ItemShortDto(1L, "Дрель", 2L);
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель", created, List.of(itemInfo));

        // When
        JsonContent<ItemRequestDto> result = json.write(dto);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(result).extractingJsonPathStringValue("$.created")
            .isEqualTo(created.format(FORMATTER));
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
    }

    @Test
    void testDeserialize() throws Exception {
        // Given
        String content = "{\"id\":1,\"description\":\"Нужна дрель\",\"created\":\"2024-01-01T10:00:00\"," +
                        "\"items\":[{\"id\":1,\"name\":\"Дрель\",\"ownerId\":2}]}";

        // When
        ItemRequestDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(result.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }

    @Test
    void testSerializeWithEmptyItems() throws Exception {
        // Given
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель", created, List.of());

        // When
        JsonContent<ItemRequestDto> result = json.write(dto);

        // Then
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }
}

