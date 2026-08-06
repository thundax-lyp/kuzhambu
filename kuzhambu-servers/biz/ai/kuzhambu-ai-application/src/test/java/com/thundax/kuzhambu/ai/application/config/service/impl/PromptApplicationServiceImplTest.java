package com.thundax.kuzhambu.ai.application.config.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
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
        PromptTemplateSaveCommand command = saveCommand();
        command.setCapability(AiBusinessCapability.CLASSICS_TRANSLATE);

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
        PromptTemplateSaveCommand command = saveCommand();
        command.setId(null);

        service.save(command);

        assertThat(repository.replaceVariablesCount).isEqualTo(1);
        assertThat(repository.insertVersionCount).isEqualTo(1);
    }

    @Test
    void saveTemplateShouldRejectVariableOutsideCapabilityCatalog() {
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(new RecordingPromptRepository());
        PromptTemplateSaveCommand command = saveCommand();
        command.setVariables(List.of(new PromptTemplateSaveCommand.VariableItem("unknownName", false, null, 1)));

        assertThatThrownBy(() -> service.save(command))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt variable is not supported by capability: unknownName");
    }

    @Test
    void saveTemplateShouldCanonicalizeOptionalVariableInVersionSnapshot() {
        RecordingPromptRepository repository = new RecordingPromptRepository();
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(repository);
        PromptTemplateSaveCommand command = saveCommand();
        command.setMessageTemplatesJson("[{\"role\":\"user\",\"content\":\"{{contentType}} {{title}}\"}]");
        command.setVariablesSnapshotJson("[{\"variableName\":\"title\",\"required\":true}]");
        command.setVariables(List.of(
                new PromptTemplateSaveCommand.VariableItem("contentType", true, "内容类型", 1),
                new PromptTemplateSaveCommand.VariableItem("title", true, "wrong", 2)));

        service.save(command);

        assertThat(repository.lastInsertedVersion.getVariablesSnapshotJson())
                .contains("\"variableName\":\"title\"")
                .contains("\"required\":false")
                .contains("\"description\":\"内容标题\"");
    }

    @Test
    void saveTemplateShouldRejectMissingRequiredCapabilityVariable() {
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(new RecordingPromptRepository());
        PromptTemplateSaveCommand command = saveCommand();
        command.setMessageTemplatesJson("[{\"role\":\"user\",\"content\":\"{{title}}\"}]");
        command.setVariables(List.of(new PromptTemplateSaveCommand.VariableItem("title", false, null, 1)));

        assertThatThrownBy(() -> service.save(command))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Prompt required capability variables are missing: [contentType]");
    }

    @Test
    void saveTemplateShouldCanonicalizeSnapshotWhenVariablesAreOmitted() {
        RecordingPromptRepository repository = new RecordingPromptRepository();
        PromptApplicationServiceImpl service = new PromptApplicationServiceImpl(repository);
        PromptTemplateSaveCommand command = saveCommand();
        command.setVariables(Collections.emptyList());
        command.setVariablesSnapshotJson(
                "[{\"variableName\":\"contentType\",\"required\":false,\"description\":\"wrong\",\"priority\":3}]");

        service.save(command);

        assertThat(repository.lastInsertedVersion.getVariablesSnapshotJson())
                .contains("\"variableName\":\"contentType\"")
                .contains("\"required\":true")
                .contains("\"description\":\"内容类型\"")
                .contains("\"priority\":3");
    }

    private PromptTemplateSaveCommand saveCommand() {
        PromptTemplateSaveCommand command = new PromptTemplateSaveCommand();
        command.setId(PromptTemplateIdCodec.toDomain(1001L));
        command.setCapability(AiBusinessCapability.CLASSICS_SUMMARY);
        command.setName("summary prompt");
        command.setEnabled(true);
        command.setMessageTemplatesJson("[{\"role\":\"user\",\"content\":\"请摘要：{{contentType}}\"}]");
        command.setVariables(List.of(new PromptTemplateSaveCommand.VariableItem("contentType", true, "内容类型", 1)));
        return command;
    }

    private static class ConflictPromptRepository implements PromptRepository {

        @Override
        public PromptTemplate get(PromptTemplateId templateId) {
            return existingTemplate(templateId, AiBusinessCapability.CLASSICS_SUMMARY);
        }

        @Override
        public PromptTemplate get(AiBusinessCapability capability) {
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
        public PromptVersion getCurrentVersion(PromptTemplateId templateId) {
            PromptVersion version = new PromptVersion();
            version.setTemplateId(templateId);
            version.setVersionNo(1);
            return version;
        }

        @Override
        public PromptVersion getVersion(PromptVersionId versionId) {
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
        public int markCurrentVersion(PromptTemplateId templateId, int versionNo) {
            return 0;
        }

        @Override
        public List<PromptVariable> listVariables(PromptTemplateId templateId) {
            return Collections.emptyList();
        }

        @Override
        public int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
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
        public int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
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
