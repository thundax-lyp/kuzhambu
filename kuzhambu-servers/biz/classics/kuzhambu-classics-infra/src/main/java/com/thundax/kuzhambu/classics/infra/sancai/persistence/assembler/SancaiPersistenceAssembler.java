package com.thundax.kuzhambu.classics.infra.sancai.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
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
        category.setId(SancaiCategoryIdCodec.toDomain(dataObject.getId()));
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

    public static SancaiCategoryDO toCategoryObject(SancaiCategory entity) {
        if (entity == null) {
            return null;
        }
        SancaiCategoryDO dataObject = new SancaiCategoryDO();
        dataObject.setId(SancaiCategoryIdCodec.toValue(entity.getId()));
        dataObject.setTitle(entity.getTitle());
        dataObject.setCategoryType(entity.getCategoryType() == null ? null : entity.getCategoryType().value());
        dataObject.setPriority(entity.getPriority());
        return dataObject;
    }

    public static SancaiVolume toVolumeDomain(SancaiVolumeDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        SancaiVolume volume = new SancaiVolume();
        volume.setId(SancaiVolumeIdCodec.toDomain(dataObject.getId()));
        volume.setCategoryId(SancaiCategoryIdCodec.toDomain(dataObject.getCategoryId()));
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

    public static SancaiVolumeDO toVolumeObject(SancaiVolume entity) {
        if (entity == null) {
            return null;
        }
        SancaiVolumeDO dataObject = new SancaiVolumeDO();
        dataObject.setId(SancaiVolumeIdCodec.toValue(entity.getId()));
        dataObject.setCategoryId(SancaiCategoryIdCodec.toValue(entity.getCategoryId()));
        dataObject.setTitle(entity.getTitle());
        dataObject.setVolumeType(entity.getVolumeType() == null ? null : entity.getVolumeType().value());
        dataObject.setPriority(entity.getPriority());
        return dataObject;
    }

    public static SancaiEntryDO toEntryObject(SancaiEntry entity) {
        if (entity == null) {
            return null;
        }
        SancaiEntryDO dataObject = new SancaiEntryDO();
        dataObject.setId(SancaiEntryIdCodec.toValue(entity.getId()));
        dataObject.setVolumeId(SancaiVolumeIdCodec.toValue(entity.getVolumeId()));
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
        entry.setId(SancaiEntryIdCodec.toDomain(dataObject.getId()));
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(dataObject.getVolumeId()));
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
