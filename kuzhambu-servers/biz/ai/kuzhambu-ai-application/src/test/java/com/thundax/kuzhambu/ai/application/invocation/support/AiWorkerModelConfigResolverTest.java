package com.thundax.kuzhambu.ai.application.invocation.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.config.command.CreateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.CreateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeleteAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiBusinessConfigCommand;
import com.thundax.kuzhambu.ai.application.config.command.UpdateAiModelCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiBusinessConfigQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetAiModelQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiBusinessConfigsQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListAiModelsQuery;
import com.thundax.kuzhambu.ai.application.config.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.application.config.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
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
    void resolveShouldRejectMissingModelId() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias("translate"));

        AiWorkerModelConfigResolver resolver = newResolver(new FakeModelApplicationService());

        assertThatThrownBy(() -> resolver.resolve(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI modelId is required");
    }

    @Test
    void resolveShouldUseBusinessConfigWhenModelIdIsMissing() {
        FakeModelApplicationService modelService = new FakeModelApplicationService();
        FakeBusinessConfigApplicationService businessConfigService =
                new FakeBusinessConfigApplicationService(AiModelIdCodec.toDomain(2001L), null);
        AiWorkerModelConfigResolver resolver = newResolver(businessConfigService, modelService);

        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias("classics_translate"));

        AiWorkerModelConfigResolver.ResolvedModelConfig resolved = resolver.resolve(command);

        assertThat(command.getModelId().value()).isEqualTo(2001L);
        assertThat(resolved.modelName().value()).isEqualTo("gpt-4o");
    }

    @Test
    void resolveShouldOverrideModelParametersWithBusinessConfigParameters() {
        FakeModelApplicationService modelService = new FakeModelApplicationService();
        modelService.model.setDefaultParamsJson("{\"temperature\":0.2,\"max_tokens\":4096}");
        FakeBusinessConfigApplicationService businessConfigService =
                new FakeBusinessConfigApplicationService(AiModelIdCodec.toDomain(2001L), "{\"temperature\":0.7}");
        AiWorkerModelConfigResolver resolver = newResolver(businessConfigService, modelService);

        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias("classics_translate"));

        AiWorkerModelConfigResolver.ResolvedModelConfig resolved = resolver.resolve(command);

        assertThat(resolved.parameters().get("temperature").asDouble()).isEqualTo(0.7);
        assertThat(resolved.parameters().get("max_tokens").asInt()).isEqualTo(4096);
    }

    @Test
    void resolveShouldKeepBusinessConfigParametersAfterModelIdIsResolved() {
        FakeModelApplicationService modelService = new FakeModelApplicationService();
        modelService.model.setDefaultParamsJson("{\"temperature\":0.2,\"max_tokens\":4096}");
        FakeBusinessConfigApplicationService businessConfigService =
                new FakeBusinessConfigApplicationService(AiModelIdCodec.toDomain(2001L), "{\"temperature\":0.7}");
        AiWorkerModelConfigResolver resolver = newResolver(businessConfigService, modelService);

        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.fromAlias("classics_translate"));

        resolver.resolve(command);
        AiWorkerModelConfigResolver.ResolvedModelConfig resolvedAgain = resolver.resolve(command);

        assertThat(command.getModelId().value()).isEqualTo(2001L);
        assertThat(resolvedAgain.parameters().get("temperature").asDouble()).isEqualTo(0.7);
        assertThat(resolvedAgain.parameters().get("max_tokens").asInt()).isEqualTo(4096);
    }

    private static AiWorkerModelConfigResolver newResolver(AiModelApplicationService modelService) {
        return newResolver(new FakeBusinessConfigApplicationService(null, null), modelService);
    }

    private static AiWorkerModelConfigResolver newResolver(
            AiBusinessConfigApplicationService businessConfigService, AiModelApplicationService modelService) {
        return new AiWorkerModelConfigResolver(businessConfigService, modelService, new ObjectMapper());
    }

    private static AiInvokeCommand command() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setServiceRole("PRIMARY");
        command.setModelId(new AiModelId(2001L));
        return command;
    }

    private static class FakeModelApplicationService implements AiModelApplicationService {

        private final AiModel model = new AiModel(
                AiModelIdCodec.toDomain(2001L),
                AiApiSource.OPENAI,
                "https://api.example",
                "encrypted",
                AiModelName.of("gpt-4o"),
                "GPT 4o",
                List.of(AiModelCapability.TEXT2TEXT),
                "{}",
                "matched model",
                true,
                null);

        @Override
        public AiModel get(GetAiModelQuery query) {
            return model;
        }

        @Override
        public List<AiModel> list(ListAiModelsQuery query) {
            return List.of(model);
        }

        @Override
        public AiModelId create(CreateAiModelCommand command) {
            return null;
        }

        @Override
        public int update(UpdateAiModelCommand command) {
            return 0;
        }

        @Override
        public int delete(DeleteAiModelCommand command) {
            return 0;
        }
    }

    private static class FakeBusinessConfigApplicationService implements AiBusinessConfigApplicationService {

        private final AiBusinessConfig config;

        private FakeBusinessConfigApplicationService(AiModelId modelId, String defaultParamsJson) {
            if (modelId == null) {
                this.config = null;
                return;
            }
            this.config = new AiBusinessConfig(
                    null, AiBusinessCapability.CLASSICS_TRANSLATE, null, modelId, defaultParamsJson, true, 1, null);
        }

        @Override
        public AiBusinessConfig get(GetAiBusinessConfigQuery query) {
            return config;
        }

        @Override
        public AiBusinessConfig getByCapability(GetAiBusinessConfigByCapabilityQuery query) {
            return config;
        }

        @Override
        public List<AiBusinessConfig> list(ListAiBusinessConfigsQuery query) {
            return config == null ? List.of() : List.of(config);
        }

        @Override
        public AiBusinessConfigId create(CreateAiBusinessConfigCommand command) {
            return null;
        }

        @Override
        public int update(UpdateAiBusinessConfigCommand command) {
            return 0;
        }

        @Override
        public int delete(DeleteAiBusinessConfigCommand command) {
            return 0;
        }
    }
}
