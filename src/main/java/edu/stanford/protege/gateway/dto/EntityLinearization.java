package edu.stanford.protege.gateway.dto;

import jakarta.validation.constraints.NotNull;

public record EntityLinearization(String isAuxiliaryAxisChild,
                                  String isGrouping,
                                  String isIncludedInLinearization,
                                  String linearizationPathParent,
                                  @NotNull String linearizationId,
                                  String codingNote) {

}
