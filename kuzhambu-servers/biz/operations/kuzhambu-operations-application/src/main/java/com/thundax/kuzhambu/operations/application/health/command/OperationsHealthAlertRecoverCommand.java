package com.thundax.kuzhambu.operations.application.health.command;

import com.thundax.kuzhambu.operations.domain.health.model.valueobject.HealthAlertId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsHealthAlertRecoverCommand {
    private HealthAlertId alertId;
    private Long recoveredByUserId;
}
