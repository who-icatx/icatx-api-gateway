package edu.stanford.protege.gateway;

public class VersionDoesNotMatchException extends RuntimeException {
    public VersionDoesNotMatchException(String message) {
        super(message);
    }
}
