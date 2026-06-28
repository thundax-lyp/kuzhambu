package com.thundax.kuzhambu.discovery.application.qa.support;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.discovery.application.qa.result.QaSourceResult;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QaSourceAssembler {

    public List<QaSource> toDomainList(List<ClassicsPublicContentFacadeDto> sourceContents, Long messageId) {
        if (sourceContents == null || sourceContents.isEmpty()) {
            return List.of();
        }
        List<QaSource> sources = new ArrayList<>();
        for (int index = 0; index < sourceContents.size(); index++) {
            ClassicsPublicContentFacadeDto sourceContent = sourceContents.get(index);
            sources.add(new QaSource(
                    null,
                    null,
                    messageId,
                    sourceContent.getContentType(),
                    parseLong(sourceContent.getContentId()),
                    sourceContent.getKnowledgeBase(),
                    sourceContent.getTitle(),
                    StringUtils.defaultIfBlank(sourceContent.getCategoryName(), sourceContent.getCategoryCode()),
                    firstSnippet(sourceContent),
                    index + 1,
                    BigDecimal.valueOf(Math.max(1, sourceContents.size() - index)),
                    "CITED",
                    new Date()));
        }
        return sources;
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

    private String firstSnippet(ClassicsPublicContentFacadeDto sourceContent) {
        if (sourceContent == null) {
            return null;
        }
        if (sourceContent.getSummary() != null && !sourceContent.getSummary().isBlank()) {
            return sourceContent.getSummary().trim();
        }
        if (sourceContent.getTextSegments() != null) {
            for (String segment : sourceContent.getTextSegments()) {
                if (segment != null && !segment.isBlank()) {
                    return segment.trim();
                }
            }
        }
        return null;
    }
}
