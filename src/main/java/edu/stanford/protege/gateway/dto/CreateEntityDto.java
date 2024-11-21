package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import javax.annotation.Nullable;

public record CreateEntityDto(
        @JsonProperty("title")
        @NotNull(message = "Entity name cannot be null")
        @NotEmpty(message = "Entity name cannot be empty")
        String title,

        @JsonProperty("parent")
        @NotNull(message = "Entity parent cannot be null")
        @NotEmpty(message = "Entity parent cannot be empty")
        String parent,

        @JsonProperty("languageTag")
        @Nullable
        String languageTag
) {
}

