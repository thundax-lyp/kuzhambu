package com.thundax.kuzhambu.ai.application.invocation.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeContext;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeModelConfig;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeOptions;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePayload;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokePrompt;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTarget;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeTrace;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeWorkerRoute;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelName;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVariableId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiBusinessInvokeConfigResolverTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void resolveShouldRenderPromptFromBusinessConfigAndInputPayload() throws Exception {
        AiBusinessInvokeConfigResolver resolver = newResolver(promptRepository(
                List.of(variable("contentType", true), variable("sourceText", true), variable("tone", false))));
        AiInvokeCommand command = command();

        AiBusinessInvokeConfigResolver.ResolvedBusinessInvokeConfig resolved = resolver.resolveConfig(command);

        JsonNode messages = objectMapper.readTree(resolved.promptMessagesJson());
        JsonNode variables = objectMapper.readTree(resolved.promptVariablesJson());

        assertThat(resolved.modelId().value()).isEqualTo(2001L);
        assertThat(resolved.serviceRole()).isEqualTo("PRIMARY");
        assertThat(resolved.modelName().value()).isEqualTo("gpt-4o");
        assertThat(resolved.promptVersionId().value()).isEqualTo(6L);
        assertThat(resolved.outputSchemaJson()).isEqualTo("{\"type\":\"text\"}");
        assertThat(messages.get(1).get("content").asText())
                .contains("SANCAI_ENTRY")
                .contains("天地玄黄");
        assertThat(variables.get("contentType").asText()).isEqualTo("SANCAI_ENTRY");
        assertThat(variables.get("sourceText").asText()).isEqualTo("天地玄黄");
    }

    @Test
    void resolveShouldRejectMissingRequiredBusinessVariable() {
        AiBusinessInvokeConfigResolver resolver =
                newResolver(promptRepository(List.of(variable("contentType", true), variable("missingText", true))));

        assertThatThrownBy(() -> resolver.resolveConfig(command()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt required variables are missing: [missingText]");
    }

    @Test
    void resolveShouldRejectDisabledPromptTemplate() {
        AiBusinessInvokeConfigResolver resolver = newResolver(
                promptRepository(List.of(variable("contentType", true), variable("sourceText", true)), null, false));

        assertThatThrownBy(() -> resolver.resolveConfig(command()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI business config prompt template is disabled or mismatched: 6");
    }

    @Test
    void validatePromptVersionEnabledShouldRejectDisabledPromptTemplate() {
        AiBusinessInvokeConfigResolver resolver = newResolver(
                promptRepository(List.of(variable("contentType", true), variable("sourceText", true)), null, false));
        AiInvokeCommand command = command(new PromptVersionId(6L));

        assertThatThrownBy(() -> resolver.validatePromptVersionEnabled(command))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI business config prompt template is disabled or mismatched: 6");
    }

    @Test
    void resolveShouldUsePromptVersionVariableSnapshotWhenLiveVariablesChanged() throws Exception {
        String variablesSnapshotJson =
                """
                [
                  {"variableName":"contentType","required":true,"description":"内容类型","priority":1},
                  {"variableName":"sourceText","required":true,"description":"原文","priority":2}
                ]
                """;
        AiBusinessInvokeConfigResolver resolver =
                newResolver(promptRepository(List.of(variable("latestText", true)), variablesSnapshotJson));
        AiInvokeCommand command = command();

        AiBusinessInvokeConfigResolver.ResolvedBusinessInvokeConfig resolved = resolver.resolveConfig(command);

        JsonNode variables = objectMapper.readTree(resolved.promptVariablesJson());
        assertThat(variables.get("sourceText").asText()).isEqualTo("天地玄黄");
        assertThat(variables.has("latestText")).isFalse();
    }

    @Test
    void resolveShouldAcceptLegacyPromptVersionVariableSnapshotNameField() throws Exception {
        String variablesSnapshotJson =
                """
                [
                  {"name":"contentType","required":true,"description":"内容类型"},
                  {"name":"sourceText","required":true,"description":"原文"}
                ]
                """;
        AiBusinessInvokeConfigResolver resolver =
                newResolver(promptRepository(List.of(variable("latestText", true)), variablesSnapshotJson));
        AiInvokeCommand command = command();

        AiBusinessInvokeConfigResolver.ResolvedBusinessInvokeConfig resolved = resolver.resolveConfig(command);

        JsonNode variables = objectMapper.readTree(resolved.promptVariablesJson());
        assertThat(variables.get("contentType").asText()).isEqualTo("SANCAI_ENTRY");
        assertThat(variables.get("sourceText").asText()).isEqualTo("天地玄黄");
    }

    private AiBusinessInvokeConfigResolver newResolver(PromptRepository promptRepository) {
        FakeBusinessConfigRepository businessConfigRepository = new FakeBusinessConfigRepository();
        AiWorkerModelConfigResolver modelConfigResolver =
                new AiWorkerModelConfigResolver(businessConfigRepository, new FakeModelRepository(), objectMapper);
        return new AiBusinessInvokeConfigResolver(
                businessConfigRepository, promptRepository, modelConfigResolver, objectMapper);
    }

    private static AiInvokeCommand command() {
        return command(null);
    }

    private static AiInvokeCommand command(PromptVersionId promptVersionId) {
        return new AiInvokeCommand(
                new AiInvokeContext(null, "classics", AiBusinessCapability.CLASSICS_SUMMARY),
                new AiInvokeWorkerRoute(null, null, null),
                new AiInvokeTarget(
                        com.thundax.kuzhambu.ai.domain.invocation.model.valueobject.AiContentRef.ofNullable(
                                "SANCAI_ENTRY", 300000000001L),
                        null),
                new AiInvokeModelConfig(null, null, null, null),
                new AiInvokeTrace(null, null),
                new AiInvokePrompt(promptVersionId, null, null, null),
                new AiInvokePayload("{\"sourceText\":\"天地玄黄\"}", null),
                new AiInvokeOptions(false, false, null, false, true));
    }

    private static PromptVariable variable(String name, boolean required) {
        PromptVariable variable = new PromptVariable();
        variable.setId(new PromptVariableId(1L));
        variable.setTemplateId(new PromptTemplateId(6L));
        variable.setVariableName(name);
        variable.setRequired(required);
        return variable;
    }

    private static PromptRepository promptRepository(List<PromptVariable> variables) {
        return promptRepository(variables, null);
    }

    private static PromptRepository promptRepository(List<PromptVariable> variables, String variablesSnapshotJson) {
        return promptRepository(variables, variablesSnapshotJson, true);
    }

    private static PromptRepository promptRepository(
            List<PromptVariable> variables, String variablesSnapshotJson, boolean templateEnabled) {
        return new PromptRepository() {
            @Override
            public PromptTemplate getTemplateById(PromptTemplateId templateId) {
                return new PromptTemplate(
                        templateId,
                        AiBusinessCapability.CLASSICS_SUMMARY,
                        "summary prompt",
                        "summary prompt",
                        templateEnabled,
                        1,
                        null);
            }

            @Override
            public PromptTemplate getTemplateByCapability(AiBusinessCapability capability) {
                return null;
            }

            @Override
            public List<PromptTemplate> list(AiBusinessCapability capability, Boolean enabled) {
                return List.of();
            }

            @Override
            public PromptTemplateId insertTemplate(PromptTemplate template) {
                return null;
            }

            @Override
            public int updateTemplate(PromptTemplate template) {
                return 0;
            }

            @Override
            public PromptVersion getCurrentVersionByTemplateId(PromptTemplateId templateId) {
                PromptVersion version = new PromptVersion();
                version.setId(new PromptVersionId(6L));
                version.setTemplateId(templateId);
                version.setMessageTemplatesJson(
                        """
                        [
                          {"role":"system","content":"生成条目摘要。"},
                          {"role":"user","content":"内容类型：{{contentType}}\\n原文：{{sourceText}}"}
                        ]
                        """);
                version.setVariablesSnapshotJson(variablesSnapshotJson);
                version.setOutputSchemaJson("{\"type\":\"text\"}");
                return version;
            }

            @Override
            public PromptVersion getVersionById(PromptVersionId versionId) {
                PromptVersion version = getCurrentVersion(new PromptTemplateId(6L));
                version.setId(versionId);
                return version;
            }

            @Override
            public List<PromptVersion> listVersions(PromptTemplateId templateId) {
                return List.of();
            }

            @Override
            public PromptVersionId insertVersion(PromptVersion version) {
                return null;
            }

            @Override
            public int updateCurrentVersion(PromptTemplateId templateId, int versionNo) {
                return 0;
            }

            @Override
            public List<PromptVariable> listVariables(PromptTemplateId templateId) {
                return variables;
            }

            @Override
            public int replaceTemplateVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
                return 0;
            }
        };
    }

    private static class FakeBusinessConfigRepository implements AiBusinessConfigRepository {

        @Override
        public AiBusinessConfig getById(AiBusinessConfigId id) {
            return config();
        }

        @Override
        public AiBusinessConfig getByCapability(AiBusinessCapability capability) {
            return config();
        }

        @Override
        public List<AiBusinessConfig> list(AiBusinessCapability capability, Boolean enabled) {
            return List.of(config());
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

        private AiBusinessConfig config() {
            return new AiBusinessConfig(
                    null,
                    AiBusinessCapability.CLASSICS_SUMMARY,
                    new PromptTemplateId(6L),
                    new AiModelId(2001L),
                    "{\"temperature\":0.7}",
                    true,
                    1,
                    null);
        }
    }

    private static class FakeModelRepository implements AiModelRepository {

        @Override
        public AiModel getById(AiModelId id) {
            return new AiModel(
                    new AiModelId(2001L),
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
        }

        @Override
        public List<AiModel> list(String apiSource, Boolean enabled) {
            return List.of(get(new AiModelId(2001L)));
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
}
