package edu.stanford.protege.gateway.dto;

import java.util.List;

public record EntityPostCoordinationSpecificationDto(String linearizationId,
                                                     List<String> requiredAxes,
                                                     List<String> allowedAxes,
                                                     List<String> notAllowedAxes,
                                                     List<String> overwrittenAllowedAxes,
                                                     List<String> overwrittenRequiredAxes,
                                                     List<String> overwrittenNotAllowedAxes) {
}
