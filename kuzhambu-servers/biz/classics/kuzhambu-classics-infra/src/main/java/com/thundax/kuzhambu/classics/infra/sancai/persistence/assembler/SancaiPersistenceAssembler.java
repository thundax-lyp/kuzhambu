package com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiCategoryDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiEntryDO;
import com.thundax.kuzhambu.classics.infra.sancai.persistence.dataobject.SancaiVolumeDO;
import java.util.ArrayList;
import java.util.List;

public final class SancaiPersistenceAssembler {

    private SancaiPersistenceAssembler() {}

    public static SancaiEntryDO toObject(SancaiEntry entity) {
        return toEntryObject(entity);
    }

    public static SancaiEntry toDomain(SancaiEntryDO dataObject) {
        return toEntryDomain(dataObject);
    }

    public static SancaiCategory toCategoryDomain(SancaiCategoryDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SancaiCategory category = new SancaiCategory();
        category.setId(dataObject.getId());
        category.setTitle(dataObject.getTitle());
        category.setCategoryType(
                dataObject.getCategoryType() == null ? null : SancaiCategoryType.from(dataObject.getCategoryType()));
        category.setPriority(priorityOrDefault(dataObject.getPriority()));
        return category;
    }

    public static List<SancaiCategory> toCategoryDomainList(List<SancaiCategoryDO> dataObjects) {
        List<SancaiCategory> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (SancaiCategoryDO dataObject : dataObjects) {
                entities.add(toCategoryDomain(dataObject));
            }
        }
        return entities;
    }

    public static SancaiVolume toVolumeDomain(SancaiVolumeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SancaiVolume volume = new SancaiVolume();
        volume.setId(dataObject.getId());
        volume.setCategoryId(dataObject.getCategoryId());
        volume.setTitle(dataObject.getTitle());
        volume.setVolumeType(
                dataObject.getVolumeType() == null ? null : SancaiVolumeType.from(dataObject.getVolumeType()));
        volume.setPriority(priorityOrDefault(dataObject.getPriority()));
        return volume;
    }

    public static List<SancaiVolume> toVolumeDomainList(List<SancaiVolumeDO> dataObjects) {
        List<SancaiVolume> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (SancaiVolumeDO dataObject : dataObjects) {
                entities.add(toVolumeDomain(dataObject));
            }
        }
        return entities;
    }

    public static SancaiEntryDO toEntryObject(SancaiEntry entity) {
        if (entity == null) {
            return null;
        }
        SancaiEntryDO dataObject = new SancaiEntryDO();
        dataObject.setId(entity.getId());
        dataObject.setVolumeId(entity.getVolumeId());
        dataObject.setTitle(entity.getTitle());
        dataObject.setOriginalText(entity.getOriginalText());
        dataObject.setTranslationText(entity.getTranslationText());
        dataObject.setSummary(entity.getSummary());
        dataObject.setLifecycleStatus(value(entity.getLifecycleStatus()));
        dataObject.setVisibility(value(entity.getVisibility()));
        dataObject.setTranslationStatus(value(entity.getTranslationStatus()));
        dataObject.setImageStatus(value(entity.getImageStatus()));
        dataObject.setVisualAssetStatus(value(entity.getVisualAssetStatus()));
        dataObject.setRefinementStatus(value(entity.getRefinementStatus()));
        dataObject.setPriority(entity.getPriority());
        return dataObject;
    }

    public static SancaiEntry toEntryDomain(SancaiEntryDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SancaiEntry entry = new SancaiEntry();
        entry.setId(dataObject.getId());
        entry.setVolumeId(dataObject.getVolumeId());
        entry.setTitle(dataObject.getTitle());
        entry.setOriginalText(dataObject.getOriginalText());
        entry.setTranslationText(dataObject.getTranslationText());
        entry.setSummary(dataObject.getSummary());
        entry.setLifecycleStatus(
                dataObject.getLifecycleStatus() == null
                        ? null
                        : SancaiEntryLifecycleStatus.from(dataObject.getLifecycleStatus()));
        entry.setVisibility(
                dataObject.getVisibility() == null ? null : SancaiEntryVisibility.from(dataObject.getVisibility()));
        entry.setTranslationStatus(
                dataObject.getTranslationStatus() == null
                        ? null
                        : SancaiEntryTranslationStatus.from(dataObject.getTranslationStatus()));
        entry.setImageStatus(
                dataObject.getImageStatus() == null ? null : SancaiEntryImageStatus.from(dataObject.getImageStatus()));
        entry.setVisualAssetStatus(
                dataObject.getVisualAssetStatus() == null
                        ? null
                        : SancaiEntryVisualAssetStatus.from(dataObject.getVisualAssetStatus()));
        entry.setRefinementStatus(
                dataObject.getRefinementStatus() == null
                        ? null
                        : SancaiEntryRefinementStatus.from(dataObject.getRefinementStatus()));
        entry.setPriority(priorityOrDefault(dataObject.getPriority()));
        return entry;
    }

    public static List<SancaiEntry> toEntryDomainList(List<SancaiEntryDO> dataObjects) {
        List<SancaiEntry> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (SancaiEntryDO dataObject : dataObjects) {
                entities.add(toEntryDomain(dataObject));
            }
        }
        return entities;
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static int priorityOrDefault(Integer priority) {
        return priority == null ? 0 : priority;
    }
}
