package com.thundax.kuzhambu.operations.application.task.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsTaskQuery {
    private String sourceDomain;
    private String taskType;
    private String taskStatus;
}
