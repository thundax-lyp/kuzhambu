package com.thundax.kuzhambu.knowledge.application.refinement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.application.refinement.support.QualitySummaryAggregationSupport;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import java.util.List;
import org.junit.jupiter.api.Test;

class QualitySummaryAggregationSupportTest {

    private final QualitySummaryAggregationSupport support = new QualitySummaryAggregationSupport();

    @Test
    void aggregateShouldReturnZeroWhenNoDraftsExist() {
        QualitySummaryResult result = support.aggregate(List.of(), List.of());

        assertEquals(0D, result.getEntityCoverageRate());
        assertEquals(0D, result.getRelationAccuracyRate());
        assertEquals(0D, result.getCompletenessRate());
    }

    @Test
    void aggregateShouldCountManualConfirmedDrafts() {
        QualitySummaryResult result = support.aggregate(
                List.of(entityDraft("MANUAL_CONFIRMED"), entityDraft("PENDING"), entityDraft("MANUAL_CONFIRMED")),
                List.of(relationDraft("MANUAL_CONFIRMED"), relationDraft("PENDING")));

        assertEquals(2D / 3D, result.getEntityCoverageRate());
        assertEquals(1D / 2D, result.getRelationAccuracyRate());
        assertEquals(3D / 5D, result.getCompletenessRate());
    }

    private static RefinementEntityDraft entityDraft(String confirmationStatus) {
        RefinementEntityDraft draft = new RefinementEntityDraft();
        draft.setConfirmationStatus(confirmationStatus);
        return draft;
    }

    private static RefinementRelationDraft relationDraft(String confirmationStatus) {
        RefinementRelationDraft draft = new RefinementRelationDraft();
        draft.setConfirmationStatus(confirmationStatus);
        return draft;
    }
}
