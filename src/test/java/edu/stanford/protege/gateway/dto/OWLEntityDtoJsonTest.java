package edu.stanford.protege.gateway.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;


class OWLEntityDtoJsonTest {

    private ObjectMapper mapper;
    private File jsonFile;

    @BeforeEach
    void setUp() {
        mapper   = new ApplicationBeans().objectMapper();
        jsonFile = new File("src/test/resources/dummyOwlEntityDto.json");
    }

    @Test
    void GIVEN_sampleJson_WHEN_deserialize_THEN_fieldsAreMappedCorrectly() throws Exception {
        OWLEntityDto dto = mapper.readValue(jsonFile, OWLEntityDto.class);

        assertNotNull(dto);
        assertEquals("http://id.who.int/icd/entity/1855860109", dto.entityIRI());
        assertNotNull(dto.lastChangeDate());
        assertEquals("Classical sporadic Creutzfeldt-Jakob Disease", dto.languageTerms().title().label());
        assertFalse(dto.entityLinearizations().linearizations().isEmpty());
    }

    @Test
    void GIVEN_dto_WHEN_serializeAndDeserialize_THEN_roundTripPreservesData() throws Exception {
        OWLEntityDto original = mapper.readValue(jsonFile, OWLEntityDto.class);

        String json = mapper.writeValueAsString(original);
        OWLEntityDto roundTrip = mapper.readValue(json, OWLEntityDto.class);

        assertEquals(original, roundTrip, "DTO should remain equal after JSON round‑trip");
    }
}
