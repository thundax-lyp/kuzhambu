package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.config.command.BuildPromptOptimizationSuggestionCommand;
import com.thundax.kuzhambu.ai.application.config.command.ChangePromptTemplateStatusCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeletePromptTemplateCommand;
import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.command.RollbackPromptVersionCommand;
import com.thundax.kuzhambu.ai.application.config.command.ValidatePromptVariablesCommand;
import com.thundax.kuzhambu.ai.application.config.query.GetCurrentPromptVersionQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetPromptByCapabilityQuery;
import com.thundax.kuzhambu.ai.application.config.query.GetPromptQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptVariablesQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptVersionsQuery;
import com.thundax.kuzhambu.ai.application.config.query.ListPromptsQuery;
import com.thundax.kuzhambu.ai.application.config.query.PromptVersionCompareQuery;
import com.thundax.kuzhambu.ai.application.config.result.PromptCapabilityVariableResult;
import com.thundax.kuzhambu.ai.application.config.result.PromptVersionResult;
import com.thundax.kuzhambu.ai.application.config.service.PromptApplicationService;
import com.thundax.kuzhambu.ai.application.config.support.PromptCapabilityVariableCatalog;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.model.valueobject.PromptTemplateId;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.exception.BizExceptionBoundary;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class PromptApplicationServiceImpl implements PromptApplicationService {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*}}");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PromptRepository promptRepository;

    public PromptApplicationServiceImpl(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    @Override
    public PromptTemplate get(GetPromptQuery query) {
        return promptRepository.get(query == null ? null : query.getTemplateId());
    }

    @Override
    public PromptTemplate getByCapability(GetPromptByCapabilityQuery query) {
        AiBusinessCapability capability = query == null ? null : query.getCapability();
        if (capability == null) {
            return null;
        }
        return promptRepository.get(capability);
    }

    @Override
    public List<PromptTemplate> list(ListPromptsQuery query) {
        return promptRepository.list(
                query == null ? null : query.getCapability(), query == null ? null : query.getEnabled());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplateId save(PromptTemplateSaveCommand command) {
        validateCommand(command);
        PromptTemplate template = toTemplate(command);
        PromptTemplateId templateId = saveOrUpdateTemplate(template);
        List<PromptVariable> variables = toVariables(command, templateId);
        normalizeCapabilityVariables(command.getCapability(), variables);
        ensureTemplateVariablesDefined(command.getMessageTemplatesJson(), variables);
        ensureRequiredCapabilityVariablesPresent(command.getCapability(), command.getMessageTemplatesJson(), variables);
        int versionNo = nextVersionNo(templateId);
        PromptVersion version = toVersion(command, templateId, versionNo, variablesSnapshotJson(variables));
        replaceVariablesOnCreate(template, templateId, variables);
        insertVersion(version);
        promptRepository.markCurrentVersion(templateId, versionNo);
        return templateId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(ChangePromptTemplateStatusCommand command) {
        PromptTemplate template = getRequiredTemplate(command == null ? null : command.templateId());
        template.setEnabled(command.enabled());
        updateTemplate(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeletePromptTemplateCommand command) {
        PromptTemplate template = getRequiredTemplate(command == null ? null : command.templateId());
        template.setEnabled(false);
        updateTemplate(template);
    }

    @Override
    public PromptVersionResult getCurrentVersion(GetCurrentPromptVersionQuery query) {
        PromptTemplateId templateId = query == null ? null : query.getTemplateId();
        if (templateId == null) {
            return null;
        }
        return PromptVersionResult.from(promptRepository.getCurrentVersion(templateId));
    }

    @Override
    public List<PromptVersionResult> listVersions(ListPromptVersionsQuery query) {
        List<PromptVersionResult> results = new ArrayList<>();
        PromptTemplateId templateId = query == null ? null : query.getTemplateId();
        if (templateId == null) {
            return results;
        }
        for (PromptVersion version : promptRepository.listVersions(templateId)) {
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
    public PromptVersionResult rollback(RollbackPromptVersionCommand command) {
        PromptTemplateId templateId = command == null ? null : command.getTemplateId();
        int versionNo = command == null ? 0 : command.getVersionNo();
        findVersion(templateId, versionNo);
        int affectedRows = promptRepository.markCurrentVersion(templateId, versionNo);
        if (affectedRows <= 0) {
            throw new BizException("Prompt rollback failed: " + templateId.value() + "#" + versionNo);
        }
        return getCurrentVersion(new GetCurrentPromptVersionQuery(templateId));
    }

    @Override
    public List<PromptVariable> listVariables(ListPromptVariablesQuery query) {
        PromptTemplateId templateId = query == null ? null : query.getTemplateId();
        if (templateId == null) {
            return new ArrayList<>();
        }
        return promptRepository.listVariables(templateId);
    }

    @Override
    public void validateRequiredVariables(ValidatePromptVariablesCommand command) {
        PromptTemplateId templateId = command == null ? null : command.getTemplateId();
        Collection<String> providedNames = command == null ? null : command.getProvidedNames();
        if (templateId == null) {
            throw new BizException("Prompt templateId is required");
        }
        List<String> missingNames =
                PromptVariable.findMissingRequiredVariables(promptRepository.listVariables(templateId), providedNames);
        if (!missingNames.isEmpty()) {
            throw new BizException("Prompt required variables are missing: " + missingNames);
        }
    }

    @Override
    public PromptVersionResult buildOptimizationSuggestion(BuildPromptOptimizationSuggestionCommand command) {
        PromptTemplateId templateId = command == null ? null : command.getTemplateId();
        PromptVersion current = promptRepository.getCurrentVersion(templateId);
        if (current == null) {
            return null;
        }
        current.setChangeSummary(command == null ? null : command.getChangeSummary());
        return PromptVersionResult.from(current);
    }

    private PromptTemplate toTemplate(PromptTemplateSaveCommand command) {
        PromptTemplate template = new PromptTemplate();
        template.setId(command.getId());
        template.setCapability(command.getCapability());
        template.setName(command.getName());
        template.setDescription(command.getDescription());
        template.setEnabled(command.isEnabled());
        template.setRegisteredAt(Instant.now());
        return template;
    }

    private PromptVersion toVersion(
            PromptTemplateSaveCommand command,
            PromptTemplateId effectiveTemplateId,
            int versionNo,
            String variablesSnapshotJson) {
        PromptVersion version = new PromptVersion();
        version.setTemplateId(effectiveTemplateId);
        version.setVersionNo(versionNo);
        version.setMessageTemplatesJson(command.getMessageTemplatesJson());
        version.setVariablesSnapshotJson(variablesSnapshotJson);
        version.setOutputSchemaJson(command.getOutputSchemaJson());
        version.setChangeSummary(command.getChangeSummary());
        version.setRegisteredAt(Instant.now());
        return version;
    }

    private List<PromptVariable> toVariables(PromptTemplateSaveCommand command, PromptTemplateId effectiveTemplateId) {
        List<PromptVariable> promptVariables = new ArrayList<>();
        List<PromptTemplateSaveCommand.VariableItem> items = command.getVariables();
        if ((items == null || items.isEmpty()) && !isBlank(command.getVariablesSnapshotJson())) {
            items = parseVariableSnapshot(command.getVariablesSnapshotJson());
        }
        if (items == null) {
            return promptVariables;
        }
        for (int i = 0; i < items.size(); i++) {
            PromptTemplateSaveCommand.VariableItem item = items.get(i);
            if (item == null) {
                continue;
            }
            PromptVariable variable = new PromptVariable();
            variable.setTemplateId(effectiveTemplateId);
            variable.setVariableName(item.getVariableName());
            variable.setRequired(item.isRequired());
            variable.setDescription(item.getDescription());
            variable.setPriority(item.getPriority() == null ? i + 1 : item.getPriority());
            promptVariables.add(variable);
        }
        return promptVariables;
    }

    private List<PromptTemplateSaveCommand.VariableItem> parseVariableSnapshot(String snapshotJson) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(snapshotJson);
            if (root == null || !root.isArray()) {
                throw new BizException("Prompt variables snapshot must be a JSON array");
            }
            List<PromptTemplateSaveCommand.VariableItem> items = new ArrayList<>();
            for (JsonNode node : root) {
                PromptTemplateSaveCommand.VariableItem item = new PromptTemplateSaveCommand.VariableItem();
                item.setVariableName(node.path("variableName").asText(null));
                item.setRequired(node.path("required").asBoolean(true));
                item.setDescription(node.path("description").asText(null));
                item.setPriority(
                        node.hasNonNull("priority") ? node.get("priority").asInt() : null);
                items.add(item);
            }
            return items;
        } catch (JsonProcessingException ex) {
            throw new BizException("Prompt variables snapshot is invalid JSON");
        }
    }

    private PromptTemplateId saveOrUpdateTemplate(PromptTemplate template) {
        if (template.getId() == null) {
            return promptRepository.insertTemplate(template);
        }
        validateImmutableCapability(template);
        int affectedRows = promptRepository.updateTemplate(template);
        if (affectedRows <= 0) {
            throw new BizException("Prompt template update failed: " + template.getId());
        }
        return template.getId();
    }

    private PromptTemplate getRequiredTemplate(PromptTemplateId templateId) {
        if (templateId == null) {
            throw new BizException("Prompt templateId is required");
        }
        PromptTemplate template = promptRepository.get(templateId);
        if (template == null) {
            throw new BizException("Prompt template not found: " + PromptTemplateIdCodec.toValue(templateId));
        }
        return template;
    }

    private void updateTemplate(PromptTemplate template) {
        if (promptRepository.updateTemplate(template) <= 0) {
            throw new BizException("Prompt template update failed: " + PromptTemplateIdCodec.toValue(template.getId()));
        }
    }

    private void validateImmutableCapability(PromptTemplate template) {
        PromptTemplate existing = promptRepository.get(template.getId());
        if (existing == null) {
            throw new BizException("Prompt template not found: " + PromptTemplateIdCodec.toValue(template.getId()));
        }
        if (existing.getCapability() != template.getCapability()) {
            throw new BizException("Prompt template capability can not be changed: "
                    + PromptTemplateIdCodec.toValue(template.getId()));
        }
    }

    private void replaceVariablesOnCreate(
            PromptTemplate template, PromptTemplateId templateId, List<PromptVariable> variables) {
        if (template.getId() == null) {
            promptRepository.replaceVariables(templateId, variables);
        }
    }

    private int nextVersionNo(PromptTemplateId templateId) {
        PromptVersion current = promptRepository.getCurrentVersion(templateId);
        return current == null ? 1 : current.getVersionNo() + 1;
    }

    private void insertVersion(PromptVersion version) {
        try {
            promptRepository.insertVersion(version);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException("Prompt version conflict, please retry: "
                    + version.getTemplateId().value() + "#" + version.getVersionNo());
        }
    }

    private PromptVersion findVersion(PromptTemplateId templateId, int versionNo) {
        if (templateId == null || versionNo <= 0) {
            throw new BizException("Prompt templateId and versionNo are required");
        }
        for (PromptVersion version : promptRepository.listVersions(templateId)) {
            if (version.getVersionNo() == versionNo) {
                return version;
            }
        }
        throw new BizException("Prompt version not found: " + templateId.value() + "#" + versionNo);
    }

    private void validateCommand(PromptTemplateSaveCommand command) {
        if (command == null
                || command.getCapability() == null
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

    private void normalizeCapabilityVariables(AiBusinessCapability capability, List<PromptVariable> variables) {
        Map<String, PromptCapabilityVariableResult> variableByName = PromptCapabilityVariableCatalog.byName(capability);
        if (variableByName.isEmpty()) {
            throw new BizException("Prompt capability variable catalog is not configured: " + capability);
        }
        for (PromptVariable variable : variables) {
            PromptCapabilityVariableResult definition = variableByName.get(variable.getVariableName());
            if (definition == null) {
                throw new BizException("Prompt variable is not supported by capability: " + variable.getVariableName());
            }
            variable.setRequired(definition.required());
            variable.setDescription(definition.description());
        }
    }

    private void ensureRequiredCapabilityVariablesPresent(
            AiBusinessCapability capability, String messageTemplatesJson, List<PromptVariable> variables) {
        Set<String> definedNames = new HashSet<>();
        for (PromptVariable variable : variables) {
            if (!isBlank(variable.getVariableName())) {
                definedNames.add(variable.getVariableName());
            }
        }
        Set<String> placeholders = extractPlaceholders(messageTemplatesJson);
        List<String> missingNames = PromptCapabilityVariableCatalog.list(capability).stream()
                .filter(PromptCapabilityVariableResult::required)
                .map(PromptCapabilityVariableResult::variableName)
                .filter(name -> !definedNames.contains(name) || !placeholders.contains(name))
                .toList();
        if (!missingNames.isEmpty()) {
            throw new BizException("Prompt required capability variables are missing: " + missingNames);
        }
    }

    private String variablesSnapshotJson(List<PromptVariable> variables) {
        if (variables == null || variables.isEmpty()) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < variables.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            PromptVariable variable = variables.get(i);
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
                    .append(variable.getPriority())
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
