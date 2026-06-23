package com.thundax.kuzhambu.knowledge.application.refinement.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefinementProgressSummaryResult {
    private int entityPendingCount;
    private int entityConfirmedCount;
    private int relationPendingCount;
    private int relationConfirmedCount;
}
