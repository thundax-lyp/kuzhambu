package com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity;

import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MingCustomsEntry implements Versionable {
    private MingCustomsEntryId id;
    private String title;
    private String category;
    private String chapter;
    private String section;
    private String summary;
    private MingCustomsContentFormat contentFormat;
    private String content;
    private String originalExcerpts;
    private MingCustomsVisibility visibility;
    private ClassicsContentVersionId currentVersionId;
    private Integer currentVersionNo;
    private Date currentVersionedAt;
    private Date contentUpdatedAt;

    public MingCustomsEntry(
            MingCustomsEntryId id,
            String title,
            String category,
            String chapter,
            String section,
            String summary,
            MingCustomsContentFormat contentFormat,
            String content,
            String originalExcerpts,
            MingCustomsVisibility visibility) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.chapter = chapter;
        this.section = section;
        this.summary = summary;
        this.contentFormat = contentFormat;
        this.content = content;
        this.originalExcerpts = originalExcerpts;
        this.visibility = visibility;
    }

    @Override
    public ClassicsContentType contentType() {
        return ClassicsContentType.MING_CUSTOMS;
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
