package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualitySummaryResult {
    private double entityCoverageRate;
    private double relationAccuracyRate;
    private double completenessRate;
}
