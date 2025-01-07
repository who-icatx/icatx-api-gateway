package edu.stanford.protege.gateway.linearization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.gateway.config.ApplicationBeans;
import edu.stanford.protege.gateway.dto.*;
import edu.stanford.protege.gateway.linearization.commands.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EntityLinearizationMapperTest {

    private List<LinearizationDefinition> definitionList;

    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper objectMapper = new ApplicationBeans().objectMapper();
        File defFile = new File("src/test/resources/LinearizationDefinitions.json");
        definitionList = objectMapper.readValue(defFile, new TypeReference<>() {
        });
    }


    @Test
    public void GIVEN_scrambledLinearizationDTO_WHEN_mapToRequest_THEN_fieldsAreCorrectlyMapped() {
        EntityLinearization entityLinearization = new EntityLinearization("true",
                "fAlSE",
                "unKnown", null,
                "PCL", null);

        EntityLinearizationWrapperDto dto = new EntityLinearizationWrapperDto(null,
                "UnKnown",
                null,
                null,
                new LinearizationTitle("test"), Collections.singletonList(entityLinearization));

        WhoficEntityLinearizationSpecification spec = LinearizationMapper.mapFromDto("http://id.who.int/icd/entity/694903163", dto, definitionList);

        assertNotNull(spec);
        assertNotNull(spec.linearizationResiduals());
        assertEquals(LinearizationSpecificationStatus.UNKNOWN, spec.linearizationResiduals().suppressUnspecifiedResiduals());
        //null values are treated as unknown
        assertEquals(LinearizationSpecificationStatus.UNKNOWN, spec.linearizationResiduals().suppressOtherSpecifiedResiduals());
        assertNull(spec.linearizationResiduals().unspecifiedResidualTitle());
        assertEquals("test", spec.linearizationResiduals().otherSpecifiedResidualTitle());

        assertNotNull(spec.linearizationSpecifications());
        assertEquals(1, spec.linearizationSpecifications().size());
        assertEquals(LinearizationSpecificationStatus.TRUE, spec.linearizationSpecifications().get(0).isAuxiliaryAxisChild());
        assertEquals(LinearizationSpecificationStatus.FALSE, spec.linearizationSpecifications().get(0).isGrouping());
        assertEquals(LinearizationSpecificationStatus.UNKNOWN, spec.linearizationSpecifications().get(0).isIncludedInLinearization());
        assertNull(spec.linearizationSpecifications().get(0).linearizationParent());
        assertNull(spec.linearizationSpecifications().get(0).codingNote());
        assertEquals("http://id.who.int/icd/release/11/pcl", spec.linearizationSpecifications().get(0).linearizationView().toString());
    }


}
