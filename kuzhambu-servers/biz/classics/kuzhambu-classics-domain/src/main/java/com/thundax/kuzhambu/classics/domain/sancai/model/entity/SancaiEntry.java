package com.thundax.kuzhambu.classics.domain.sancai.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.common.core.sort.Sortable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiEntry implements Sortable, Versionable {
    private SancaiEntryId id;
    private SancaiVolumeId volumeId;
    private String title;
    private String originalText;
    private String translationText;
    private String summary;
    private SancaiEntryLifecycleStatus lifecycleStatus;
    private SancaiEntryVisibility visibility;
    private SancaiEntryTranslationStatus translationStatus;
    private SancaiEntryImageStatus imageStatus;
    private SancaiEntryVisualAssetStatus visualAssetStatus;
    private SancaiEntryRefinementStatus refinementStatus;
    private int priority;
    private ClassicsContentVersionId currentVersionId;
    private Integer currentVersionNo;
    private Date currentVersionedAt;
    private Date contentUpdatedAt;

    public SancaiEntry(
            SancaiEntryId id,
            SancaiVolumeId volumeId,
            String title,
            String originalText,
            String translationText,
            String summary,
            SancaiEntryLifecycleStatus lifecycleStatus,
            SancaiEntryVisibility visibility,
            SancaiEntryTranslationStatus translationStatus,
            SancaiEntryImageStatus imageStatus,
            SancaiEntryVisualAssetStatus visualAssetStatus,
            SancaiEntryRefinementStatus refinementStatus,
            int priority) {
        this.id = id;
        this.volumeId = volumeId;
        this.title = title;
        this.originalText = originalText;
        this.translationText = translationText;
        this.summary = summary;
        this.lifecycleStatus = lifecycleStatus;
        this.visibility = visibility;
        this.translationStatus = translationStatus;
        this.imageStatus = imageStatus;
        this.visualAssetStatus = visualAssetStatus;
        this.refinementStatus = refinementStatus;
        this.priority = priority;
    }

    @Override
    public ClassicsContentType contentType() {
        return ClassicsContentType.SANCAI_ENTRY;
    }

    @Override
    public ClassicsContentId contentId() {
        return ClassicsContentId.ofNullable(id == null ? null : id.value());
    }

    @Override
    public ClassicsContentVersionId currentVersionId() {
        return currentVersionId;
    }

    @Override
    public Integer currentVersionNo() {
        return currentVersionNo;
    }

    @Override
    public Date currentVersionedAt() {
        return currentVersionedAt;
    }

    @Override
    public Date contentUpdatedAt() {
        return contentUpdatedAt;
    }

    @Override
    public void markVersioned(ClassicsContentVersion version) {
        this.currentVersionId = version.getId();
        this.currentVersionNo = version.getVersionNo();
        this.currentVersionedAt = version.getVersionedAt();
    }
}
