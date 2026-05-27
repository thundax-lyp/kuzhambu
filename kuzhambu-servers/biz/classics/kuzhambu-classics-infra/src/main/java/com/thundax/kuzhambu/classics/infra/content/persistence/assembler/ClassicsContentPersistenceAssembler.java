package com.thundax.kuzhambu.classics.infra.content.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentExportJobDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentQaPairDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentTagDO;
import com.thundax.kuzhambu.classics.infra.content.persistence.dataobject.ClassicsContentVersionDO;
import java.util.ArrayList;
import java.util.List;

public final class ClassicsContentPersistenceAssembler {
    private ClassicsContentPersistenceAssembler() {}

    public static ClassicsContentTagDO toObject(ClassicsContentTag entity) {
        return toTagObject(entity);
    }

    public static ClassicsContentTag toDomain(ClassicsContentTagDO dataObject) {
        return toTagDomain(dataObject);
    }

    public static ClassicsContentTagDO toTagObject(ClassicsContentTag entity) {
        return entity == null
                ? null
                : new ClassicsContentTagDO(
                        entity.getId(),
                        value(entity.getContentType()),
                        entity.getContentId(),
                        entity.getTagId(),
                        entity.getTagNameSnapshot(),
                        value(entity.getSource()),
                        value(entity.getStatus()),
                        entity.getPriority());
    }

    public static ClassicsContentTag toTagDomain(ClassicsContentTagDO dataObject) {
        return dataObject == null
                ? null
                : new ClassicsContentTag(
                        dataObject.getId(),
                        fromContentType(dataObject.getContentType()),
                        dataObject.getContentId(),
                        dataObject.getTagId(),
                        dataObject.getTagNameSnapshot(),
                        fromSource(dataObject.getSource()),
                        fromTagStatus(dataObject.getStatus()),
                        priority(dataObject.getPriority()));
    }

    public static List<ClassicsContentTag> toTagDomainList(List<ClassicsContentTagDO> dataObjects) {
        List<ClassicsContentTag> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toTagDomain(item)));
        }
        return entities;
    }

    public static ClassicsContentQaPairDO toQaObject(ClassicsContentQaPair entity) {
        return entity == null
                ? null
                : new ClassicsContentQaPairDO(
                        entity.getId(),
                        value(entity.getContentType()),
                        entity.getContentId(),
                        entity.getQuestion(),
                        entity.getAnswer(),
                        value(entity.getSource()),
                        entity.getPriority());
    }

    public static ClassicsContentQaPair toQaDomain(ClassicsContentQaPairDO dataObject) {
        return dataObject == null
                ? null
                : new ClassicsContentQaPair(
                        dataObject.getId(),
                        fromContentType(dataObject.getContentType()),
                        dataObject.getContentId(),
                        dataObject.getQuestion(),
                        dataObject.getAnswer(),
                        fromSource(dataObject.getSource()),
                        priority(dataObject.getPriority()));
    }

    public static List<ClassicsContentQaPair> toQaDomainList(List<ClassicsContentQaPairDO> dataObjects) {
        List<ClassicsContentQaPair> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toQaDomain(item)));
        }
        return entities;
    }

    public static ClassicsContentVersionDO toVersionObject(ClassicsContentVersion entity) {
        return entity == null
                ? null
                : new ClassicsContentVersionDO(
                        entity.getId(),
                        value(entity.getContentType()),
                        entity.getContentId(),
                        entity.getVersionNo(),
                        entity.getVersionedAt(),
                        entity.getSnapshotJson(),
                        value(entity.getChangeType()),
                        entity.getChangeSummary());
    }

    public static ClassicsContentVersion toVersionDomain(ClassicsContentVersionDO dataObject) {
        return dataObject == null
                ? null
                : new ClassicsContentVersion(
                        dataObject.getId(),
                        fromContentType(dataObject.getContentType()),
                        dataObject.getContentId(),
                        priority(dataObject.getVersionNo()),
                        dataObject.getVersionedAt(),
                        dataObject.getSnapshotJson(),
                        dataObject.getChangeType() == null
                                ? null
                                : ClassicsContentChangeType.from(dataObject.getChangeType()),
                        dataObject.getChangeSummary());
    }

    public static List<ClassicsContentVersion> toVersionDomainList(List<ClassicsContentVersionDO> dataObjects) {
        List<ClassicsContentVersion> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toVersionDomain(item)));
        }
        return entities;
    }

    public static ClassicsContentExportJobDO toExportObject(ClassicsContentExportJob entity) {
        return entity == null
                ? null
                : new ClassicsContentExportJobDO(
                        entity.getId(),
                        value(entity.getExportKind()),
                        value(entity.getContentType()),
                        value(entity.getExportFormat()),
                        value(entity.getScopeType()),
                        entity.getScopeJson(),
                        entity.getRequestedAt(),
                        entity.getExpiresAt(),
                        value(entity.getStatus()),
                        entity.getStorageObjectId(),
                        entity.getItemCount(),
                        entity.getAssetCount(),
                        value(entity.getVisibilityRiskStatus()),
                        entity.isContentChanged());
    }

    public static ClassicsContentExportJob toExportDomain(ClassicsContentExportJobDO dataObject) {
        return dataObject == null
                ? null
                : new ClassicsContentExportJob(
                        dataObject.getId(),
                        dataObject.getExportKind() == null ? null : ClassicsExportKind.from(dataObject.getExportKind()),
                        fromContentType(dataObject.getContentType()),
                        dataObject.getExportFormat() == null
                                ? null
                                : ClassicsExportFormat.from(dataObject.getExportFormat()),
                        dataObject.getScopeType() == null
                                ? null
                                : ClassicsExportScopeType.from(dataObject.getScopeType()),
                        dataObject.getScopeJson(),
                        dataObject.getRequestedAt(),
                        dataObject.getExpiresAt(),
                        dataObject.getStatus() == null ? null : ClassicsExportStatus.from(dataObject.getStatus()),
                        dataObject.getStorageObjectId(),
                        priority(dataObject.getItemCount()),
                        priority(dataObject.getAssetCount()),
                        dataObject.getVisibilityRiskStatus() == null
                                ? null
                                : SancaiVisibilityRiskStatus.from(dataObject.getVisibilityRiskStatus()),
                        Boolean.TRUE.equals(dataObject.getContentChanged()));
    }

    public static List<ClassicsContentExportJob> toExportDomainList(List<ClassicsContentExportJobDO> dataObjects) {
        List<ClassicsContentExportJob> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toExportDomain(item)));
        }
        return entities;
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static int priority(Integer value) {
        return value == null ? 0 : value;
    }

    private static ClassicsContentType fromContentType(String value) {
        return value == null ? null : ClassicsContentType.from(value);
    }

    private static ClassicsContentSource fromSource(String value) {
        return value == null ? null : ClassicsContentSource.from(value);
    }

    private static ClassicsContentTagStatus fromTagStatus(String value) {
        return value == null ? null : ClassicsContentTagStatus.from(value);
    }
}
