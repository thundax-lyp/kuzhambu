package com.thundax.kuzhambu.ai.application.config.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.ai.application.config.command.BuildPromptOptimizationSuggestionCommand;
import com.thundax.kuzhambu.ai.application.config.command.ChangePromptTemplateStatusCommand;
import com.thundax.kuzhambu.ai.application.config.command.DeletePromptTemplateCommand;
import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateSaveCommand;
import com.thundax.kuzhambu.ai.application.config.command.PromptTemplateVariableItem;
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
        return promptRepository.getTemplateById(query == null ? null : query.templateId());
    }

    @Override
    public PromptTemplate getByCapability(GetPromptByCapabilityQuery query) {
        AiBusinessCapability capability = query == null ? null : query.capability();
        if (capability == null) {
            return null;
        }
        return promptRepository.getTemplateByCapability(capability);
    }

    @Override
    public List<PromptTemplate> list(ListPromptsQuery query) {
        return promptRepository.list(query == null ? null : query.capability(), query == null ? null : query.enabled());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptTemplateId save(PromptTemplateSaveCommand command) {
        validateCommand(command);
        PromptTemplate template = toTemplate(command);
        PromptTemplateId templateId = saveOrUpdateTemplate(template);
        List<PromptVariable> variables = toVariables(command, templateId);
        normalizeCapabilityVariables(command.capability(), variables);
        ensureTemplateVariablesDefined(command.messageTemplatesJson(), variables);
        ensureRequiredCapabilityVariablesPresent(command.capability(), command.messageTemplatesJson(), variables);
        int versionNo = nextVersionNo(templateId);
        PromptVersion version = toVersion(command, templateId, versionNo, variablesSnapshotJson(variables));
        replaceVariablesOnCreate(template, templateId, variables);
        insertVersion(version);
        promptRepository.updateCurrentVersion(templateId, versionNo);
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
        PromptTemplateId templateId = query == null ? null : query.templateId();
        if (templateId == null) {
            return null;
        }
        return PromptVersionResult.from(promptRepository.getCurrentVersionByTemplateId(templateId));
    }

    @Override
    public List<PromptVersionResult> listVersions(ListPromptVersionsQuery query) {
        List<PromptVersionResult> results = new ArrayList<>();
        PromptTemplateId templateId = query == null ? null : query.templateId();
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
        if (query == null || query.templateId() == null) {
            throw new BizException("Prompt version compare query can not be null");
        }
        List<PromptVersionResult> results = new ArrayList<>();
        results.add(PromptVersionResult.from(findVersion(query.templateId(), query.leftVersionNo())));
        results.add(PromptVersionResult.from(findVersion(query.templateId(), query.rightVersionNo())));
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PromptVersionResult rollback(RollbackPromptVersionCommand command) {
        PromptTemplateId templateId = command == null ? null : command.templateId();
        int versionNo = command == null ? 0 : command.versionNo();
        findVersion(templateId, versionNo);
        int affectedRows = promptRepository.updateCurrentVersion(templateId, versionNo);
        if (affectedRows <= 0) {
            throw new BizException("Prompt rollback failed: " + templateId.value() + "#" + versionNo);
        }
        return getCurrentVersion(new GetCurrentPromptVersionQuery(templateId));
    }

    @Override
    public List<PromptVariable> listVariables(ListPromptVariablesQuery query) {
        PromptTemplateId templateId = query == null ? null : query.templateId();
        if (templateId == null) {
            return new ArrayList<>();
        }
        return promptRepository.listVariables(templateId);
    }

    @Override
    public void validateRequiredVariables(ValidatePromptVariablesCommand command) {
        PromptTemplateId templateId = command == null ? null : command.templateId();
        Collection<String> providedNames = command == null ? null : command.providedNames();
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
        PromptTemplateId templateId = command == null ? null : command.templateId();
        PromptVersion current = promptRepository.getCurrentVersionByTemplateId(templateId);
        if (current == null) {
            return null;
        }
        current.setChangeSummary(command == null ? null : command.changeSummary());
        return PromptVersionResult.from(current);
    }

    private PromptTemplate toTemplate(PromptTemplateSaveCommand command) {
        PromptTemplate template = new PromptTemplate();
        template.setId(command.id());
        template.setCapability(command.capability());
        template.setName(command.name());
        template.setDescription(command.description());
        template.setEnabled(command.enabled());
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
        version.setMessageTemplatesJson(command.messageTemplatesJson());
        version.setVariablesSnapshotJson(variablesSnapshotJson);
        version.setOutputSchemaJson(command.outputSchemaJson());
        version.setChangeSummary(command.changeSummary());
        version.setRegisteredAt(Instant.now());
        return version;
    }

    private List<PromptVariable> toVariables(PromptTemplateSaveCommand command, PromptTemplateId effectiveTemplateId) {
        List<PromptVariable> promptVariables = new ArrayList<>();
        List<PromptTemplateVariableItem> items = command.variables();
        if ((items == null || items.isEmpty()) && !isBlank(command.variablesSnapshotJson())) {
            items = parseVariableSnapshot(command.variablesSnapshotJson());
        }
        if (items == null) {
            return promptVariables;
        }
        for (int i = 0; i < items.size(); i++) {
            PromptTemplateVariableItem item = items.get(i);
            if (item == null) {
                continue;
            }
            PromptVariable variable = new PromptVariable();
            variable.setTemplateId(effectiveTemplateId);
            variable.setVariableName(item.variableName());
            variable.setRequired(item.required());
            variable.setDescription(item.description());
            variable.setPriority(item.priority() == null ? i + 1 : item.priority());
            promptVariables.add(variable);
        }
        return promptVariables;
    }

    private List<PromptTemplateVariableItem> parseVariableSnapshot(String snapshotJson) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(snapshotJson);
            if (root == null || !root.isArray()) {
                throw new BizException("Prompt variables snapshot must be a JSON array");
            }
            List<PromptTemplateVariableItem> items = new ArrayList<>();
            for (JsonNode node : root) {
                items.add(new PromptTemplateVariableItem(
                        node.path("variableName").asText(null),
                        node.path("required").asBoolean(true),
                        node.path("description").asText(null),
                        node.hasNonNull("priority") ? node.get("priority").asInt() : null));
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
        PromptTemplate template = promptRepository.getTemplateById(templateId);
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
        PromptTemplate existing = promptRepository.getTemplateById(template.getId());
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
            promptRepository.replaceTemplateVariables(templateId, variables);
        }
    }

    private int nextVersionNo(PromptTemplateId templateId) {
        PromptVersion current = promptRepository.getCurrentVersionByTemplateId(templateId);
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
                || command.capability() == null
                || isBlank(command.name())
                || isBlank(command.messageTemplatesJson())) {
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
