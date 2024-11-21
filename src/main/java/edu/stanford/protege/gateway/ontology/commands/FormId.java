package edu.stanford.protege.gateway.ontology.commands;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FormId(@JsonProperty String id){

    @JsonCreator
    public static FormId create(@JsonProperty String id) {
        return new FormId(id);
    }

}
