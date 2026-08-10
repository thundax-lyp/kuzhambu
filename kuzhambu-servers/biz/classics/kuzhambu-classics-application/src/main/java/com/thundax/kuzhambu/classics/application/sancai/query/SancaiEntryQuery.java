package com.thundax.kuzhambu.classics.application.sancai.query;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Set;

public record SancaiEntryQuery(
        Long categoryId,
        Long volumeId,
        String keyword,
        SancaiEntryLifecycleStatus lifecycleStatus,
        SancaiEntryTranslationStatus translationStatus,
        SancaiEntryImageStatus imageStatus,
        SancaiEntryVisualAssetStatus visualAssetStatus,
        SancaiEntryRefinementStatus refinementStatus,
        SortDirection sortDirection,
        Set<String> operatorPermissions) {
    public SancaiEntryQuery(
            Long categoryId,
            Long volumeId,
            String keyword,
            SancaiEntryLifecycleStatus lifecycleStatus,
            SancaiEntryTranslationStatus translationStatus,
            SancaiEntryImageStatus imageStatus,
            SancaiEntryVisualAssetStatus visualAssetStatus,
            SancaiEntryRefinementStatus refinementStatus,
            SortDirection sortDirection) {
        this(
                categoryId,
                volumeId,
                keyword,
                lifecycleStatus,
                translationStatus,
                imageStatus,
                visualAssetStatus,
                refinementStatus,
                sortDirection,
                null);
    }

    public SancaiEntryQuery {
        if (sortDirection == null) {
            sortDirection = SortDirection.ASC;
        }
    }
}
