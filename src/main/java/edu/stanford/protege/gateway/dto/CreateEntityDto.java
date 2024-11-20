package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import jakarta.validation.constraints.*;

import javax.annotation.Nullable;

public record CreateEntityDto(
        @JsonProperty("entityName")
        @NotNull(message = "Entity name cannot be null")
        @NotEmpty(message = "Entity name cannot be empty")
        String entityName,

        @JsonProperty("entityParents")
        @NotNull(message = "Entity parents cannot be null")
        @NotEmpty(message = "Entity parents cannot be empty")
        ImmutableList<String> entityParents,

        @JsonProperty("languageTag")
        @Nullable
        String languageTag
) {
}

