package com.thundax.kuzhambu.ai.application.config.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptVersionId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiBusinessConfigApplicationServiceImplTest {

    @Test
    void updateShouldRejectCapabilityChange() {
        FakeBusinessConfigRepository businessConfigRepository = new FakeBusinessConfigRepository();
        businessConfigRepository.existing = businessConfig(AiBusinessCapability.CLASSICS_SUMMARY);
        AiBusinessConfigApplicationServiceImpl service = new AiBusinessConfigApplicationServiceImpl(
                businessConfigRepository,
                new FakePromptRepository(AiBusinessCapability.CLASSICS_TRANSLATE),
                new FakeAiModelRepository());

        BizException exception = assertThrows(
                BizException.class, () -> service.update(businessConfig(AiBusinessCapability.CLASSICS_TRANSLATE)));

        assertEquals("AI business config capability cannot be changed", exception.getMessage());
        assertEquals(0, businessConfigRepository.updateCount);
    }

    private static AiBusinessConfig businessConfig(AiBusinessCapability capability) {
        return new AiBusinessConfig(
                new AiBusinessConfigId(9001L),
                capability,
                new PromptTemplateId(930001L),
                new AiModelId(910001L),
                "{}",
                true,
                10,
                null);
    }

    private static class FakeBusinessConfigRepository implements AiBusinessConfigRepository {

        private AiBusinessConfig existing;
        private int updateCount;

        @Override
        public AiBusinessConfig get(AiBusinessConfigId id) {
            return existing;
        }

        @Override
        public AiBusinessConfig get(AiBusinessCapability capability) {
            return null;
        }

        @Override
        public List<AiBusinessConfig> list(AiBusinessCapability capability, Boolean enabled) {
            return List.of();
        }

        @Override
        public AiBusinessConfigId insert(AiBusinessConfig config) {
            return config.getId();
        }

        @Override
        public int update(AiBusinessConfig config) {
            updateCount += 1;
            return updateCount;
        }

        @Override
        public int maxPriority() {
            return 10;
        }

        @Override
        public int delete(AiBusinessConfigId id) {
            return 0;
        }
    }

    private record FakePromptRepository(AiBusinessCapability capability) implements PromptRepository {

        @Override
        public PromptTemplate get(PromptTemplateId templateId) {
            return new PromptTemplate(templateId, capability, "模板", null, true, 1, null);
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
            return template.getId();
        }

        @Override
        public int updateTemplate(PromptTemplate template) {
            return 0;
        }

        @Override
        public PromptVersion getCurrentVersion(PromptTemplateId templateId) {
            return null;
        }

        @Override
        public PromptVersion getVersion(PromptVersionId versionId) {
            return null;
        }

        @Override
        public List<PromptVersion> listVersions(PromptTemplateId templateId) {
            return List.of();
        }

        @Override
        public PromptVersionId insertVersion(PromptVersion version) {
            return version.getId();
        }

        @Override
        public int markCurrentVersion(PromptTemplateId templateId, int versionNo) {
            return 0;
        }

        @Override
        public List<PromptVariable> listVariables(PromptTemplateId templateId) {
            return List.of();
        }

        @Override
        public int replaceVariables(PromptTemplateId templateId, List<PromptVariable> variables) {
            return 0;
        }
    }

    private static class FakeAiModelRepository implements AiModelRepository {

        @Override
        public AiModel get(AiModelId id) {
            return new AiModel(
                    id,
                    AiApiSource.OPENAI,
                    "https://example.com",
                    "encrypted",
                    "gpt-test",
                    "测试模型",
                    List.of(AiModelCapability.TEXT2TEXT),
                    "{}",
                    null,
                    true,
                    null);
        }

        @Override
        public List<AiModel> list(String apiSource, Boolean enabled) {
            return List.of();
        }

        @Override
        public AiModelId insert(AiModel model) {
            return model.getId();
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
