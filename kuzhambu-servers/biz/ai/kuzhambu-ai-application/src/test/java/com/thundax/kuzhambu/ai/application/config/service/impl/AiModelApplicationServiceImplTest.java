package com.thundax.kuzhambu.ai.application.config.service.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.thundax.kuzhambu.ai.application.config.command.DeleteAiModelCommand;
import com.thundax.kuzhambu.ai.domain.config.codec.AiModelIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiBusinessConfigRepository;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiModelApplicationServiceImplTest {

    @Test
    void deleteShouldRejectModelUsedByBusinessConfig() {
        AiModelApplicationServiceImpl service = new AiModelApplicationServiceImpl(
                new FakeBusinessConfigRepository(AiModelIdCodec.toDomain(2001L)), new FakeModelRepository());

        assertThatThrownBy(() -> service.delete(new DeleteAiModelCommand(new AiModelId(2001L))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("AI model is used by business config");
    }

    private static class FakeBusinessConfigRepository implements AiBusinessConfigRepository {

        private final AiBusinessConfig config;

        private FakeBusinessConfigRepository(AiModelId modelId) {
            config = new AiBusinessConfig(
                    null, AiBusinessCapability.CLASSICS_TRANSLATE, null, modelId, null, true, 1, null);
        }

        @Override
        public AiBusinessConfig getById(AiBusinessConfigId id) {
            return config;
        }

        @Override
        public AiBusinessConfig getByCapability(AiBusinessCapability capability) {
            return config;
        }

        @Override
        public List<AiBusinessConfig> list(AiBusinessCapability capability, Boolean enabled) {
            return List.of(config);
        }

        @Override
        public int maxPriority() {
            return 1;
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
        public int delete(AiBusinessConfigId id) {
            return 0;
        }
    }

    private static class FakeModelRepository implements AiModelRepository {

        @Override
        public AiModel getById(AiModelId id) {
            return null;
        }

        @Override
        public List<AiModel> list(String apiSource, Boolean enabled) {
            return List.of();
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
            return 1;
        }
    }
}
