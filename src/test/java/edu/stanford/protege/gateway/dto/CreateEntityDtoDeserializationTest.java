package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateEntityDtoDeserializationTest {

    @Test
    void testDeserialization() throws Exception {
        String json = """
            {
              "entityName": "api created entity",
              "entityParents": [
                "http://id.who.int/icd/entity/1384854244",
                "http://id.who.int/icd/entity/1105069515"
              ],
              "languageTag": "en"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();
        CreateEntityDto dto = objectMapper.readValue(json, CreateEntityDto.class);

        assertEquals("api created entity", dto.entityName());
        assertEquals(2, dto.entityParents().size());
        assertEquals("http://id.who.int/icd/entity/1384854244", dto.entityParents().get(0));
        assertEquals("http://id.who.int/icd/entity/1105069515", dto.entityParents().get(1));
        assertEquals("en", dto.languageTag());
    }
}