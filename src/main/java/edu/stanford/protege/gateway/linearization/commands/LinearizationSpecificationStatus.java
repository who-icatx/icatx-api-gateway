package edu.stanford.protege.gateway.linearization.commands;

public enum LinearizationSpecificationStatus {
    TRUE,
    FALSE,
    FOLLOW_BASE_LINEARIZATION,
    UNKNOWN;

    public static LinearizationSpecificationStatus getStatusFromString(String input) {
        if(input == null || input.isEmpty()) {
            return LinearizationSpecificationStatus.UNKNOWN;
        }
        if("true".equalsIgnoreCase(input)){
            return TRUE;
        }
        if("false".equalsIgnoreCase(input)){
            return FALSE;
        }
        if("FOLLOW_BASE_LINEARIZATION".equalsIgnoreCase(input)){
            return FOLLOW_BASE_LINEARIZATION;
        }
        return UNKNOWN;
    }
}
