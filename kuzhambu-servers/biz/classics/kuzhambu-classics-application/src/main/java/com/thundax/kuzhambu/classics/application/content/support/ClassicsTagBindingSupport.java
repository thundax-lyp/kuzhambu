package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.domain.common.model.valueobject.KnowledgeTagId;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.knowledge.domain.service.KnowledgeTagBindingDomainService;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.entity.Tag;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.ContentType;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.enums.TagSource;
import com.thundax.kuzhambu.knowledge.domain.taxonomy.model.valueobject.TagId;
import org.springframework.stereotype.Component;

@Component
public class ClassicsTagBindingSupport {
    private final KnowledgeTagBindingDomainService knowledgeTagBindingDomainService;
    private final ClassicsContentRepository repository;

    public ClassicsTagBindingSupport(
            KnowledgeTagBindingDomainService knowledgeTagBindingDomainService, ClassicsContentRepository repository) {
        this.knowledgeTagBindingDomainService = knowledgeTagBindingDomainService;
        this.repository = repository;
    }

    public ClassicsContentTag bindManualTag(ContentTagCommand command, Integer priority) {
        ClassicsContentTag tag = command.toEntity();
        Tag knowledgeTag = knowledgeTagBindingDomainService.resolveOrCreateManualTag(command.getTagNameSnapshot());
        tag.setTagId(KnowledgeTagId.ofNullable(
                knowledgeTag == null || knowledgeTag.getTagId() == null
                        ? null
                        : knowledgeTag.getTagId().value()));
        if (priority != null) {
            tag.setPriority(priority);
        }
        return tag;
    }

    public void syncTagRef(ClassicsContentTag tag) {
        if (tag == null || tag.getTagId() == null || tag.getContentType() == null || tag.getContentId() == null) {
            return;
        }
        knowledgeTagBindingDomainService.syncContentTagRef(
                TagId.of(tag.getTagId().value()),
                ContentType.from(tag.getContentType().value()),
                tag.getContentId().value(),
                resolveContentTitle(tag.getContentType(), tag.getContentId()),
                toKnowledgeSource(tag.getSource()));
    }

    public void removeTagRef(ClassicsContentTag tag) {
        if (tag == null || tag.getTagId() == null || tag.getContentType() == null || tag.getContentId() == null) {
            return;
        }
        knowledgeTagBindingDomainService.removeContentTagRef(
                TagId.of(tag.getTagId().value()),
                ContentType.from(tag.getContentType().value()),
                tag.getContentId().value());
    }

    private String resolveContentTitle(ClassicsContentType contentType, ClassicsContentId contentId) {
        return switch (contentType) {
            case SANCAI_ENTRY -> titleOf(repository.getSancaiEntryForAiApply(contentId));
            case WANGQI_DOCUMENT -> titleOf(repository.getWangqiDocumentForAiApply(contentId));
            case MING_CUSTOMS -> titleOf(repository.getMingCustomsEntryForAiApply(contentId));
        };
    }

    private TagSource toKnowledgeSource(ClassicsContentSource source) {
        return source == ClassicsContentSource.AI ? TagSource.AI_EXTRACTED : TagSource.MANUAL;
    }

    private String titleOf(Versionable content) {
        if (content instanceof SancaiEntry entry) {
            return entry.getTitle();
        }
        if (content instanceof WangqiDocument document) {
            return document.getTitle();
        }
        if (content instanceof MingCustomsEntry entry) {
            return entry.getTitle();
        }
        return null;
    }
}
