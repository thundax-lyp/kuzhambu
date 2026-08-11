package com.thundax.kuzhambu.operations.application.health.query;

public record OperationsHealthAlertQuery(
        String component,
        String alertLevel,
        String alertStatus,
        String sourceRefType,
        Long sourceRefId,
        Long latestCheckId) {}
