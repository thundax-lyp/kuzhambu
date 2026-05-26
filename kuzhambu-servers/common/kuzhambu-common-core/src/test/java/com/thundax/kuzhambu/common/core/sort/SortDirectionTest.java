package com.thundax.kuzhambu.common.core.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SortDirectionTest {

    @Test
    void shouldKeepStableEnumNames() {
        assertEquals("ASC", SortDirection.ASC.name());
        assertEquals("DESC", SortDirection.DESC.name());
    }
}
