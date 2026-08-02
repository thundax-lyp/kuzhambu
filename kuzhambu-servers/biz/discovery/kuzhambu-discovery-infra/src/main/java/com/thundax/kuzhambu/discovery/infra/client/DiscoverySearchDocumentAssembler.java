package com.thundax.kuzhambu.discovery.infra.client;

import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DiscoverySearchDocumentAssembler {

    public DiscoverySearchDocument toDocument(SearchSourceContent sourceContent) {
        if (sourceContent == null) {
            return null;
        }
        DiscoverySearchDocument document = new DiscoverySearchDocument();
        document.setDocumentId(buildDocumentId(sourceContent));
        document.setContentDomain(sourceContent.getContentDomain());
        document.setContentType(sourceContent.getContentType());
        document.setContentId(sourceContent.getContentId());
        document.setKnowledgeBase(sourceContent.getKnowledgeBase());
        document.setCategoryCode(sourceContent.getCategoryCode());
        document.setCategoryName(sourceContent.getCategoryName());
        document.setTitle(sourceContent.getTitle());
        document.setSummary(sourceContent.getSummary());
        document.setBodyText(joinBodyText(sourceContent.getTextSegments()));
        document.setTextSegments(sourceContent.getTextSegments());
        document.setTagNames(
                sourceContent.getTagNames() == null ? Collections.emptyList() : sourceContent.getTagNames());
        document.setSourceVersionNo(sourceContent.getCurrentVersionNo());
        document.setPublishedAt(sourceContent.getPublishedAt());
        document.setUpdatedAt(sourceContent.getUpdatedAt());
        document.setDeleted(Boolean.FALSE);
        document.setSourcePath(buildSourcePath(sourceContent));
        return document;
    }

    private String buildDocumentId(SearchSourceContent sourceContent) {
        return sourceContent.getContentType() + ":" + sourceContent.getContentId();
    }

    private String joinBodyText(List<String> textSegments) {
        if (textSegments == null || textSegments.isEmpty()) {
            return null;
        }
        return textSegments.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .map(this::stripHtml)
                .filter(value -> !value.isBlank())
                .reduce((left, right) -> left + "\n" + right)
                .orElse(null);
    }

    private String stripHtml(String value) {
        return value.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String buildSourcePath(SearchSourceContent sourceContent) {
        String contentId = sourceContent.getContentId();
        return switch (sourceContent.getContentType()) {
            case "SANCAI_ENTRY" -> "/classics/sancai/" + contentId;
            case "WANGQI_DOCUMENT" -> "/classics/wangqi/" + contentId;
            case "MING_CUSTOMS" -> "/classics/ming-customs/" + contentId;
            default ->
                throw new UnsupportedOperationException(
                        "Unknown discovery search source content type: " + sourceContent.getContentType());
        };
    }
}
