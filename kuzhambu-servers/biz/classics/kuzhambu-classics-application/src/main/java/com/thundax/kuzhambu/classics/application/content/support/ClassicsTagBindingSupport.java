package com.thundax.kuzhambu.classics.application.content.support;

import com.thundax.kuzhambu.classics.application.content.assembler.ClassicsContentApplicationAssembler;
import com.thundax.kuzhambu.classics.application.content.command.ContentTagCommand;
import com.thundax.kuzhambu.classics.domain.common.codec.KnowledgeTagIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentSource;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeContentTagRefFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeRemoveContentTagRefFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.request.KnowledgeResolveTagFacadeRequest;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagFacadeResponse;
import org.springframework.stereotype.Component;

@Component
public class ClassicsTagBindingSupport {
    private final KnowledgeFacade knowledgeFacade;
    private final ClassicsContentRepository repository;

    public ClassicsTagBindingSupport(KnowledgeFacade knowledgeFacade, ClassicsContentRepository repository) {
        this.knowledgeFacade = knowledgeFacade;
        this.repository = repository;
    }

    public ClassicsContentTag bindManualTag(ContentTagCommand command, Integer priority) {
        return bindTag(command, priority, false);
    }

    public ClassicsContentTag bindAiTag(ContentTagCommand command, Integer priority) {
        return bindTag(command, priority, true);
    }

    private ClassicsContentTag bindTag(ContentTagCommand command, Integer priority, boolean aiTag) {
        ClassicsContentTag tag = ClassicsContentApplicationAssembler.toTag(command);
        KnowledgeResolveTagFacadeRequest request = KnowledgeResolveTagFacadeRequest.builder()
                .tagName(command.tagNameSnapshot())
                .build();
        KnowledgeTagFacadeResponse knowledgeTag = aiTag
                ? knowledgeFacade.resolveOrCreateAiTag(request)
                : knowledgeFacade.resolveOrCreateManualTag(request);
        tag.setTagId(KnowledgeTagIdCodec.toDomain(knowledgeTag == null ? null : knowledgeTag.getTagId()));
        if (priority != null) {
            tag.setPriority(priority);
        }
        return tag;
    }

    public void syncTagRef(ClassicsContentTag tag) {
        if (tag == null || tag.getTagId() == null || tag.getContentType() == null || tag.getContentId() == null) {
            return;
        }
        knowledgeFacade.syncContentTagRef(KnowledgeContentTagRefFacadeRequest.builder()
                .tagId(tag.getTagId().value())
                .contentType(toKnowledgeContentType(tag.getContentType()))
                .contentId(tag.getContentId().value())
                .contentTitle(resolveContentTitle(tag.getContentType(), tag.getContentId()))
                .tagSource(toKnowledgeSource(tag.getSource()))
                .build());
    }

    public void removeTagRef(ClassicsContentTag tag) {
        if (tag == null || tag.getTagId() == null || tag.getContentType() == null || tag.getContentId() == null) {
            return;
        }
        knowledgeFacade.removeContentTagRef(KnowledgeRemoveContentTagRefFacadeRequest.builder()
                .tagId(tag.getTagId().value())
                .contentType(toKnowledgeContentType(tag.getContentType()))
                .contentId(tag.getContentId().value())
                .build());
    }

    private String resolveContentTitle(ClassicsContentType contentType, ClassicsContentId contentId) {
        return switch (contentType) {
            case SANCAI_ENTRY -> titleOf(repository.getBySancaiEntryForAiApply(contentId));
            case WANGQI_DOCUMENT -> titleOf(repository.getByWangqiDocumentForAiApply(contentId));
            case MING_CUSTOMS -> titleOf(repository.getByMingCustomsEntryForAiApply(contentId));
        };
    }

    private String toKnowledgeContentType(ClassicsContentType contentType) {
        return contentType == ClassicsContentType.MING_CUSTOMS ? "MING_CUSTOM" : contentType.value();
    }

    private String toKnowledgeSource(ClassicsContentSource source) {
        return source == ClassicsContentSource.AI ? "AI_EXTRACTED" : "MANUAL";
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
