package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateEntityDtoDeserializationTest {

    @Test
    void testDeserialization() throws Exception {
        String json = """
                {
                  "title": "api created entity",
                  "parent": "http://id.who.int/icd/entity/1384854244",
                  "languageTag": "en"
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();
        CreateEntityDto dto = objectMapper.readValue(json, CreateEntityDto.class);

        assertEquals("api created entity", dto.title());
        assertEquals("http://id.who.int/icd/entity/1384854244", dto.parent());
        assertEquals("en", dto.languageTag());
    }
}