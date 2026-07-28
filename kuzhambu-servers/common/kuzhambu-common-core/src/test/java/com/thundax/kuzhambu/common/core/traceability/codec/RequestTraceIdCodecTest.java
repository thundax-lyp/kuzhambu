package com.thundax.kuzhambu.common.core.traceability.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.common.core.traceability.valueobject.RequestId;
import com.thundax.kuzhambu.common.core.traceability.valueobject.TraceId;
import org.junit.jupiter.api.Test;

class RequestTraceIdCodecTest {

    @Test
    void shouldConvertRequestIdFromText() {
        RequestId requestId = RequestIdCodec.toDomain(" req-1 ");

        assertEquals("req-1", requestId.value());
        assertEquals("req-1", RequestIdCodec.toValue(requestId));
    }

    @Test
    void shouldConvertTraceIdFromText() {
        TraceId traceId = TraceIdCodec.toDomain(" trace-1 ");

        assertEquals("trace-1", traceId.value());
        assertEquals("trace-1", TraceIdCodec.toValue(traceId));
    }

    @Test
    void shouldCanonicalizeValueObjectConstruction() {
        assertEquals("req-1", new RequestId(" req-1 ").value());
        assertEquals("trace-1", new TraceId(" trace-1 ").value());
    }

    @Test
    void shouldReturnNullForBlankValues() {
        assertNull(RequestIdCodec.toDomain(null));
        assertNull(RequestIdCodec.toDomain(" "));
        assertNull(RequestIdCodec.toValue(null));
        assertNull(TraceIdCodec.toDomain(null));
        assertNull(TraceIdCodec.toDomain(" "));
        assertNull(TraceIdCodec.toValue(null));
    }

    @Test
    void shouldGenerateUniqueIds() {
        RequestId requestId = RequestIdCodec.generate();
        TraceId traceId = TraceIdCodec.generate();

        assertNotNull(requestId.value());
        assertNotNull(traceId.value());
        assertNotEquals(requestId.value(), traceId.value());
    }
}
