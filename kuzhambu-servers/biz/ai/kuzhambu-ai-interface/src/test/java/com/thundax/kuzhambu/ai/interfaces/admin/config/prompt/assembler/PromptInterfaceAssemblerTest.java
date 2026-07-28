package com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.interfaces.admin.config.prompt.controller.request.PromptRequests;
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
}
