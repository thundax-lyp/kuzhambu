package com.thundax.kuzhambu.knowledge.application.graph.operator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.facade.ClassicsFacade;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentFacadeResponse;
import com.thundax.kuzhambu.classics.facade.response.ClassicsPublicContentsFacadeResponse;
import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GraphMaterialContentResolverTest {

    private final ClassicsFacade classicsFacade = mock(ClassicsFacade.class);
    private final GraphMaterialContentResolver resolver = new GraphMaterialContentResolver(classicsFacade);

    @Test
    void shouldResolvePortalVisibilityForAllSupportedContentTypes() {
        for (String contentType : List.of("SANCAI_ENTRY", "WANGQI_DOCUMENT", "MING_CUSTOMS")) {
            when(classicsFacade.getPublicContent(argThat(request -> request != null
                            && contentType.equals(request.getContentType())
                            && "101".equals(request.getContentId()))))
                    .thenReturn(ClassicsPublicContentFacadeResponse.builder()
                            .content(content(contentType, 101L, "PUBLISHED"))
                            .build());

            assertThat(resolver.isPortalVisible(new ContentRef(contentType, 101L)))
                    .isTrue();
        }
    }

    @Test
    void shouldListVisibleContentRefsWithOneFacadeCall() {
        when(classicsFacade.listPublicContents())
                .thenReturn(ClassicsPublicContentsFacadeResponse.builder()
                        .contents(List.of(
                                content("SANCAI_ENTRY", 101L, "PUBLISHED"),
                                content("WANGQI_DOCUMENT", 201L, "PUBLISHED"),
                                content("MING_CUSTOMS", 301L, "PUBLISHED"),
                                content("SANCAI_ENTRY", 102L, "DRAFT")))
                        .build());

        assertThat(resolver.listPortalVisibleContentRefs())
                .isEqualTo(Set.of(
                        new ContentRef("SANCAI_ENTRY", 101L),
                        new ContentRef("WANGQI_DOCUMENT", 201L),
                        new ContentRef("MING_CUSTOMS", 301L)));
    }

    private static ClassicsPublicContentFacadeDto content(String contentType, long contentId, String status) {
        return ClassicsPublicContentFacadeDto.builder()
                .contentType(contentType)
                .contentId(String.valueOf(contentId))
                .status(status)
                .build();
    }
}
