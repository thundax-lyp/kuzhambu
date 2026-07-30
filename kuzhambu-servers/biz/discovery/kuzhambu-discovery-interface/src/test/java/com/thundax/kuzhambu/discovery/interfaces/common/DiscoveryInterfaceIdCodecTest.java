package com.thundax.kuzhambu.discovery.interfaces.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thundax.kuzhambu.common.web.exception.BadRequestException;
import org.junit.jupiter.api.Test;

class DiscoveryInterfaceIdCodecTest {

    @Test
    void toLongValueShouldParseValidNumericId() {
        assertEquals(5001L, DiscoveryInterfaceIdCodec.toLongValue(" 5001 "));
    }

    @Test
    void toLongValueShouldKeepBlankAsNull() {
        assertNull(DiscoveryInterfaceIdCodec.toLongValue(" "));
    }

    @Test
    void toLongValueShouldRejectOverflowNumericIdAsBadRequest() {
        assertThrows(BadRequestException.class, () -> DiscoveryInterfaceIdCodec.toLongValue("999999999999999999999"));
    }

    @Test
    void toLongValueShouldRejectNonPositiveIdAsBadRequest() {
        assertThrows(BadRequestException.class, () -> DiscoveryInterfaceIdCodec.toLongValue("0"));
        assertThrows(BadRequestException.class, () -> DiscoveryInterfaceIdCodec.toLongValue("-1"));
    }

    @Test
    void toLongValueShouldRejectNonnumericIdAsBadRequest() {
        assertThrows(BadRequestException.class, () -> DiscoveryInterfaceIdCodec.toLongValue("stored-5001"));
    }
}
