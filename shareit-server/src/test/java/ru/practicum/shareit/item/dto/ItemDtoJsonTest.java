package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testSerialize() throws Exception {
        // Given
        ItemDto dto = new ItemDto(1L, "Дрель", "Мощная дрель", true, 5L);

        // When
        JsonContent<ItemDto> result = json.write(dto);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Мощная дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    void testDeserialize() throws Exception {
        // Given
        String content = "{\"id\":1,\"name\":\"Дрель\",\"description\":\"Мощная дрель\"," +
                        "\"available\":true,\"requestId\":5}";

        // When
        ItemDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isEqualTo(5L);
    }

    @Test
    void testSerializeWithoutRequestId() throws Exception {
        // Given
        ItemDto dto = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);

        // When
        JsonContent<ItemDto> result = json.write(dto);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
    }

    @Test
    void testDeserializeWithoutRequestId() throws Exception {
        // Given
        String content = "{\"id\":1,\"name\":\"Дрель\",\"description\":\"Мощная дрель\"," +
                        "\"available\":true,\"requestId\":null}";

        // When
        ItemDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getRequestId()).isNull();
    }
}

