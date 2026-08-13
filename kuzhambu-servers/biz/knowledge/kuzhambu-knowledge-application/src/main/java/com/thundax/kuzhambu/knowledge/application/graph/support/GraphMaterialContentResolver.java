package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GraphMaterialContentResolver {

    private static final String CONTENT_TYPE_SANCAI_ENTRY = "SANCAI_ENTRY";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final ClassicsFacade classicsFacade;

    public GraphMaterialContentResolver(ClassicsFacade classicsFacade) {
        this.classicsFacade = classicsFacade;
    }

    public GraphMaterialContentSnapshot resolveWorkbench(ContentRef ref) {
        requireSancaiEntry(ref);
        ClassicsPublicContentFacadeResponse response = classicsFacade.getWorkbenchContent(toRequest(ref));
        ClassicsPublicContentFacadeDto content = content(response);
        if (content == null) {
            throw new BizException("Graph material content does not exist");
        }
        return toSnapshot(content);
    }

    public boolean isPortalVisible(ContentRef ref) {
        requireSancaiEntry(ref);
        ClassicsPublicContentFacadeResponse response = classicsFacade.getPublicContent(toRequest(ref));
        ClassicsPublicContentFacadeDto content = content(response);
        return content != null && STATUS_PUBLISHED.equals(content.getStatus());
    }

    private ClassicsPublicContentFacadeRequest toRequest(ContentRef ref) {
        return ClassicsPublicContentFacadeRequest.builder()
                .contentType(ContentRefCodec.toContentType(ref))
                .contentId(String.valueOf(ContentRefCodec.toValue(ref)))
                .build();
    }

    private GraphMaterialContentSnapshot toSnapshot(ClassicsPublicContentFacadeDto content) {
        return new GraphMaterialContentSnapshot(
                ContentRefCodec.toDomain(content.getContentType(), Long.valueOf(content.getContentId())),
                content.getTitle(),
                content.getSummary(),
                safeList(content.getTextSegments()),
                safeList(content.getTagNames()),
                content.getStatus(),
                content.getVisibility(),
                content.getCurrentVersionNo());
    }

    private ClassicsPublicContentFacadeDto content(ClassicsPublicContentFacadeResponse response) {
        return response == null ? null : response.getContent();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private void requireSancaiEntry(ContentRef ref) {
        if (ref == null || !CONTENT_TYPE_SANCAI_ENTRY.equals(ContentRefCodec.toContentType(ref))) {
            throw new BizException("Graph material only supports SANCAI_ENTRY content");
        }
    }
}
