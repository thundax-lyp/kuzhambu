package com.thundax.kuzhambu.operations.application.health.command;

import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;

public record OperationsHealthAlertRecoverCommand(HealthAlertId alertId, Long recoveredByUserId) {}
