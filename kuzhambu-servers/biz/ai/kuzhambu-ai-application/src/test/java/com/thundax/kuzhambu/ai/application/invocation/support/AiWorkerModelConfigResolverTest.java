package com.thundax.kuzhambu.ai.application.invocation.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiWorkerModelConfigResolverTest {

    @Test
    void resolveShouldRejectModelWithoutBaseUrl() {
        FakeModelRepository modelService = new FakeModelRepository();
        modelService.model.setBaseUrl("");
        AiWorkerModelConfigResolver resolver = newResolver(modelService);

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI model baseUrl is required");
    }

    @Test
    void resolveShouldRejectDisabledModel() {
        FakeModelRepository modelService = new FakeModelRepository();
        modelService.model.setEnabled(false);
        AiWorkerModelConfigResolver resolver = newResolver(modelService);

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI model is disabled");
    }

    @Test
    void resolveShouldRejectMissingModelId() {
        AiInvokeCommand command = command(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.from(
                        AiBusinessCapability.CLASSICS_TRANSLATE.value()),
                null,
                null);

        AiWorkerModelConfigResolver resolver = newResolver(new FakeModelRepository());

        assertThatThrownBy(() -> resolver.resolve(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI modelId is required");
    }

    @Test
    void resolveShouldUseBusinessConfigWhenModelIdIsMissing() {
        FakeModelRepository modelService = new FakeModelRepository();
        FakeBusinessConfigRepository businessConfigService =
                new FakeBusinessConfigRepository(AiModelIdCodec.toDomain(2001L), null);
        AiWorkerModelConfigResolver resolver = newResolver(businessConfigService, modelService);

        AiInvokeCommand command = command(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.from("CLASSICS_TRANSLATE"),
                null,
                null);

        AiWorkerModelConfigResolver.ResolvedModelConfig resolved = resolver.resolve(command);

        assertThat(resolved.modelId().value()).isEqualTo(2001L);
        assertThat(resolved.modelName().value()).isEqualTo("gpt-4o");
    }

    @Test
    void resolveShouldOverrideModelParametersWithBusinessConfigParameters() {
        FakeModelRepository modelService = new FakeModelRepository();
        modelService.model.setDefaultParamsJson("{\"temperature\":0.2,\"max_tokens\":4096}");
        FakeBusinessConfigRepository businessConfigService =
                new FakeBusinessConfigRepository(AiModelIdCodec.toDomain(2001L), "{\"temperature\":0.7}");
        AiWorkerModelConfigResolver resolver = newResolver(businessConfigService, modelService);

        AiInvokeCommand command = command(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.from("CLASSICS_TRANSLATE"),
                null,
                null);

        AiWorkerModelConfigResolver.ResolvedModelConfig resolved = resolver.resolve(command);

        assertThat(resolved.parameters().get("temperature").asDouble()).isEqualTo(0.7);
        assertThat(resolved.parameters().get("max_tokens").asInt()).isEqualTo(4096);
    }

    @Test
    void resolveShouldKeepBusinessConfigParametersAfterModelIdIsResolved() {
        FakeModelRepository modelService = new FakeModelRepository();
        modelService.model.setDefaultParamsJson("{\"temperature\":0.2,\"max_tokens\":4096}");
        FakeBusinessConfigRepository businessConfigService =
                new FakeBusinessConfigRepository(AiModelIdCodec.toDomain(2001L), "{\"temperature\":0.7}");
        AiWorkerModelConfigResolver resolver = newResolver(businessConfigService, modelService);

        AiInvokeCommand command = command(
                com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability.from("CLASSICS_TRANSLATE"),
                null,
                null);

        AiWorkerModelConfigResolver.ResolvedModelConfig resolved = resolver.resolve(command);
        AiWorkerModelConfigResolver.ResolvedModelConfig resolvedAgain = resolver.resolve(command);

        assertThat(resolved.modelId().value()).isEqualTo(2001L);
        assertThat(resolvedAgain.parameters().get("temperature").asDouble()).isEqualTo(0.7);
        assertThat(resolvedAgain.parameters().get("max_tokens").asInt()).isEqualTo(4096);
    }

    private static AiWorkerModelConfigResolver newResolver(AiModelRepository modelRepository) {
        return newResolver(new FakeBusinessConfigRepository(null, null), modelRepository);
    }

    private static AiWorkerModelConfigResolver newResolver(
            AiBusinessConfigRepository businessConfigRepository, AiModelRepository modelRepository) {
        return new AiWorkerModelConfigResolver(businessConfigRepository, modelRepository, new ObjectMapper());
    }

    private static AiInvokeCommand command() {
        return command(null, new AiModelId(2001L), "PRIMARY");
    }

    private static AiInvokeCommand command(AiBusinessCapability capability, AiModelId modelId, String serviceRole) {
        return new AiInvokeCommand(
                null,
                "classics",
                capability,
                null,
                null,
                null,
                null,
                null,
                null,
                serviceRole,
                modelId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                false,
                null,
                false,
                true);
    }

    private static class FakeModelRepository implements AiModelRepository {

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
        public AiModel get(AiModelId id) {
            return model;
        }

        @Override
        public List<AiModel> list(String apiSource, Boolean enabled) {
            return List.of(model);
        }

        @Override
        public AiModelId insert(AiModel model) {
            return null;
        }

        @Override
        public int update(AiModel model) {
            return 0;
        }

        @Override
        public int delete(AiModelId id) {
            return 0;
        }
    }

    private static class FakeBusinessConfigRepository implements AiBusinessConfigRepository {

        private final AiBusinessConfig config;

        private FakeBusinessConfigRepository(AiModelId modelId, String defaultParamsJson) {
            if (modelId == null) {
                this.config = null;
                return;
            }
            this.config = new AiBusinessConfig(
                    null, AiBusinessCapability.CLASSICS_TRANSLATE, null, modelId, defaultParamsJson, true, 1, null);
        }

        @Override
        public AiBusinessConfig get(AiBusinessConfigId id) {
            return config;
        }

        @Override
        public AiBusinessConfig get(AiBusinessCapability capability) {
            return config;
        }

        @Override
        public List<AiBusinessConfig> list(AiBusinessCapability capability, Boolean enabled) {
            return config == null ? List.of() : List.of(config);
        }

        @Override
        public AiBusinessConfigId insert(AiBusinessConfig config) {
            return null;
        }

        @Override
        public int update(AiBusinessConfig config) {
            return 0;
        }

        @Override
        public int maxPriority() {
            return 0;
        }

        @Override
        public int delete(AiBusinessConfigId id) {
            return 0;
        }
    }
}
