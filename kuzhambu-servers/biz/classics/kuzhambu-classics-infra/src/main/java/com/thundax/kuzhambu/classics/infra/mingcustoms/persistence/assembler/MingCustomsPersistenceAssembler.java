package com.thundax.kuzhambu.classics.infra.mingcustoms.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsKeywordIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
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
        return new MingCustomsEntryDO(
                MingCustomsEntryIdCodec.toValue(entity.getId()),
                entity.getTitle(),
                entity.getCategory(),
                entity.getChapter(),
                entity.getSection(),
                entity.getSummary(),
                entity.getContentFormat() == null
                        ? null
                        : entity.getContentFormat().value(),
                entity.getContent(),
                entity.getOriginalExcerpts(),
                entity.getVisibility() == null ? null : entity.getVisibility().value(),
                ClassicsContentVersionIdCodec.toValue(entity.getCurrentVersionId()),
                entity.getCurrentVersionNo(),
                entity.getCurrentVersionedAt(),
                contentUpdatedAt(entity.getContentUpdatedAt()));
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
                dataObject.getOriginalExcerpts(),
                dataObject.getVisibility() == null ? null : MingCustomsVisibility.from(dataObject.getVisibility()));
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
}
