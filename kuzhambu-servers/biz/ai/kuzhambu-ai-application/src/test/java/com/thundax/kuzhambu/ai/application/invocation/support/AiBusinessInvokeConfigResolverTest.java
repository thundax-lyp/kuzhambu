package com.thundax.kuzhambu.ai.application.invocation.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.config.business.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.application.config.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVariableId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
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

        resolver.resolve(command);

        JsonNode messages = objectMapper.readTree(command.getPromptMessagesJson());
        JsonNode variables = objectMapper.readTree(command.getPromptVariablesJson());

        assertThat(command.getModelId()).isEqualTo(2001L);
        assertThat(command.getServiceRole()).isEqualTo("PRIMARY");
        assertThat(command.getModelName()).isEqualTo("gpt-4o");
        assertThat(command.getPromptVersionId()).isEqualTo(940106L);
        assertThat(command.getOutputSchemaJson()).isEqualTo("{\"type\":\"text\"}");
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

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt required variables are missing: [missingText]");
    }

    @Test
    void resolveShouldRejectDisabledPromptTemplate() {
        AiBusinessInvokeConfigResolver resolver = newResolver(
                promptRepository(List.of(variable("contentType", true), variable("sourceText", true)), null, false));

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI business config prompt template is disabled or mismatched: 930106");
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

        resolver.resolve(command);

        JsonNode variables = objectMapper.readTree(command.getPromptVariablesJson());
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

        resolver.resolve(command);

        JsonNode variables = objectMapper.readTree(command.getPromptVariablesJson());
        assertThat(variables.get("contentType").asText()).isEqualTo("SANCAI_ENTRY");
        assertThat(variables.get("sourceText").asText()).isEqualTo("天地玄黄");
    }

    private AiBusinessInvokeConfigResolver newResolver(PromptRepository promptRepository) {
        FakeBusinessConfigApplicationService businessConfigService = new FakeBusinessConfigApplicationService();
        AiWorkerModelConfigResolver modelConfigResolver =
                new AiWorkerModelConfigResolver(businessConfigService, new FakeModelApplicationService(), objectMapper);
        return new AiBusinessInvokeConfigResolver(
                businessConfigService, promptRepository, modelConfigResolver, objectMapper);
    }

    private static AiInvokeCommand command() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setScope("classics");
        command.setCapability(AiBusinessCapability.CLASSICS_SUMMARY.value());
        command.setContentType("SANCAI_ENTRY");
        command.setContentId(300000000001L);
        command.setInputPayloadJson("{\"sourceText\":\"天地玄黄\"}");
        return command;
    }

    private static PromptVariable variable(String name, boolean required) {
        PromptVariable variable = new PromptVariable();
        variable.setId(new PromptVariableId(1L));
        variable.setTemplateId(new PromptTemplateId(930106L));
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
            public PromptTemplate get(PromptTemplateId templateId) {
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
            public PromptTemplate get(AiBusinessCapability capability) {
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
            public PromptVersion getCurrentVersion(PromptTemplateId templateId) {
                PromptVersion version = new PromptVersion();
                version.setId(new PromptVersionId(940106L));
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
            public List<PromptVersion> listVersions(PromptTemplateId templateId) {
                return List.of();
            }

            @Override
            public PromptVersionId insertVersion(PromptVersion version) {
                return null;
            }

            @Override
            public int markCurrentVersion(PromptTemplateId templateId, int versionNo) {
                return 0;
            }

            @Override
            public List<PromptVariable> listVariables(PromptTemplateId templateId) {
                return variables;
            }

            @Override
            public int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
                return 0;
            }
        };
    }

    private static class FakeBusinessConfigApplicationService implements AiBusinessConfigApplicationService {

        @Override
        public AiBusinessConfig get(Long id) {
            return config();
        }

        @Override
        public AiBusinessConfig get(String capability) {
            return config();
        }

        @Override
        public List<AiBusinessConfig> list(String capability, Boolean enabled) {
            return List.of(config());
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

        private AiBusinessConfig config() {
            return new AiBusinessConfig(
                    null,
                    AiBusinessCapability.CLASSICS_SUMMARY,
                    new PromptTemplateId(930106L),
                    new AiModelId(2001L),
                    "{\"temperature\":0.7}",
                    true,
                    1,
                    null);
        }
    }

    private static class FakeModelApplicationService implements AiModelApplicationService {

        @Override
        public AiModel get(Long id) {
            return new AiModel(
                    new AiModelId(2001L),
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
        }

        @Override
        public List<AiModel> list(String apiSource, Boolean enabled) {
            return List.of(get(2001L));
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
        public int delete(Long id) {
            return 0;
        }
    }
}
