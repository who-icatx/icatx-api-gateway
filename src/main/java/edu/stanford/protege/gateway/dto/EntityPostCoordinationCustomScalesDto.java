package edu.stanford.protege.gateway.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EntityPostCoordinationCustomScalesDto(List<String> postcoordinationScaleValues,
                                                    @NotNull String postcoordinationAxis) {
}
