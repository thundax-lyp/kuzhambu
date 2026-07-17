package com.thundax.kuzhambu.ai.application.capability.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.thundax.kuzhambu.ai.application.capability.command.AiCapabilityMappingSaveCommand;
import com.thundax.kuzhambu.ai.application.capability.result.AiActionStatusResult;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiActionStatus;
import com.thundax.kuzhambu.ai.domain.capability.model.entity.AiCapabilityMapping;
import com.thundax.kuzhambu.ai.domain.capability.repository.AiCapabilityRepository;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiModel;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiModelCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiModelId;
import com.thundax.kuzhambu.ai.domain.config.repository.AiModelRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiCapabilityApplicationServiceImplTest {

    @Test
    void saveMappingShouldAllowDisabledMappingWithMismatchedTags() {
        FakeCapabilityRepository capabilityRepository = new FakeCapabilityRepository();
        FakeModelRepository modelRepository = new FakeModelRepository();
        modelRepository.model.setCapabilities(List.of(AiModelCapability.TEXT_TO_IMAGE));
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
        modelRepository.model.setBaseUrl("");
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
                AiModelId.of(2001L),
                AiApiSource.OPENAI,
                "https://api.example",
                "encrypted",
                "gpt-4o",
                "GPT 4o",
                List.of(AiModelCapability.TEXT_TO_TEXT),
                "{}",
                "matched model",
                true,
                null);

        @Override
        public AiModel getModelById(AiModelId modelId) {
            model.setId(modelId);
            return model;
        }

        @Override
        public List<AiModel> listModels(String apiSource, Boolean enabled) {
            return new ArrayList<>();
        }

        @Override
        public AiModelId saveModel(AiModel model) {
            return null;
        }

        @Override
        public int updateModel(AiModel model) {
            return 0;
        }

        @Override
        public int deleteModel(AiModelId modelId) {
            return 0;
        }
    }
}
