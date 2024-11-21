package edu.stanford.protege.gateway.linearization.commands;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class LinearizationSpecificationStatusDeserializer  extends StdDeserializer<LinearizationSpecificationStatus> {
    public LinearizationSpecificationStatusDeserializer(Class<?> vc) {
        super(vc);
    }
    public LinearizationSpecificationStatusDeserializer() {
        this(null);
    }

    @Override
    public LinearizationSpecificationStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {

        return LinearizationSpecificationStatus.getStatusFromString(jsonParser.getValueAsString());
    }
}
