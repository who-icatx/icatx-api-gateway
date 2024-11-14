package edu.stanford.protege.gateway.postcoordination;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationSpecificationDto;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityPostCoordinationResponse;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PostCoordinationSpecificationMapperTest {
    private GetEntityPostCoordinationResponse response;

    private List<LinearizationDefinition> definitionList;

    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
        File specFile = new File("src/test/resources/dummyPostCoordinationResponse.json");
        response = objectMapper.readValue(specFile, GetEntityPostCoordinationResponse.class);

        File defFile = new File("src/test/resources/LinearizationDefinitions.json");
        definitionList = objectMapper.readValue(defFile, new TypeReference<>() {});
    }



    @Test
    public void GIVEN_response_WHEN_mappingToDto_THEN_isMappedCorrectly(){
        List<EntityPostCoordinationSpecificationDto> dtos = SpecificationMapper.mapFromResponse(response, definitionList);
        assertNotNull(dtos);
        assertEquals(2, dtos.size());

        Optional<EntityPostCoordinationSpecificationDto> nerSpec = getDtoFromLinId(dtos, "NER");
        assertTrue(nerSpec.isPresent());
        assertNotNull(nerSpec.get().overwrittenAllowedAxes());
        assertEquals(3, nerSpec.get().overwrittenAllowedAxes().size());

        Optional<EntityPostCoordinationSpecificationDto> mmsSpec = getDtoFromLinId(dtos, "MMS");
        assertTrue(mmsSpec.isPresent());
        assertNotNull(mmsSpec.get().notAllowedAxes());
        assertEquals(3, mmsSpec.get().notAllowedAxes().size());
    }


    private Optional<EntityPostCoordinationSpecificationDto> getDtoFromLinId(List<EntityPostCoordinationSpecificationDto> dtos, String linearizationId) {
        return dtos.stream().filter(dto -> dto.linearizationId().equalsIgnoreCase(linearizationId))
                .findFirst();
    }

}
