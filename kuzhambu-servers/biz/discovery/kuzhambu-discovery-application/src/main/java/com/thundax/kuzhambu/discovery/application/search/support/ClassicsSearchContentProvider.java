package com.thundax.kuzhambu.discovery.application.search.support;

import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.discovery.application.search.result.SearchSourceContent;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ClassicsSearchContentProvider implements SearchContentProvider {

    private final ClassicsSearchContentApplicationService classicsSearchContentApplicationService;

    public ClassicsSearchContentProvider(
            ClassicsSearchContentApplicationService classicsSearchContentApplicationService) {
        this.classicsSearchContentApplicationService = classicsSearchContentApplicationService;
    }

    @Override
    public List<SearchSourceContent> listPublicContents() {
        List<ClassicsSearchSourceContent> sourceContents = classicsSearchContentApplicationService.listPublicContents();
        if (sourceContents == null || sourceContents.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceContents.stream().map(this::toSearchSourceContent).toList();
    }

    @Override
    public SearchSourceContent getPublicContent(String contentType, String contentId) {
        ClassicsSearchSourceContent sourceContent =
                classicsSearchContentApplicationService.getPublicContent(contentType, contentId);
        if (sourceContent == null) {
            return null;
        }
        return toSearchSourceContent(sourceContent);
    }

    private SearchSourceContent toSearchSourceContent(ClassicsSearchSourceContent sourceContent) {
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
                sourceContent.getStatus(),
                sourceContent.getVisibility(),
                sourceContent.getCurrentVersionNo(),
                sourceContent.getPublishedAt(),
                sourceContent.getUpdatedAt());
    }
}
