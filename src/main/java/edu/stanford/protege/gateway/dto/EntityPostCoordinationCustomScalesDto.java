package edu.stanford.protege.gateway.dto;

import java.util.List;

public record EntityPostCoordinationCustomScalesDto(List<String> postcoordinationScaleValues,
                                                    String postcoordinationAxis) {
}
