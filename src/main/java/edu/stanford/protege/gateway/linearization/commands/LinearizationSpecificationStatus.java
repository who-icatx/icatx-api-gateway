package edu.stanford.protege.gateway.linearization.commands;

public enum LinearizationSpecificationStatus {
    TRUE,
    FALSE,
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
        return UNKNOWN;
    }
}
