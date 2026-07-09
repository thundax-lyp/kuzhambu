package com.thundax.kuzhambu.ai.application.invocation.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.config.service.AiServiceConfigApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.application.model.command.AiModelCheckCommand;
import com.thundax.kuzhambu.ai.application.model.service.AiModelApplicationService;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiWorkerModelConfigResolverTest {

    @Test
    void resolveShouldRejectUnavailableService() {
        FakeServiceConfigApplicationService serviceConfigService = new FakeServiceConfigApplicationService();
        serviceConfigService.serviceConfig.setStatus("UNAVAILABLE");
        AiWorkerModelConfigResolver resolver = newResolver(serviceConfigService, new FakeModelApplicationService());

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI service is unavailable");
    }

    @Test
    void resolveShouldRejectDisabledModel() {
        FakeModelApplicationService modelService = new FakeModelApplicationService();
        modelService.model.setEnabled(false);
        AiWorkerModelConfigResolver resolver = newResolver(new FakeServiceConfigApplicationService(), modelService);

        assertThatThrownBy(() -> resolver.resolve(command()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AI model is disabled");
    }

    private static AiWorkerModelConfigResolver newResolver(
            AiServiceConfigApplicationService serviceConfigService, AiModelApplicationService modelService) {
        return new AiWorkerModelConfigResolver(serviceConfigService, modelService, new ObjectMapper());
    }

    private static AiInvokeCommand command() {
        AiInvokeCommand command = new AiInvokeCommand();
        command.setServiceRole("PRIMARY");
        command.setModelId(2001L);
        return command;
    }

    private static class FakeServiceConfigApplicationService implements AiServiceConfigApplicationService {

        private final AiServiceConfig serviceConfig = new AiServiceConfig(
                1L, 1001L, "PRIMARY", "OPENAI", "https://api.example", "encrypted", true, "AVAILABLE", null, null);

        @Override
        public AiServiceConfig getByServiceId(Long serviceId) {
            return serviceConfig;
        }

        @Override
        public AiServiceConfig getByRole(String serviceRole) {
            return serviceConfig;
        }

        @Override
        public Long save(AiServiceConfig serviceConfig) {
            return null;
        }
    }

    private static class FakeModelApplicationService implements AiModelApplicationService {

        private final AiModel model =
                new AiModel(1L, 2001L, 1001L, "gpt-4o", "GPT 4o", List.of("chat"), "{}", "matched model", true, null);

        @Override
        public AiModel get(Long modelId) {
            return model;
        }

        @Override
        public List<AiModel> list(Long serviceId, Boolean enabled) {
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

        @Override
        public AiModelCheckRecord check(Long modelId) {
            return null;
        }

        @Override
        public Long recordCheck(AiModelCheckCommand command) {
            return null;
        }

        @Override
        public List<AiModelCheckRecord> listCheckRecords(Long modelId) {
            return List.of();
        }
    }
}
