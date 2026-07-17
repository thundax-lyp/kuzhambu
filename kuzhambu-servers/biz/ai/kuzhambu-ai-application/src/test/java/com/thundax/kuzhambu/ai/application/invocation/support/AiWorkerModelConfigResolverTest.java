package com.thundax.kuzhambu.ai.application.invocation.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.config.business.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.application.config.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
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
        command.setCapability("translate");

        AiWorkerModelConfigResolver resolver = newResolver(new FakeModelApplicationService());

        assertThatThrownBy(() -> resolver.resolve(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI modelId is required");
    }

    @Test
    void resolveShouldUseBusinessConfigWhenModelIdIsMissing() {
        FakeModelApplicationService modelService = new FakeModelApplicationService();
        FakeBusinessConfigApplicationService businessConfigService =
                new FakeBusinessConfigApplicationService(AiModelIdCodec.toDomain(2001L));
        AiWorkerModelConfigResolver resolver = newResolver(businessConfigService, modelService);

        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability("classics_translate");

        AiWorkerModelConfigResolver.ResolvedModelConfig resolved = resolver.resolve(command);

        assertThat(command.getModelId()).isEqualTo(2001L);
        assertThat(resolved.modelName()).isEqualTo("gpt-4o");
    }

    private static AiWorkerModelConfigResolver newResolver(AiModelApplicationService modelService) {
        return newResolver(new FakeBusinessConfigApplicationService(null), modelService);
    }

    private static AiWorkerModelConfigResolver newResolver(
            AiBusinessConfigApplicationService businessConfigService, AiModelApplicationService modelService) {
        return new AiWorkerModelConfigResolver(businessConfigService, modelService, new ObjectMapper());
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

    private static class FakeBusinessConfigApplicationService implements AiBusinessConfigApplicationService {

        private final AiBusinessConfig config;

        private FakeBusinessConfigApplicationService(AiModelId modelId) {
            if (modelId == null) {
                this.config = null;
                return;
            }
            this.config = new AiBusinessConfig(
                    null, AiBusinessCapability.CLASSICS_TRANSLATE, null, modelId, null, true, 1, null);
        }

        @Override
        public AiBusinessConfig get(Long id) {
            return config;
        }

        @Override
        public AiBusinessConfig get(String capability) {
            return config;
        }

        @Override
        public List<AiBusinessConfig> list(String capability, Boolean enabled) {
            return config == null ? List.of() : List.of(config);
        }

        @Override
        public Long save(AiBusinessConfig config) {
            return null;
        }

        @Override
        public int update(AiBusinessConfig config) {
            return 0;
        }

        @Override
        public int delete(Long id) {
            return 0;
        }
    }
}
