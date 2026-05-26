package com.thundax.kuzhambu.common.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DomainExceptionTest {

    @Test
    public void shouldExposeDomainFailureDetails() {
        DomainException exception = new DomainException("MEMBER-00002", "member.exception.disabled", "会员状态不可用");

        assertEquals("MEMBER-00002", exception.getCode());
        assertEquals("member.exception.disabled", exception.getMessageKey());
        assertEquals("会员状态不可用", exception.getDefaultMessage());
        assertEquals("会员状态不可用", exception.getMessage());
    }
}
