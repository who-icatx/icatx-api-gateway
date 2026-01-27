package edu.stanford.protege.gateway.ontology.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

import javax.annotation.Nonnull;
import java.util.List;

@JsonTypeName(ValidateLogicalDefinitionFromApiRequest.CHANNEL)
public record ValidateLogicalDefinitionFromApiRequest(
        @JsonProperty("projectId") @Nonnull ProjectId projectId,
        @JsonProperty("entityIri") @Nonnull String entityIri,
        @JsonProperty("logicalDefinitions") @Nonnull List<LogicalDefinition> logicalDefinitions,
        @JsonProperty("necessaryConditions") @Nonnull List<Relationship> necessaryConditions
) implements Request<ValidateLogicalDefinitionFromApiResponse> {

    public static final String CHANNEL = "webprotege.icd.ValidateLogicalDefinitionsFromApi";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public record LogicalDefinition(
            @JsonProperty("logicalDefinitionSuperclass") @Nonnull String logicalDefinitionSuperclass,
            @JsonProperty("relationships") @Nonnull List<Relationship> relationships
    ) {}

    public record Relationship(
            @JsonProperty("axis") @Nonnull String axis,
            @JsonProperty("filler") @Nonnull String filler
    ) {}
}
