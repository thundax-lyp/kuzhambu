package com.thundax.kuzhambu.discovery.application.search.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.knowledge.facade.KnowledgeFacade;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeEntityHintsFacadeResponse;
import com.thundax.kuzhambu.knowledge.facade.response.KnowledgeTagHintFacadeResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiscoveryKnowledgeEnhancementProviderTest {

    @Test
    void enhanceShouldReadTagAndEntityHints() {
        KnowledgeFacade knowledgeFacade = mock(KnowledgeFacade.class);
        KnowledgeTagHintFacadeResponse tagHint = mock(KnowledgeTagHintFacadeResponse.class);
        when(knowledgeFacade.getTagHint(argThat(request -> "礼制".equals(request.getTerm()))))
                .thenReturn(tagHint);
        when(knowledgeFacade.listEntityHints(argThat(request -> "礼制".equals(request.getTerm()))))
                .thenReturn(KnowledgeEntityHintsFacadeResponse.builder()
                        .entityHints(List.of())
                        .build());

        DiscoveryKnowledgeEnhancementProvider provider = new DiscoveryKnowledgeEnhancementProvider(knowledgeFacade);
        com.thundax.kuzhambu.discovery.application.search.result.KnowledgeEnhancementResult enhancement =
                provider.enhance("礼制");

        assertEquals(tagHint, enhancement.tagHint());
        assertEquals(0, enhancement.recognizedEntities().size());
        verify(knowledgeFacade).getTagHint(argThat(request -> "礼制".equals(request.getTerm())));
        verify(knowledgeFacade).listEntityHints(argThat(request -> "礼制".equals(request.getTerm())));
    }
}
