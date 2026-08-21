package com.thundax.kuzhambu.knowledge.application.graph.operator;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.request.ClassicsPublicContentFacadeRequest;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.common.core.content.codec.ContentRefCodec;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.application.graph.dto.GraphMaterialContentSnapshotDto;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GraphMaterialContentResolver {

    private static final Set<String> SUPPORTED_CONTENT_TYPES =
            Set.of("SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS");
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final ClassicsFacade classicsFacade;

    public GraphMaterialContentResolver(ClassicsFacade classicsFacade) {
        this.classicsFacade = classicsFacade;
    }

    public GraphMaterialContentSnapshotDto resolveWorkbench(ContentRef ref) {
        requireSupportedContent(ref);
        ClassicsPublicContentFacadeResponse response = classicsFacade.getWorkbenchContent(toRequest(ref));
        ClassicsPublicContentFacadeDto content = content(response);
        if (content == null) {
            throw new BizException("Graph material content does not exist");
        }
        return toSnapshot(content);
    }

    public boolean isPortalVisible(ContentRef ref) {
        requireSupportedContent(ref);
        ClassicsPublicContentFacadeResponse response = classicsFacade.getPublicContent(toRequest(ref));
        ClassicsPublicContentFacadeDto content = content(response);
        return content != null && STATUS_PUBLISHED.equals(content.getStatus());
    }

    public Set<ContentRef> listPortalVisibleContentRefs() {
        ClassicsPublicContentsFacadeResponse response = classicsFacade.listPublicContents();
        if (response == null || response.getContents() == null) {
            return Set.of();
        }
        Set<ContentRef> refs = new LinkedHashSet<>();
        for (ClassicsPublicContentFacadeDto content : response.getContents()) {
            if (content == null
                    || !SUPPORTED_CONTENT_TYPES.contains(content.getContentType())
                    || !STATUS_PUBLISHED.equals(content.getStatus())) {
                continue;
            }
            refs.add(ContentRefCodec.toDomain(content.getContentType(), Long.valueOf(content.getContentId())));
        }
        return Set.copyOf(refs);
    }

    private ClassicsPublicContentFacadeRequest toRequest(ContentRef ref) {
        return ClassicsPublicContentFacadeRequest.builder()
                .contentType(ContentRefCodec.toContentType(ref))
                .contentId(String.valueOf(ContentRefCodec.toValue(ref)))
                .build();
    }

    private GraphMaterialContentSnapshotDto toSnapshot(ClassicsPublicContentFacadeDto content) {
        return new GraphMaterialContentSnapshotDto(
                ContentRefCodec.toDomain(content.getContentType(), Long.valueOf(content.getContentId())),
                content.getCategoryName(),
                content.getVolumeName(),
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

    private void requireSupportedContent(ContentRef ref) {
        if (ref == null || !SUPPORTED_CONTENT_TYPES.contains(ContentRefCodec.toContentType(ref))) {
            throw new BizException("Graph material content type is unsupported");
        }
    }
}
