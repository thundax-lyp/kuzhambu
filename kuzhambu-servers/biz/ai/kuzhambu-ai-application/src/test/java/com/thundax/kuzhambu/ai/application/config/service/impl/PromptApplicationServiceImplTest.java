package com.thundax.kuzhambu.ai.application.config.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateVariableItem;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

class PromptApplicationServiceImplTest {

    @Test
    void saveTemplateShouldConvertVersionUniqueConflictToBusinessException() {
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(new ConflictPromptRepository());

        assertThatThrownBy(() -> service.save(saveCommand()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt version conflict, please retry: 1001#2");
    }

    @Test
    void saveTemplateShouldRejectCapabilityChange() {
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(new ConflictPromptRepository());
        PromptTemplateSaveCommand command = saveCommand(
                PromptTemplateIdCodec.toDomain(1001L),
                AiBusinessCapability.CLASSICS_TRANSLATE,
                defaultMessageTemplatesJson(),
                null,
                defaultVariables());

        assertThatThrownBy(() -> service.save(command))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt template capability can not be changed: 1001");
    }

    @Test
    void saveTemplateShouldNotReplaceVariablesWhenUpdatingExistingTemplate() {
        RecordingPromptRepository repository = new RecordingPromptRepository();
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(repository);

        service.save(saveCommand());

        assertThat(repository.replaceVariablesCount).isZero();
        assertThat(repository.insertVersionCount).isEqualTo(1);
    }

    @Test
    void saveTemplateShouldReplaceVariablesWhenCreatingTemplate() {
        RecordingPromptRepository repository = new RecordingPromptRepository();
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(repository);
        PromptTemplateSaveCommand command = saveCommand(
                null, AiBusinessCapability.CLASSICS_SUMMARY, defaultMessageTemplatesJson(), null, defaultVariables());

        service.save(command);

        assertThat(repository.replaceVariablesCount).isEqualTo(1);
        assertThat(repository.insertVersionCount).isEqualTo(1);
    }

    @Test
    void saveTemplateShouldRejectVariableOutsideCapabilityCatalog() {
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(new RecordingPromptRepository());
        PromptTemplateSaveCommand command = saveCommand(
                PromptTemplateIdCodec.toDomain(1001L),
                AiBusinessCapability.CLASSICS_SUMMARY,
                defaultMessageTemplatesJson(),
                null,
                List.of(new PromptTemplateVariableItem("unknownName", false, null, 1)));

        assertThatThrownBy(() -> service.save(command))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt variable is not supported by capability: unknownName");
    }

    @Test
    void saveTemplateShouldCanonicalizeOptionalVariableInVersionSnapshot() {
        RecordingPromptRepository repository = new RecordingPromptRepository();
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(repository);
        PromptTemplateSaveCommand command = saveCommand(
                PromptTemplateIdCodec.toDomain(1001L),
                AiBusinessCapability.CLASSICS_SUMMARY,
                "[{\"role\":\"user\",\"content\":\"{{contentType}} {{title}}\"}]",
                "[{\"variableName\":\"title\",\"required\":true}]",
                List.of(
                        new PromptTemplateVariableItem("contentType", true, "内容类型", 1),
                        new PromptTemplateVariableItem("title", true, "wrong", 2)));

        service.save(command);

        assertThat(repository.lastInsertedVersion.getVariablesSnapshotJson())
                .contains("\"variableName\":\"title\"")
                .contains("\"required\":false")
                .contains("\"description\":\"内容标题\"");
    }

    @Test
    void saveTemplateShouldRejectMissingRequiredCapabilityVariable() {
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(new RecordingPromptRepository());
        PromptTemplateSaveCommand command = saveCommand(
                PromptTemplateIdCodec.toDomain(1001L),
                AiBusinessCapability.CLASSICS_SUMMARY,
                "[{\"role\":\"user\",\"content\":\"{{title}}\"}]",
                null,
                List.of(new PromptTemplateVariableItem("title", false, null, 1)));

        assertThatThrownBy(() -> service.save(command))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt required capability variables are missing: [contentType]");
    }

    @Test
    void saveTemplateShouldCanonicalizeSnapshotWhenVariablesAreOmitted() {
        RecordingPromptRepository repository = new RecordingPromptRepository();
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(repository);
        PromptTemplateSaveCommand command = saveCommand(
                PromptTemplateIdCodec.toDomain(1001L),
                AiBusinessCapability.CLASSICS_SUMMARY,
                defaultMessageTemplatesJson(),
                "[{\"variableName\":\"contentType\",\"required\":false,\"description\":\"wrong\",\"priority\":3}]",
                List.of());

        service.save(command);

        assertThat(repository.lastInsertedVersion.getVariablesSnapshotJson())
                .contains("\"variableName\":\"contentType\"")
                .contains("\"required\":true")
                .contains("\"description\":\"内容类型\"")
                .contains("\"priority\":3");
    }

    private PromptTemplateSaveCommand saveCommand() {
        return saveCommand(
                PromptTemplateIdCodec.toDomain(1001L),
                AiBusinessCapability.CLASSICS_SUMMARY,
                defaultMessageTemplatesJson(),
                null,
                defaultVariables());
    }

    private PromptTemplateSaveCommand saveCommand(
            PromptTemplateId id,
            AiBusinessCapability capability,
            String messageTemplatesJson,
            String variablesSnapshotJson,
            List<PromptTemplateVariableItem> variables) {
        return new PromptTemplateSaveCommand(
                id,
                capability,
                "summary prompt",
                null,
                true,
                messageTemplatesJson,
                variablesSnapshotJson,
                null,
                null,
                variables);
    }

    private String defaultMessageTemplatesJson() {
        return "[{\"role\":\"user\",\"content\":\"请摘要：{{contentType}}\"}]";
    }

    private List<PromptTemplateVariableItem> defaultVariables() {
        return List.of(new PromptTemplateVariableItem("contentType", true, "内容类型", 1));
    }

    private static class ConflictPromptRepository implements PromptRepository {

        @Override
        public PromptTemplate getTemplateById(PromptTemplateId templateId) {
            return existingTemplate(templateId, AiBusinessCapability.CLASSICS_SUMMARY);
        }

        @Override
        public PromptTemplate getTemplateByCapability(AiBusinessCapability capability) {
            return null;
        }

        @Override
        public List<PromptTemplate> list(AiBusinessCapability capability, Boolean enabled) {
            return Collections.emptyList();
        }

        @Override
        public PromptTemplateId insertTemplate(PromptTemplate template) {
            return PromptTemplateIdCodec.toDomain(1001L);
        }

        @Override
        public int updateTemplate(PromptTemplate template) {
            return 1;
        }

        @Override
        public PromptVersion getCurrentVersionByTemplateId(PromptTemplateId templateId) {
            PromptVersion version = new PromptVersion();
            version.setTemplateId(templateId);
            version.setVersionNo(1);
            return version;
        }

        @Override
        public PromptVersion getVersionById(PromptVersionId versionId) {
            return null;
        }

        @Override
        public List<PromptVersion> listVersions(PromptTemplateId templateId) {
            return Collections.emptyList();
        }

        @Override
        public PromptVersionId insertVersion(PromptVersion version) {
            throw new DuplicateKeyException("uk_ai_prompt_version_no");
        }

        @Override
        public int updateCurrentVersion(PromptTemplateId templateId, int versionNo) {
            return 0;
        }

        @Override
        public List<PromptVariable> listVariables(PromptTemplateId templateId) {
            return Collections.emptyList();
        }

        @Override
        public int replaceTemplateVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
            return 0;
        }
    }

    private static class RecordingPromptRepository extends ConflictPromptRepository {

        private int insertVersionCount;
        private int replaceVariablesCount;
        private PromptVersion lastInsertedVersion;

        @Override
        public PromptVersionId insertVersion(PromptVersion version) {
            insertVersionCount++;
            lastInsertedVersion = version;
            return new PromptVersionId(2001L);
        }

        @Override
        public int replaceTemplateVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
            replaceVariablesCount++;
            return variables == null ? 0 : variables.size();
        }
    }

    private static PromptTemplate existingTemplate(PromptTemplateId templateId, AiBusinessCapability capability) {
        PromptTemplate template = new PromptTemplate();
        template.setId(templateId);
        template.setCapability(capability);
        template.setName("summary prompt");
        template.setEnabled(true);
        return template;
    }
}
