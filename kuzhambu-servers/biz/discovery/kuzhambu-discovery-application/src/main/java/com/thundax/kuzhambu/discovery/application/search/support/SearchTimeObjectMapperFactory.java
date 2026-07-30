package com.thundax.kuzhambu.discovery.application.search.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.Instant;

public final class SearchTimeObjectMapperFactory {

    private SearchTimeObjectMapperFactory() {}

    public static ObjectMapper create() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantEpochMillisSerializer());
        module.addDeserializer(Instant.class, new InstantEpochMillisDeserializer());
        return new ObjectMapper().registerModule(module);
    }

    private static final class InstantEpochMillisSerializer extends JsonSerializer<Instant> {

        @Override
        public void serialize(Instant value, JsonGenerator generator, SerializerProvider serializers)
                throws IOException {
            generator.writeNumber(value.toEpochMilli());
        }
    }

    private static final class InstantEpochMillisDeserializer extends JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return Instant.ofEpochMilli(parser.getLongValue());
        }
    }
}
