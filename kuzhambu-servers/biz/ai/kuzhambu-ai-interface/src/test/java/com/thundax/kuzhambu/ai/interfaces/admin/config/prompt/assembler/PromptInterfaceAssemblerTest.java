package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.request.PromptRequests;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.response.PromptResponses;
import org.junit.jupiter.api.Test;

class PromptInterfaceAssemblerTest {

    @Test
    void shouldConvertTemplateIdentityAndCapabilityToDomainTypes() {
        PromptRequests.TemplateSaveRequest request = new PromptRequests.TemplateSaveRequest();
        request.setId(1001L);
        request.setCapability("classics_summary");

        PromptTemplateSaveCommand command = PromptInterfaceAssembler.toSaveCommand(request);

        assertThat(command.getId()).isEqualTo(new PromptTemplateId(1001L));
        assertThat(command.getCapability()).isEqualTo(AiBusinessCapability.CLASSICS_SUMMARY);
    }

    @Test
    void shouldConvertVersionIdentityAtInterfaceBoundary() {
        PromptRequests.VersionCompareRequest request = new PromptRequests.VersionCompareRequest();
        request.setId(1001L);

        PromptVersionCompareQuery query = PromptInterfaceAssembler.toCompareQuery(request);
        PromptResponses.VersionResponse response = PromptInterfaceAssembler.toResponse(new PromptVersionResult(
                new PromptVersionId(2001L), new PromptTemplateId(1001L), 1, null, null, null, null, null));

        assertThat(query.getTemplateId()).isEqualTo(new PromptTemplateId(1001L));
        assertThat(response.getId()).isEqualTo(2001L);
        assertThat(response.getTemplateId()).isEqualTo(1001L);
    }
}
