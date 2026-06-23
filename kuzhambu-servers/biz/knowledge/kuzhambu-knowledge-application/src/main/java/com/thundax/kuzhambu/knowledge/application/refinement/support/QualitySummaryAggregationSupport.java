package com.thundax.kuzhambu.knowledge.application.refinement.support;

import com.thundax.kuzhambu.knowledge.application.refinement.result.QualitySummaryResult;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementEntityDraft;
import com.thundax.kuzhambu.knowledge.domain.refinement.model.entity.RefinementRelationDraft;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QualitySummaryAggregationSupport {

    public QualitySummaryResult aggregate(
            List<RefinementEntityDraft> entityDrafts, List<RefinementRelationDraft> relationDrafts) {
        double entityCoverageRate = ratio(confirmed(entityDrafts), size(entityDrafts));
        double relationAccuracyRate = ratio(confirmed(relationDrafts), size(relationDrafts));
        double completenessRate =
                ratio(confirmed(entityDrafts) + confirmed(relationDrafts), size(entityDrafts) + size(relationDrafts));
        return new QualitySummaryResult(entityCoverageRate, relationAccuracyRate, completenessRate);
    }

    private int confirmed(List<?> drafts) {
        return drafts == null
                ? 0
                : (int) drafts.stream()
                        .filter(item -> {
                            if (item instanceof RefinementEntityDraft entityDraft) {
                                return "MANUAL_CONFIRMED".equals(entityDraft.getConfirmationStatus());
                            }
                            return "MANUAL_CONFIRMED".equals(((RefinementRelationDraft) item).getConfirmationStatus());
                        })
                        .count();
    }

    private int size(List<?> drafts) {
        return drafts == null ? 0 : drafts.size();
    }

    private double ratio(int numerator, int denominator) {
        return denominator <= 0 ? 0D : (double) numerator / denominator;
    }
}
