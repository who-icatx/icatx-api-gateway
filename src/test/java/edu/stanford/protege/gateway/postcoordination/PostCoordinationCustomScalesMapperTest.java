package edu.stanford.protege.gateway.postcoordination;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationCustomScalesDto;
import edu.stanford.protege.gateway.dto.EntityPostCoordinationSpecificationDto;
import edu.stanford.protege.gateway.linearization.commands.LinearizationDefinition;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValueResponse;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityCustomScaleValuesRequest;
import edu.stanford.protege.gateway.postcoordination.commands.GetEntityPostCoordinationResponse;
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
public class PostCoordinationCustomScalesMapperTest {

    private GetEntityCustomScaleValueResponse response;

    private List<LinearizationDefinition> definitionList;

    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
        File specFile = new File("src/test/resources/dummyCustomScaleaValuesResponse.json");
        response = objectMapper.readValue(specFile, GetEntityCustomScaleValueResponse.class);

        File defFile = new File("src/test/resources/LinearizationDefinitions.json");
        definitionList = objectMapper.readValue(defFile, new TypeReference<>() {});
    }

    @Test
    public void GIVEN_customScalesResponse_WHEN_mappingToDto_THEN_dtoIsCorrectlyMapped(){
        List<EntityPostCoordinationCustomScalesDto> dtos = CustomScalesMapper.mapFromResponse(response);
        assertNotNull(dtos);
        assertEquals(2, dtos.size());
        Optional<EntityPostCoordinationCustomScalesDto> infectiousAgent = extractByPostCoordinationAxis("http://id.who.int/icd/schema/infectiousAgent", dtos);
        assertTrue(infectiousAgent.isPresent());
        assertEquals(3, infectiousAgent.get().postcoordinationScaleValues().size());

        Optional<EntityPostCoordinationCustomScalesDto> associatedWith = extractByPostCoordinationAxis("http://id.who.int/icd/schema/associatedWith", dtos);

        assertTrue(associatedWith.isPresent());
        assertEquals(1, associatedWith.get().postcoordinationScaleValues().size());
    }


    private Optional<EntityPostCoordinationCustomScalesDto> extractByPostCoordinationAxis(String postCoordAxis, List<EntityPostCoordinationCustomScalesDto> dtos) {
        return dtos.stream().filter(dto -> dto.postcoordinationAxis().equalsIgnoreCase(postCoordAxis)).findFirst();
    }

}
