package com.thundax.kuzhambu.ai.application.config.prompt.service.impl;

import com.thundax.kuzhambu.ai.application.capability.service.AiCapabilityApplicationService;
import com.thundax.kuzhambu.ai.application.config.prompt.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.prompt.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.prompt.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.application.config.prompt.service.PromptApplicationService;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.ai.domain.config.service.PromptVariableDomainService;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class PromptApplicationServiceImpl implements PromptApplicationService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*}}");

    private final PromptRepository promptRepository;
    private final AiCapabilityApplicationService aiCapabilityApplicationService;
    private final PromptVariableDomainService promptVariableDomainService = new PromptVariableDomainService();

    public PromptApplicationServiceImpl(
            PromptRepository promptRepository, AiCapabilityApplicationService aiCapabilityApplicationService) {
        this.promptRepository = promptRepository;
        this.aiCapabilityApplicationService = aiCapabilityApplicationService;
    }

    @Override
    public PromptTemplate getTemplate(Long templateId) {
        return promptRepository.getTemplate(PromptTemplateIdCodec.toDomain(templateId));
    }

    @Override
    public PromptTemplate getTemplate(String capability) {
        if (isBlank(capability)) {
            return null;
        }
        return promptRepository.getTemplate(capability);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveTemplate(PromptTemplateSaveCommand command) {
        validateCommand(command);
        PromptTemplate template = command.toTemplate();
        PromptTemplateId templateId = saveOrUpdateTemplate(template);
        List<PromptVariable> variables = command.toVariables(templateId);
        ensureTemplateVariablesDefined(command.getMessageTemplatesJson(), variables);
        int versionNo = nextVersionNo(templateId);
        PromptVersion version = command.toVersion(templateId, versionNo, variablesSnapshotJson(command));
        promptRepository.replaceVariables(templateId, variables);
        promptRepository.saveVersion(version);
        promptRepository.markCurrentVersion(templateId, versionNo);
        aiCapabilityApplicationService.refreshActionStatusesByCapability(
                template.getCapability().value());
        return PromptTemplateIdCodec.toValue(templateId);
    }

    @Override
    public PromptVersionResult getCurrentVersion(Long templateId) {
        if (templateId == null) {
            return null;
        }
        return PromptVersionResult.from(promptRepository.getCurrentVersion(PromptTemplateIdCodec.toDomain(templateId)));
    }

    @Override
    public List<PromptVersionResult> listVersions(Long templateId) {
        List<PromptVersionResult> results = new ArrayList<>();
        if (templateId == null) {
            return results;
        }
        for (PromptVersion version : promptRepository.listVersions(PromptTemplateIdCodec.toDomain(templateId))) {
            results.add(PromptVersionResult.from(version));
        }
        return results;
    }

    @Override
    public List<PromptVersionResult> compareVersions(PromptVersionCompareQuery query) {
        if (query == null || query.getTemplateId() == null) {
            throw new BizException("Prompt version compare query can not be null");
        }
        List<PromptVersionResult> results = new ArrayList<>();
        results.add(PromptVersionResult.from(findVersion(query.getTemplateId(), query.getLeftVersionNo())));
        results.add(PromptVersionResult.from(findVersion(query.getTemplateId(), query.getRightVersionNo())));
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptVersionResult rollback(Long templateId, int versionNo) {
        findVersion(templateId, versionNo);
        PromptTemplateId id = PromptTemplateIdCodec.toDomain(templateId);
        int affectedRows = promptRepository.markCurrentVersion(id, versionNo);
        if (affectedRows <= 0) {
            throw new BizException("Prompt rollback failed: " + templateId + "#" + versionNo);
        }
        PromptTemplate template = promptRepository.getTemplate(id);
        if (template != null) {
            aiCapabilityApplicationService.refreshActionStatusesByCapability(
                    template.getCapability().value());
        }
        return getCurrentVersion(templateId);
    }

    @Override
    public List<PromptVariable> listVariables(Long templateId) {
        if (templateId == null) {
            return new ArrayList<>();
        }
        return promptRepository.listVariables(PromptTemplateIdCodec.toDomain(templateId));
    }

    @Override
    public void validateRequiredVariables(Long templateId, Collection<String> providedNames) {
        if (templateId == null) {
            throw new BizException("Prompt templateId is required");
        }
        List<String> missingNames = promptVariableDomainService.findMissingRequiredVariables(
                promptRepository.listVariables(PromptTemplateIdCodec.toDomain(templateId)), providedNames);
        if (!missingNames.isEmpty()) {
            throw new BizException("Prompt required variables are missing: " + missingNames);
        }
    }

    @Override
    public PromptVersionResult buildOptimizationSuggestion(Long templateId, String changeSummary) {
        PromptVersion current = promptRepository.getCurrentVersion(PromptTemplateIdCodec.toDomain(templateId));
        if (current == null) {
            return null;
        }
        current.setChangeSummary(changeSummary);
        return PromptVersionResult.from(current);
    }

    private PromptTemplateId saveOrUpdateTemplate(PromptTemplate template) {
        if (template.getId() == null) {
            return promptRepository.saveTemplate(template);
        }
        int affectedRows = promptRepository.updateTemplate(template);
        if (affectedRows <= 0) {
            throw new BizException("Prompt template update failed: " + template.getId());
        }
        return template.getId();
    }

    private int nextVersionNo(PromptTemplateId templateId) {
        PromptVersion current = promptRepository.getCurrentVersion(templateId);
        return current == null ? 1 : current.getVersionNo() + 1;
    }

    private PromptVersion findVersion(Long templateId, int versionNo) {
        if (templateId == null || versionNo <= 0) {
            throw new BizException("Prompt templateId and versionNo are required");
        }
        for (PromptVersion version : promptRepository.listVersions(PromptTemplateIdCodec.toDomain(templateId))) {
            if (version.getVersionNo() == versionNo) {
                return version;
            }
        }
        throw new BizException("Prompt version not found: " + templateId + "#" + versionNo);
    }

    private void validateCommand(PromptTemplateSaveCommand command) {
        if (command == null
                || isBlank(command.getCapability())
                || isBlank(command.getName())
                || isBlank(command.getMessageTemplatesJson())) {
            throw new BizException("Prompt capability, name and message templates are required");
        }
    }

    private void ensureTemplateVariablesDefined(String messageTemplatesJson, List<PromptVariable> variables) {
        Set<String> definedNames = new HashSet<>();
        for (PromptVariable variable : variables) {
            if (!isBlank(variable.getVariableName())) {
                definedNames.add(variable.getVariableName());
            }
        }
        for (String placeholder : extractPlaceholders(messageTemplatesJson)) {
            if (!definedNames.contains(placeholder)) {
                throw new BizException("Prompt variable is not defined: " + placeholder);
            }
        }
    }

    private String variablesSnapshotJson(PromptTemplateSaveCommand command) {
        if (!isBlank(command.getVariablesSnapshotJson())) {
            return command.getVariablesSnapshotJson();
        }
        List<PromptTemplateSaveCommand.VariableItem> variables = command.getVariables();
        if (variables == null || variables.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < variables.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            PromptTemplateSaveCommand.VariableItem variable = variables.get(i);
            if (variable == null) {
                builder.append("{}");
                continue;
            }
            builder.append('{')
                    .append("\"variableName\":\"")
                    .append(escapeJson(variable.getVariableName()))
                    .append("\",\"required\":")
                    .append(variable.isRequired())
                    .append(",\"description\":\"")
                    .append(escapeJson(variable.getDescription()))
                    .append("\",\"priority\":")
                    .append(variable.getPriority() == null ? i + 1 : variable.getPriority())
                    .append('}');
        }
        return builder.append(']').toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Set<String> extractPlaceholders(String text) {
        Set<String> placeholders = new HashSet<>();
        if (isBlank(text)) {
            return placeholders;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
