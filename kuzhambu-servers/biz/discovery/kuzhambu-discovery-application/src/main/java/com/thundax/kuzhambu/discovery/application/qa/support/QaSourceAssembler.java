package com.thundax.kuzhambu.discovery.application.qa.support;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsQaKnowledgeFacadeDto;
import com.thundax.kuzhambu.common.knowledge.model.chat.KnowledgeChatSource;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QaSourceAssembler {

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";
    private static final String SOURCE_ID_SEPARATOR = ":";

    public List<QaSource> toDomainList(List<ClassicsPublicContentFacadeDto> sourceContents, Long messageId) {
        if (sourceContents == null || sourceContents.isEmpty()) {
            return List.of();
        }
        List<QaSource> sources = new ArrayList<>();
        for (int index = 0; index < sourceContents.size(); index++) {
            ClassicsPublicContentFacadeDto sourceContent = sourceContents.get(index);
            sources.add(toDomain(sourceContent, messageId, index + 1));
        }
        return sources;
    }

    public List<QaSource> toKnowledgeDomainList(List<KnowledgeChatSource> sources, Long messageId) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        List<QaSource> domainSources = new ArrayList<>();
        for (int index = 0; index < sources.size(); index++) {
            domainSources.add(toDomain(sources.get(index), messageId, index + 1));
        }
        return domainSources;
    }

    public List<QaSourceResult> toResultList(List<QaSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        List<QaSourceResult> results = new ArrayList<>();
        for (QaSource source : sources) {
            results.add(new QaSourceResult(
                    source.getSourceId(),
                    source.getContentType(),
                    source.getContentId(),
                    source.getKnowledgeBase(),
                    source.getTitleSnapshot(),
                    source.getLocationLabel(),
                    source.getSnippet(),
                    source.getSourceRank(),
                    source.getScore(),
                    source.getSourceStatus()));
        }
        return results;
    }

    public QaSource toDomain(ClassicsPublicContentFacadeDto sourceContent, Long messageId, Integer sourceRank) {
        if (sourceContent == null) {
            return null;
        }
        String sourceId = sourceId(null, sourceContent.getContentType(), sourceContent.getContentId());
        return new QaSource(
                null,
                null,
                sourceId,
                messageId,
                sourceContent.getContentType(),
                parseLong(sourceContent.getContentId()),
                sourceContent.getKnowledgeBase(),
                sourceContent.getTitle(),
                StringUtils.defaultIfBlank(sourceContent.getCategoryName(), sourceContent.getCategoryCode()),
                firstSnippet(sourceContent),
                null,
                sourceRank,
                null,
                STATUS_AVAILABLE,
                new Date());
    }

    public QaSource toDomain(ClassicsQaKnowledgeFacadeDto knowledge, Long messageId, Integer sourceRank) {
        if (knowledge == null) {
            return null;
        }
        String sourceId = sourceId(knowledge.getSourceId(), knowledge.getContentType(), knowledge.getContentId());
        return new QaSource(
                null,
                null,
                sourceId,
                messageId,
                knowledge.getContentType(),
                parseLong(knowledge.getContentId()),
                knowledge.getKnowledgeBase(),
                knowledge.getTitle(),
                knowledge.getCategoryPath(),
                StringUtils.defaultIfBlank(knowledge.getSummary(), knowledge.getBody()),
                knowledge.getSourcePath(),
                sourceRank,
                null,
                sourceStatus(sourceId, knowledge.getContentType(), knowledge.getContentId()),
                new Date());
    }

    public QaSource toDomain(KnowledgeChatSource source, Long messageId, Integer sourceRank) {
        if (source == null) {
            return null;
        }
        String sourceId = sourceId(source.sourceId(), source.contentType(), source.contentId());
        return new QaSource(
                null,
                null,
                sourceId,
                messageId,
                source.contentType(),
                parseLong(source.contentId()),
                source.knowledgeBase(),
                source.title(),
                null,
                source.snippet(),
                sourcePath(source.raw()),
                sourceRank,
                toScore(source.score()),
                sourceStatus(sourceId, source.contentType(), source.contentId()),
                new Date());
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String sourceId(String sourceId, String contentType, String contentId) {
        if (StringUtils.isNotBlank(sourceId)) {
            return sourceId;
        }
        if (StringUtils.isBlank(contentType) || StringUtils.isBlank(contentId)) {
            return null;
        }
        return contentType + SOURCE_ID_SEPARATOR + contentId;
    }

    private String sourcePath(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        Object sourcePath = raw.get("sourcePath");
        return sourcePath == null ? null : sourcePath.toString();
    }

    private String sourceStatus(String sourceBusinessId, String contentType, String contentId) {
        return StringUtils.isNotBlank(sourceBusinessId)
                        && StringUtils.isNotBlank(contentType)
                        && StringUtils.isNotBlank(contentId)
                        && parseLong(contentId) != null
                ? STATUS_AVAILABLE
                : STATUS_UNAVAILABLE;
    }

    private String firstSnippet(ClassicsPublicContentFacadeDto sourceContent) {
        if (sourceContent == null) {
            return null;
        }
        return sourceContent.getSummary();
    }

    private BigDecimal toScore(Double score) {
        return score == null ? null : BigDecimal.valueOf(score);
    }
}
