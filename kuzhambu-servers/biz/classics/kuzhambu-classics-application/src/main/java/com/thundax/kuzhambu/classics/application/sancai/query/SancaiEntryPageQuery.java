package com.thundax.kuzhambu.classics.application.sancai.query;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntryPageQuery {
    private Long categoryId;
    private Long volumeId;
    private String keyword;
    private SancaiEntryLifecycleStatus lifecycleStatus;
    private SancaiEntryVisibility visibility;
    private SancaiEntryTranslationStatus translationStatus;
    private SancaiEntryImageStatus imageStatus;
    private SancaiEntryVisualAssetStatus visualAssetStatus;
    private SancaiEntryRefinementStatus refinementStatus;
    private SortDirection sortDirection = SortDirection.ASC;
    private Set<String> operatorPermissions;

    public SancaiEntryPageQuery(
            Long categoryId,
            Long volumeId,
            String keyword,
            SancaiEntryLifecycleStatus lifecycleStatus,
            SancaiEntryVisibility visibility,
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
                visibility,
                translationStatus,
                imageStatus,
                visualAssetStatus,
                refinementStatus,
                sortDirection,
                null);
    }
}
