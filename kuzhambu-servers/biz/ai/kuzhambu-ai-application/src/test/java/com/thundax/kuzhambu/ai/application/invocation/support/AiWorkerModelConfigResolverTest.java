package com.thundax.kuzhambu.ai.application.invocation.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.capability.command.AiCapabilityMappingSaveCommand;
import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiWorkerModelConfigResolverTest {

    @Test
    void resolveShouldRejectModelWithoutBaseUrl() {
        FakeModelApplicationService modelService = new FakeModelApplicationService();
        modelService.model.setBaseUrl("");
        AiWorkerModelConfigResolver resolver = newResolver(modelService);

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI model baseUrl is required");
    }

    @Test
    void resolveShouldRejectDisabledModel() {
        FakeModelApplicationService modelService = new FakeModelApplicationService();
        modelService.model.setEnabled(false);
        AiWorkerModelConfigResolver resolver = newResolver(modelService);

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI model is disabled");
    }

    @Test
    void resolveShouldUseCapabilityMappingWhenModelIdIsMissing() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability("translate");

        AiWorkerModelConfigResolver resolver = newResolver(new FakeModelApplicationService());

        var resolved = resolver.resolve(command);

        assertThat(command.getModelId()).isEqualTo(2001L);
        assertThat(resolved.serviceRole()).isEqualTo("PRIMARY");
        assertThat(resolved.modelName()).isEqualTo("gpt-4o");
    }

    private static AiWorkerModelConfigResolver newResolver(AiModelApplicationService modelService) {
        return new AiWorkerModelConfigResolver(
                modelService, new FakeCapabilityApplicationService(), new ObjectMapper());
    }

    private static AiInvokeCommand command() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setServiceRole("PRIMARY");
        command.setModelId(2001L);
        return command;
    }

    private static class FakeModelApplicationService implements AiModelApplicationService {

        private final AiModel model = new AiModel(
                AiModelIdCodec.toDomain(2001L),
                AiApiSource.OPENAI,
                "https://api.example",
                "encrypted",
                "gpt-4o",
                "GPT 4o",
                List.of(AiModelCapability.TEXT2TEXT),
                "{}",
                "matched model",
                true,
                null);

        @Override
        public AiModel get(Long modelId) {
            return model;
        }

        @Override
        public List<AiModel> list(String apiSource, Boolean enabled) {
            return List.of(model);
        }

        @Override
        public Long save(AiModel model) {
            return null;
        }

        @Override
        public int update(AiModel model) {
            return 0;
        }

        @Override
        public int delete(Long modelId) {
            return 0;
        }
    }

    private static class FakeCapabilityApplicationService implements AiCapabilityApplicationService {

        @Override
        public AiBusinessCapability getCapability(String capability) {
            return AiBusinessCapability.from(capability);
        }

        @Override
        public List<AiBusinessCapability> listCapabilities(Boolean enabled) {
            return List.of(AiBusinessCapability.CLASSICS_TRANSLATE);
        }

        @Override
        public AiCapabilityMapping getMapping(String scope, String capability) {
            return new AiCapabilityMapping(1L, 3001L, scope, capability, 2001L, true, null);
        }

        @Override
        public List<AiCapabilityMapping> listMappings(String scope, String capability, Boolean enabled) {
            return List.of();
        }

        @Override
        public Long saveMapping(AiCapabilityMappingSaveCommand command) {
            return null;
        }

        @Override
        public void assertModelCanBeDeleted(Long modelId) {}

        @Override
        public void refreshActionStatusesByModelId(Long modelId) {}

        @Override
        public void refreshActionStatusesByCapability(String capability) {}

        @Override
        public AiActionStatusResult getActionStatus(String scope, String capability) {
            return null;
        }

        @Override
        public List<AiActionStatusResult> listActionStatuses(String scope, String capability, Boolean available) {
            return List.of();
        }

        @Override
        public AiActionStatusResult refreshActionStatus(String scope, String capability) {
            return null;
        }
    }
}
