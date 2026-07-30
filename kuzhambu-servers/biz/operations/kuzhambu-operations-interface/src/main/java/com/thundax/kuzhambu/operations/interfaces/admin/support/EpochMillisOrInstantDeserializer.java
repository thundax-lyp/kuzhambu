package com.thundax.kuzhambu.operations.interfaces.admin.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class EpochMillisOrInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return Instant.ofEpochMilli(parser.getLongValue());
        }
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String value = parser.getText().trim();
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException exception) {
                return (Instant) context.handleWeirdStringValue(
                        Instant.class, value, "Expected an ISO-8601 instant or epoch-millis integer");
            }
        }
        return (Instant) context.handleUnexpectedToken(Instant.class, parser);
    }
}
