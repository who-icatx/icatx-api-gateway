package edu.stanford.protege.gateway.dto;

public record EntityLinearization(String isAuxiliaryAxisChild,
                                  String isGrouping,
                                  String isIncludedInLinearization,
                                  String linearizationPathParent,
                                  String linearizationId,
                                  String codingNote) {

}
