package edu.stanford.protege.gateway.projects;

public record ReproducibleProject(String projectId, long lastBackupTimestamp, String associatedBranch) {
}
