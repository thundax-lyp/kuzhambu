package com.thundax.kuzhambu.discovery.application.qa.support;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.request.ClassicsQaKnowledgeFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsQaKnowledgeFacadeResponse;
import com.thundax.kuzhambu.discovery.domain.qa.model.entity.QaSource;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeSourceResolver {

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

    private final ClassicsFacade classicsFacade;

    public KnowledgeSourceResolver(ClassicsFacade classicsFacade) {
        this.classicsFacade = classicsFacade;
    }

    public QaSource resolve(QaSource source) {
        if (source == null) {
            return null;
        }
        source.setSourceStatus(resolveStatus(source));
        return source;
    }

    public String resolveStatus(QaSource source) {
        return hasCurrentVisibility(source) ? STATUS_AVAILABLE : STATUS_UNAVAILABLE;
    }

    private boolean hasCurrentVisibility(QaSource source) {
        if (source == null || source.getContentType() == null || source.getContentId() == null) {
            return false;
        }
        ClassicsQaKnowledgeFacadeResponse response =
                classicsFacade.getQaKnowledge(ClassicsQaKnowledgeFacadeRequest.builder()
                        .contentType(source.getContentType())
                        .contentId(String.valueOf(source.getContentId()))
                        .build());
        if (response == null || response.getKnowledge() == null) {
            return false;
        }
        return Objects.equals("PUBLIC", response.getKnowledge().getVisibility())
                && Objects.equals("PUBLISHED", response.getKnowledge().getStatus());
    }
}
