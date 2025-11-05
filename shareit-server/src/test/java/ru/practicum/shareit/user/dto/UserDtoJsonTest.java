package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialize() throws Exception {
        // Given
        UserDto dto = new UserDto(1L, "John Doe", "john@test.com");

        // When
        JsonContent<UserDto> result = json.write(dto);

        // Then
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john@test.com");
    }

    @Test
    void testDeserialize() throws Exception {
        // Given
        String content = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@test.com\"}";

        // When
        UserDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void testDeserializeWithNullId() throws Exception {
        // Given
        String content = "{\"id\":null,\"name\":\"John Doe\",\"email\":\"john@test.com\"}";

        // When
        UserDto result = json.parse(content).getObject();

        // Then
        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@test.com");
    }
}

