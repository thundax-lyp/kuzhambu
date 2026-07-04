package com.thundax.kuzhambu.discovery.application.qa.support;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto.QaPair;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeDocumentAssembler {

    public KnowledgeDocument toKnowledgeDocument(ClassicsQaKnowledgeFacadeResponse response) {
        if (response == null || response.getKnowledge() == null) {
            return null;
        }
        ClassicsQaKnowledgeFacadeDto knowledge = response.getKnowledge();
        return new KnowledgeDocument(buildMetadata(knowledge), buildKnowledge(knowledge));
    }

    private KnowledgeDocument.Metadata buildMetadata(ClassicsQaKnowledgeFacadeDto sourceKnowledge) {
        return new KnowledgeDocument.Metadata(
                sourceKnowledge.getSourceId(),
                sourceKnowledge.getContentType(),
                sourceKnowledge.getContentId(),
                sourceKnowledge.getKnowledgeBase(),
                sourceKnowledge.getCurrentVersionNo(),
                sourceKnowledge.getKnowledgeRevision(),
                sourceKnowledge.getVisibility(),
                sourceKnowledge.getStatus(),
                sourceKnowledge.getSourcePath(),
                sourceKnowledge.getUpdatedAt());
    }

    private KnowledgeDocument.Knowledge buildKnowledge(ClassicsQaKnowledgeFacadeDto sourceKnowledge) {
        return new KnowledgeDocument.Knowledge(
                sourceKnowledge.getTitle(),
                sourceKnowledge.getCategoryPath(),
                sourceKnowledge.getSummary(),
                sourceKnowledge.getBody(),
                sourceKnowledge.getOriginalText(),
                sourceKnowledge.getTranslationText(),
                sourceKnowledge.getOriginalExcerpts(),
                sourceKnowledge.getTags() == null
                        ? List.of()
                        : sourceKnowledge.getTags().stream()
                                .filter(StringUtils::isNotBlank)
                                .toList(),
                sourceKnowledge.getQaPairs() == null
                        ? List.of()
                        : sourceKnowledge.getQaPairs().stream()
                                .filter(pair -> pair != null && isValidQaPair(pair))
                                .map(pair -> toKnowledgeQaPair(pair))
                                .toList());
    }

    private boolean isValidQaPair(QaPair qaPair) {
        return StringUtils.isNotBlank(qaPair.getQuestion()) && StringUtils.isNotBlank(qaPair.getAnswer());
    }

    private KnowledgeDocument.QaPair toKnowledgeQaPair(QaPair qaPair) {
        return new KnowledgeDocument.QaPair(qaPair.getQuestion(), qaPair.getAnswer());
    }
}
