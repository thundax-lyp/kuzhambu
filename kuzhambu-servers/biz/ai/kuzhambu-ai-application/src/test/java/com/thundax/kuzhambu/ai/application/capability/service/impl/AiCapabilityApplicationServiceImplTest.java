package com.thundax.kuzhambu.ai.application.capability.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.application.capability.command.AiCapabilityMappingSaveCommand;
import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapability;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.capability.repository.AiCapabilityRepository;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiServiceConfig;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.model.model.entity.AiModelCheckRecord;
import com.thundax.kuzhambu.ai.domain.model.repository.AiModelRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiCapabilityApplicationServiceImplTest {

    @Test
    void saveMappingShouldAllowDisabledMappingWithMismatchedTags() {
        FakeCapabilityRepository capabilityRepository = new FakeCapabilityRepository();
        FakeModelRepository modelRepository = new FakeModelRepository();
        modelRepository.model.setCapabilityTags(List.of("embedding"));
        AiCapabilityApplicationServiceImpl service =
                new AiCapabilityApplicationServiceImpl(capabilityRepository, modelRepository);
        AiCapabilityMappingSaveCommand command = new AiCapabilityMappingSaveCommand();
        command.setMappingId(3001L);
        command.setScope("classics");
        command.setCapability("summary");
        command.setModelId(2001L);
        command.setEnabled(false);

        Long mappingId = service.saveMapping(command);

        assertThat(mappingId).isEqualTo(3001L);
        assertThat(capabilityRepository.mapping).isNotNull();
        assertThat(capabilityRepository.mapping.isEnabled()).isFalse();
        AiActionStatusResult actionStatus = service.getActionStatus("classics", "summary");
        assertThat(actionStatus).isNotNull();
        assertThat(actionStatus.isAvailable()).isFalse();
        assertThat(actionStatus.getUnavailableReason()).isEqualTo("No enabled capability mapping");
    }

    @Test
    void refreshActionStatusShouldRejectDisabledMappedModel() {
        FakeCapabilityRepository capabilityRepository = new FakeCapabilityRepository();
        capabilityRepository.setMapping(enabledMapping());
        FakeModelRepository modelRepository = new FakeModelRepository();
        modelRepository.model.setEnabled(false);
        AiCapabilityApplicationServiceImpl service =
                new AiCapabilityApplicationServiceImpl(capabilityRepository, modelRepository);

        AiActionStatusResult actionStatus = service.refreshActionStatus("classics", "summary");

        assertThat(actionStatus.isAvailable()).isFalse();
        assertThat(actionStatus.getUnavailableReason()).isEqualTo("Mapped model does not satisfy capability tags");
    }

    @Test
    void refreshActionStatusShouldRejectUnavailableMappedService() {
        FakeCapabilityRepository capabilityRepository = new FakeCapabilityRepository();
        capabilityRepository.setMapping(enabledMapping());
        FakeModelRepository modelRepository = new FakeModelRepository();
        modelRepository.serviceConfig.setStatus("UNAVAILABLE");
        AiCapabilityApplicationServiceImpl service =
                new AiCapabilityApplicationServiceImpl(capabilityRepository, modelRepository);

        AiActionStatusResult actionStatus = service.refreshActionStatus("classics", "summary");

        assertThat(actionStatus.isAvailable()).isFalse();
        assertThat(actionStatus.getUnavailableReason()).isEqualTo("Mapped service is unavailable");
    }

    private static AiCapabilityMapping enabledMapping() {
        AiCapabilityMapping mapping = new AiCapabilityMapping();
        mapping.setMappingId(3001L);
        mapping.setScope("classics");
        mapping.setCapability("summary");
        mapping.setModelId(2001L);
        mapping.setEnabled(true);
        return mapping;
    }

    private static class FakeCapabilityRepository implements AiCapabilityRepository {

        private AiCapabilityMapping mapping;
        private AiActionStatus actionStatus;

        private void setMapping(AiCapabilityMapping mapping) {
            this.mapping = mapping;
        }

        @Override
        public AiCapability getCapability(String capability) {
            return new AiCapability(1L, capability, "摘要生成", List.of("chat", "long-context"), "JSON", true, 10);
        }

        @Override
        public List<AiCapability> listCapabilities(Boolean enabled) {
            return new ArrayList<>();
        }

        @Override
        public AiCapabilityMapping getMapping(String scope, String capability) {
            return mapping;
        }

        @Override
        public List<AiCapabilityMapping> listMappings(String scope, String capability, Boolean enabled) {
            return mapping == null ? new ArrayList<>() : List.of(mapping);
        }

        @Override
        public List<AiCapabilityMapping> listMappingsByModelId(Long modelId) {
            return new ArrayList<>();
        }

        @Override
        public Long saveMapping(AiCapabilityMapping mapping) {
            this.mapping = mapping;
            this.mapping.setMappingId(3001L);
            return this.mapping.getMappingId();
        }

        @Override
        public int updateMapping(AiCapabilityMapping mapping) {
            this.mapping = mapping;
            return 1;
        }

        @Override
        public AiActionStatus getActionStatus(String scope, String capability) {
            return actionStatus;
        }

        @Override
        public List<AiActionStatus> listActionStatuses(String scope, String capability, Boolean available) {
            return actionStatus == null ? new ArrayList<>() : List.of(actionStatus);
        }

        @Override
        public Long saveActionStatus(AiActionStatus actionStatus) {
            this.actionStatus = actionStatus;
            this.actionStatus.setActionStatusId(4001L);
            return this.actionStatus.getActionStatusId();
        }

        @Override
        public int updateActionStatus(AiActionStatus actionStatus) {
            this.actionStatus = actionStatus;
            return 1;
        }
    }

    private static class FakeModelRepository implements AiModelRepository {

        private final AiModel model = new AiModel(
                1L,
                2001L,
                1001L,
                "gpt-4o",
                "GPT 4o",
                List.of("chat", "long-context"),
                "{}",
                "matched model",
                true,
                null);
        private final AiServiceConfig serviceConfig = new AiServiceConfig(
                1L, 1001L, "PRIMARY", "OPENAI", "https://api.example", "encrypted", true, "AVAILABLE", null, null);

        @Override
        public AiServiceConfig getServiceConfigByServiceId(Long serviceId) {
            return serviceConfig;
        }

        @Override
        public AiServiceConfig getServiceConfigByRole(String serviceRole) {
            return null;
        }

        @Override
        public Long saveServiceConfig(AiServiceConfig serviceConfig) {
            return null;
        }

        @Override
        public AiModel getModelByModelId(Long modelId) {
            model.setModelId(modelId);
            return model;
        }

        @Override
        public List<AiModel> listModels(Long serviceId, Boolean enabled) {
            return new ArrayList<>();
        }

        @Override
        public Long saveModel(AiModel model) {
            return null;
        }

        @Override
        public int updateModel(AiModel model) {
            return 0;
        }

        @Override
        public int deleteModel(Long modelId) {
            return 0;
        }

        @Override
        public Long insertCheckRecord(AiModelCheckRecord checkRecord) {
            return null;
        }

        @Override
        public List<AiModelCheckRecord> listCheckRecords(Long modelId) {
            return new ArrayList<>();
        }
    }
}
