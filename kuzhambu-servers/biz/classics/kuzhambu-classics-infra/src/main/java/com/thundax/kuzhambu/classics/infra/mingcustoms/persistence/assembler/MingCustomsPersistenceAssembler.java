package com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsKeywordIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsEntryDO;
import com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.dataobject.MingCustomsKeywordDO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class MingCustomsPersistenceAssembler {

    private MingCustomsPersistenceAssembler() {}

    public static MingCustomsEntryDO toObject(MingCustomsEntry entity) {
        return toEntryObject(entity);
    }

    public static MingCustomsEntry toDomain(MingCustomsEntryDO dataObject) {
        return toEntryDomain(dataObject);
    }

    public static MingCustomsEntryDO toEntryObject(MingCustomsEntry entity) {
        if (entity == null) {
            return null;
        }
        MingCustomsEntryDO dataObject = new MingCustomsEntryDO();
        dataObject.setId(MingCustomsEntryIdCodec.toValue(entity.getId()));
        dataObject.setTitle(entity.getTitle());
        dataObject.setCategory(entity.getCategory());
        dataObject.setChapter(entity.getChapter());
        dataObject.setSection(entity.getSection());
        dataObject.setSummary(entity.getSummary());
        dataObject.setContentFormat(
                entity.getContentFormat() == null
                        ? null
                        : entity.getContentFormat().value());
        dataObject.setContent(entity.getContent());
        dataObject.setOriginalExcerpts(entity.getOriginalExcerpts());
        dataObject.setLifecycleStatus(value(entity.getLifecycleStatus()));
        dataObject.setTransitionStatus(value(entity.getTransitionStatus()));
        dataObject.setCurrentPublicationJobId(
                ClassicsPublicationJobIdCodec.toValue(entity.getCurrentPublicationJobId()));
        dataObject.setCurrentVersionId(ClassicsContentVersionIdCodec.toValue(entity.getCurrentVersionId()));
        dataObject.setCurrentVersionNo(entity.getCurrentVersionNo());
        dataObject.setCurrentVersionedAt(entity.getCurrentVersionedAt());
        dataObject.setContentUpdatedAt(contentUpdatedAt(entity.getContentUpdatedAt()));
        return dataObject;
    }

    public static MingCustomsEntry toEntryDomain(MingCustomsEntryDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        MingCustomsEntry entry = new MingCustomsEntry(
                MingCustomsEntryIdCodec.toDomain(dataObject.getId()),
                dataObject.getTitle(),
                dataObject.getCategory(),
                dataObject.getChapter(),
                dataObject.getSection(),
                dataObject.getSummary(),
                dataObject.getContentFormat() == null
                        ? null
                        : MingCustomsContentFormat.from(dataObject.getContentFormat()),
                dataObject.getContent(),
                dataObject.getOriginalExcerpts());
        entry.setLifecycleStatus(parseLifecycle(dataObject.getLifecycleStatus()));
        entry.setTransitionStatus(parseTransition(dataObject.getTransitionStatus()));
        entry.setCurrentPublicationJobId(
                ClassicsPublicationJobIdCodec.toDomain(dataObject.getCurrentPublicationJobId()));
        entry.setCurrentVersionId(ClassicsContentVersionIdCodec.toDomain(dataObject.getCurrentVersionId()));
        entry.setCurrentVersionNo(dataObject.getCurrentVersionNo());
        entry.setCurrentVersionedAt(dataObject.getCurrentVersionedAt());
        entry.setContentUpdatedAt(dataObject.getContentUpdatedAt());
        return entry;
    }

    public static List<MingCustomsEntry> toEntryDomainList(List<MingCustomsEntryDO> dataObjects) {
        List<MingCustomsEntry> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (MingCustomsEntryDO dataObject : dataObjects) {
                entities.add(toEntryDomain(dataObject));
            }
        }
        return entities;
    }

    public static MingCustomsKeywordDO toKeywordObject(MingCustomsKeyword entity) {
        return entity == null
                ? null
                : new MingCustomsKeywordDO(
                        MingCustomsKeywordIdCodec.toValue(entity.getId()),
                        MingCustomsEntryIdCodec.toValue(entity.getCustomId()),
                        entity.getKeyword(),
                        entity.getPriority());
    }

    public static MingCustomsKeyword toKeywordDomain(MingCustomsKeywordDO dataObject) {
        return dataObject == null
                ? null
                : new MingCustomsKeyword(
                        MingCustomsKeywordIdCodec.toDomain(dataObject.getId()),
                        MingCustomsEntryIdCodec.toDomain(dataObject.getCustomId()),
                        dataObject.getKeyword(),
                        dataObject.getPriority() == null ? 0 : dataObject.getPriority());
    }

    public static List<MingCustomsKeyword> toKeywordDomainList(List<MingCustomsKeywordDO> dataObjects) {
        List<MingCustomsKeyword> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (MingCustomsKeywordDO dataObject : dataObjects) {
                entities.add(toKeywordDomain(dataObject));
            }
        }
        return entities;
    }

    private static Instant contentUpdatedAt(Instant contentUpdatedAt) {
        return contentUpdatedAt == null ? Instant.now() : contentUpdatedAt;
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static ClassicsPublicationLifecycleStatus parseLifecycle(String value) {
        return value == null ? null : ClassicsPublicationLifecycleStatus.valueOf(value);
    }

    private static ClassicsPublicationTransitionStatus parseTransition(String value) {
        return value == null ? null : ClassicsPublicationTransitionStatus.valueOf(value);
    }
}
