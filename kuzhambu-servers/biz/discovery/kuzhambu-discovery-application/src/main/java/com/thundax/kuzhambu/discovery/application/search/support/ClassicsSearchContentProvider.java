package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClassicsSearchContentProvider implements SearchContentProvider {

    private final ClassicsFacade classicsFacade;

    public ClassicsSearchContentProvider(ClassicsFacade classicsFacade) {
        this.classicsFacade = classicsFacade;
    }

    @Override
    public List<SearchSourceContent> listPublicContents() {
        List<ClassicsPublicContentFacadeDto> sourceContents =
                classicsFacade.listPublicContents().getContents();
        if (sourceContents == null || sourceContents.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceContents.stream().map(this::toSearchSourceContent).toList();
    }

    @Override
    public SearchSourceContent getPublicContent(String contentType, String contentId) {
        ClassicsPublicContentFacadeDto sourceContent = classicsFacade
                .getPublicContent(ClassicsPublicContentFacadeRequest.builder()
                        .contentType(contentType)
                        .contentId(contentId)
                        .build())
                .getContent();
        if (sourceContent == null) {
            return null;
        }
        return toSearchSourceContent(sourceContent);
    }

    private SearchSourceContent toSearchSourceContent(ClassicsPublicContentFacadeDto sourceContent) {
        return new SearchSourceContent(
                "CLASSICS",
                sourceContent.getContentType(),
                sourceContent.getContentId(),
                sourceContent.getKnowledgeBase(),
                sourceContent.getCategoryCode(),
                sourceContent.getCategoryName(),
                sourceContent.getTitle(),
                sourceContent.getSummary(),
                sourceContent.getTextSegments(),
                sourceContent.getTagNames(),
                sourceContent.getCurrentVersionNo(),
                sourceContent.getPublishedAt(),
                sourceContent.getUpdatedAt());
    }
}
