package edu.stanford.protege.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityChildren(@JsonProperty("children") List<String> children) {
    public static EntityChildren create(List<String> children) {
        return new EntityChildren(children);
    }
}
