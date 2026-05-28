package com.thundax.kuzhambu.classics.infra.sharing.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareAccessRecordIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareLinkIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.codec.ClassicsShareTargetIdCodec;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareAccessRecord;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareLink;
import com.thundax.kuzhambu.classics.domain.sharing.model.entity.ClassicsShareTarget;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareAccessResult;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareLinkStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareTargetStatus;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsShareVisibility;
import com.thundax.kuzhambu.classics.domain.sharing.model.enums.ClassicsSharedContentVisibility;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareAccessRecordDO;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareLinkDO;
import com.thundax.kuzhambu.classics.infra.sharing.persistence.dataobject.ClassicsShareTargetDO;
import java.util.ArrayList;
import java.util.List;

public final class ClassicsSharingPersistenceAssembler {
    private ClassicsSharingPersistenceAssembler() {}

    public static ClassicsShareLinkDO toObject(ClassicsShareLink entity) {
        return toLinkObject(entity);
    }

    public static ClassicsShareLink toDomain(ClassicsShareLinkDO dataObject) {
        return toLinkDomain(dataObject);
    }

    public static ClassicsShareLinkDO toLinkObject(ClassicsShareLink entity) {
        return entity == null
                ? null
                : new ClassicsShareLinkDO(
                        ClassicsShareLinkIdCodec.toValue(entity.getId()),
                        entity.getTokenHash(),
                        entity.getTitle(),
                        value(entity.getVisibility()),
                        value(entity.getStatus()),
                        value(entity.getVisibilityRiskStatus()),
                        entity.getIssuedAt(),
                        entity.getExpiresAt(),
                        entity.getAccessCount());
    }

    public static ClassicsShareLink toLinkDomain(ClassicsShareLinkDO dataObject) {
        return dataObject == null
                ? null
                : new ClassicsShareLink(
                        ClassicsShareLinkIdCodec.toDomain(dataObject.getId()),
                        dataObject.getTokenHash(),
                        dataObject.getTitle(),
                        dataObject.getVisibility() == null
                                ? null
                                : ClassicsShareVisibility.from(dataObject.getVisibility()),
                        dataObject.getStatus() == null ? null : ClassicsShareLinkStatus.from(dataObject.getStatus()),
                        dataObject.getVisibilityRiskStatus() == null
                                ? null
                                : SancaiVisibilityRiskStatus.from(dataObject.getVisibilityRiskStatus()),
                        dataObject.getIssuedAt(),
                        dataObject.getExpiresAt(),
                        dataObject.getAccessCount() == null ? 0L : dataObject.getAccessCount());
    }

    public static List<ClassicsShareLink> toLinkDomainList(List<ClassicsShareLinkDO> dataObjects) {
        List<ClassicsShareLink> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toLinkDomain(item)));
        }
        return entities;
    }

    public static ClassicsShareTargetDO toTargetObject(ClassicsShareTarget entity) {
        return entity == null
                ? null
                : new ClassicsShareTargetDO(
                        ClassicsShareTargetIdCodec.toValue(entity.getId()),
                        ClassicsShareLinkIdCodec.toValue(entity.getShareLinkId()),
                        value(entity.getContentType()),
                        ClassicsContentIdCodec.toValue(entity.getContentId()),
                        entity.getTitleSnapshot(),
                        entity.getContentSnapshotJson(),
                        value(entity.getContentVisibilitySnapshot()),
                        value(entity.getTargetStatus()),
                        entity.getPriority());
    }

    public static ClassicsShareTarget toTargetDomain(ClassicsShareTargetDO dataObject) {
        return dataObject == null
                ? null
                : new ClassicsShareTarget(
                        ClassicsShareTargetIdCodec.toDomain(dataObject.getId()),
                        ClassicsShareLinkIdCodec.toDomain(dataObject.getShareLinkId()),
                        dataObject.getContentType() == null
                                ? null
                                : ClassicsContentType.from(dataObject.getContentType()),
                        ClassicsContentId.ofNullable(dataObject.getContentId()),
                        dataObject.getTitleSnapshot(),
                        dataObject.getContentSnapshotJson(),
                        dataObject.getContentVisibilitySnapshot() == null
                                ? null
                                : ClassicsSharedContentVisibility.from(dataObject.getContentVisibilitySnapshot()),
                        dataObject.getTargetStatus() == null
                                ? null
                                : ClassicsShareTargetStatus.from(dataObject.getTargetStatus()),
                        dataObject.getPriority() == null ? 0 : dataObject.getPriority());
    }

    public static List<ClassicsShareTarget> toTargetDomainList(List<ClassicsShareTargetDO> dataObjects) {
        List<ClassicsShareTarget> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toTargetDomain(item)));
        }
        return entities;
    }

    public static ClassicsShareAccessRecordDO toAccessObject(ClassicsShareAccessRecord entity) {
        return entity == null
                ? null
                : new ClassicsShareAccessRecordDO(
                        ClassicsShareAccessRecordIdCodec.toValue(entity.getId()),
                        ClassicsShareLinkIdCodec.toValue(entity.getShareLinkId()),
                        ClassicsShareTargetIdCodec.toValue(entity.getShareTargetId()),
                        entity.getAccessedAt(),
                        value(entity.getAccessResult()),
                        entity.getClientSnapshot());
    }

    public static ClassicsShareAccessRecord toAccessDomain(ClassicsShareAccessRecordDO dataObject) {
        return dataObject == null
                ? null
                : new ClassicsShareAccessRecord(
                        ClassicsShareAccessRecordIdCodec.toDomain(dataObject.getId()),
                        ClassicsShareLinkIdCodec.toDomain(dataObject.getShareLinkId()),
                        ClassicsShareTargetIdCodec.toDomain(dataObject.getShareTargetId()),
                        dataObject.getAccessedAt(),
                        dataObject.getAccessResult() == null
                                ? null
                                : ClassicsShareAccessResult.from(dataObject.getAccessResult()),
                        dataObject.getClientSnapshot());
    }

    public static List<ClassicsShareAccessRecord> toAccessDomainList(List<ClassicsShareAccessRecordDO> dataObjects) {
        List<ClassicsShareAccessRecord> entities = new ArrayList<>();
        if (dataObjects != null) {
            dataObjects.forEach(item -> entities.add(toAccessDomain(item)));
        }
        return entities;
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
