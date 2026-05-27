package com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryDraft;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVisualAsset;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisualAssetStatus;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDraftDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryImageDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiShowcaseDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiVisualAssetDO;
import java.util.ArrayList;
import java.util.List;

public final class SancaiAssetPersistenceAssembler {

    private SancaiAssetPersistenceAssembler() {}

    public static SancaiEntryDraftDO toDraftObject(SancaiEntryDraft entity) {
        return entity == null ? null : new SancaiEntryDraftDO(entity.getId(), entity.getEntryId(), entity.getAutosavedAt(), entity.getDraftJson());
    }

    public static SancaiEntryDraft toDraftDomain(SancaiEntryDraftDO dataObject) {
        return dataObject == null ? null : new SancaiEntryDraft(dataObject.getId(), dataObject.getEntryId(), dataObject.getAutosavedAt(), dataObject.getDraftJson());
    }

    public static SancaiEntryImageDO toImageObject(SancaiEntryImage entity) {
        return entity == null ? null : new SancaiEntryImageDO(entity.getId(), entity.getEntryId(), entity.getStorageObjectId(), value(entity.getImageType()), entity.getTitle(), entity.isCurrentUsed(), entity.getPriority());
    }

    public static SancaiEntryImage toImageDomain(SancaiEntryImageDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new SancaiEntryImage(dataObject.getId(), dataObject.getEntryId(), dataObject.getStorageObjectId(), dataObject.getImageType() == null ? null : SancaiEntryImageType.from(dataObject.getImageType()), dataObject.getTitle(), Boolean.TRUE.equals(dataObject.getCurrentUsed()), priority(dataObject.getPriority()));
    }

    public static List<SancaiEntryImage> toImageDomainList(List<SancaiEntryImageDO> dataObjects) {
        List<SancaiEntryImage> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (SancaiEntryImageDO dataObject : dataObjects) {
                entities.add(toImageDomain(dataObject));
            }
        }
        return entities;
    }

    public static SancaiVisualAssetDO toVisualAssetObject(SancaiVisualAsset entity) {
        return entity == null ? null : new SancaiVisualAssetDO(entity.getId(), entity.getEntryId(), entity.getVersionNo(), value(entity.getStatus()), entity.getSourceImageStorageObjectId(), entity.getGeneratedImageStorageObjectId(), entity.isCurrentUsed(), entity.getTextWeight(), entity.getImageWeight(), entity.getImageAnalysisMarkdown(), entity.getFusionDescription(), entity.getVisualDescription(), entity.getGenerationParamsJson());
    }

    public static SancaiVisualAsset toVisualAssetDomain(SancaiVisualAssetDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new SancaiVisualAsset(dataObject.getId(), dataObject.getEntryId(), priority(dataObject.getVersionNo()), dataObject.getStatus() == null ? null : SancaiVisualAssetStatus.from(dataObject.getStatus()), dataObject.getSourceImageStorageObjectId(), dataObject.getGeneratedImageStorageObjectId(), Boolean.TRUE.equals(dataObject.getCurrentUsed()), priority(dataObject.getTextWeight()), priority(dataObject.getImageWeight()), dataObject.getImageAnalysisMarkdown(), dataObject.getFusionDescription(), dataObject.getVisualDescription(), dataObject.getGenerationParamsJson());
    }

    public static List<SancaiVisualAsset> toVisualAssetDomainList(List<SancaiVisualAssetDO> dataObjects) {
        List<SancaiVisualAsset> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (SancaiVisualAssetDO dataObject : dataObjects) {
                entities.add(toVisualAssetDomain(dataObject));
            }
        }
        return entities;
    }

    public static SancaiShowcaseDO toShowcaseObject(SancaiShowcase entity) {
        return entity == null ? null : new SancaiShowcaseDO(entity.getId(), entity.getRequestedAt(), value(entity.getStatus()), entity.getScopeJson(), entity.getStorageObjectId(), entity.getEntryCount(), value(entity.getVisibilityRiskStatus()));
    }

    public static SancaiShowcase toShowcaseDomain(SancaiShowcaseDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new SancaiShowcase(dataObject.getId(), dataObject.getRequestedAt(), dataObject.getStatus() == null ? null : SancaiShowcaseStatus.from(dataObject.getStatus()), dataObject.getScopeJson(), dataObject.getStorageObjectId(), priority(dataObject.getEntryCount()), dataObject.getVisibilityRiskStatus() == null ? null : SancaiVisibilityRiskStatus.from(dataObject.getVisibilityRiskStatus()));
    }

    public static List<SancaiShowcase> toShowcaseDomainList(List<SancaiShowcaseDO> dataObjects) {
        List<SancaiShowcase> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (SancaiShowcaseDO dataObject : dataObjects) {
                entities.add(toShowcaseDomain(dataObject));
            }
        }
        return entities;
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static int priority(Integer value) {
        return value == null ? 0 : value;
    }
}
