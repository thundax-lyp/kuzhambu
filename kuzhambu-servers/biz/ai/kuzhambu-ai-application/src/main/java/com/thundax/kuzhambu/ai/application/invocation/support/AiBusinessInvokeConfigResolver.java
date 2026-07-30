package com.thundax.kuzhambu.ai.application.invocation.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thundax.kuzhambu.ai.application.config.query.ListAiBusinessConfigsQuery;
import com.thundax.kuzhambu.ai.application.config.service.AiBusinessConfigApplicationService;
import com.thundax.kuzhambu.ai.application.invocation.command.AiInvokeCommand;
import com.thundax.kuzhambu.ai.domain.config.codec.PromptTemplateIdCodec;
import com.thundax.kuzhambu.ai.domain.config.model.entity.AiBusinessConfig;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptTemplate;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVariable;
import com.thundax.kuzhambu.ai.domain.config.model.entity.PromptVersion;
import com.thundax.kuzhambu.ai.domain.config.model.enums.AiBusinessCapability;
import com.thundax.kuzhambu.ai.domain.config.repository.PromptRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class AiBusinessInvokeConfigResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*}}");

    private final AiBusinessConfigApplicationService businessConfigService;
    private final PromptRepository promptRepository;
    private final AiWorkerModelConfigResolver modelConfigResolver;
    private final ObjectMapper objectMapper;

    public AiBusinessInvokeConfigResolver(
            AiBusinessConfigApplicationService businessConfigService,
            PromptRepository promptRepository,
            AiWorkerModelConfigResolver modelConfigResolver,
            ObjectMapper objectMapper) {
        this.businessConfigService = businessConfigService;
        this.promptRepository = promptRepository;
        this.modelConfigResolver = modelConfigResolver;
        this.objectMapper = objectMapper;
    }

    public void resolve(AiInvokeCommand command) {
        if (command == null || command.getCapability() == null) {
            throw new BizException("AI business capability is required");
        }
        AiBusinessConfig config = resolveBusinessConfig(command.getCapability());
        PromptVersion promptVersion = resolveCurrentPromptVersion(config);
        ObjectNode inputPayload = withCommandMetadata(parseInputPayload(command.getInputPayloadJson()), command);
        List<PromptVariable> variables = resolvePromptVariables(config, promptVersion);
        ObjectNode promptVariables = buildPromptVariables(variables, inputPayload);

        command.setModelId(config.getModelId());
        command.setPromptVersionId(promptVersion.getId());
        command.setPromptMessagesJson(renderPromptMessages(promptVersion.getMessageTemplatesJson(), promptVariables));
        command.setPromptVariablesJson(toJson(promptVariables, "AI prompt variables is not valid JSON"));
        if (isBlank(command.getOutputSchemaJson())) {
            command.setOutputSchemaJson(promptVersion.getOutputSchemaJson());
        }

        var resolved = modelConfigResolver.resolve(command);
        command.setServiceId(resolved.serviceId());
        command.setServiceRole(resolved.serviceRole());
        command.setModelId(resolved.modelId());
        command.setModelName(resolved.modelName());
    }

    public void validatePromptVersionEnabled(AiInvokeCommand command) {
        if (command == null || command.getCapability() == null || command.getPromptVersionId() == null) {
            throw new BizException("AI prompt version is required");
        }
        PromptVersion promptVersion = promptRepository.getVersion(command.getPromptVersionId());
        if (promptVersion == null || promptVersion.getTemplateId() == null) {
            throw new BizException("AI prompt version is not configured: " + command.getPromptVersionId());
        }
        PromptTemplate promptTemplate = promptRepository.get(promptVersion.getTemplateId());
        AiBusinessCapability capability = command.getCapability();
        if (promptTemplate == null || !promptTemplate.isEnabled() || promptTemplate.getCapability() != capability) {
            throw new BizException("AI business config prompt template is disabled or mismatched: "
                    + PromptTemplateIdCodec.toValue(promptVersion.getTemplateId()));
        }
    }

    private AiBusinessConfig resolveBusinessConfig(AiBusinessCapability capability) {
        List<AiBusinessConfig> configs = businessConfigService.list(new ListAiBusinessConfigsQuery(capability, true));
        if (configs == null || configs.isEmpty()) {
            throw new BizException("AI business config is not configured: " + capability);
        }
        return configs.get(0);
    }

    private PromptVersion resolveCurrentPromptVersion(AiBusinessConfig config) {
        if (config == null || config.getPromptTemplateId() == null) {
            throw new BizException("AI business config prompt template is required");
        }
        PromptTemplate promptTemplate = promptRepository.get(config.getPromptTemplateId());
        if (!config.promptMatches(promptTemplate)) {
            throw new BizException("AI business config prompt template is disabled or mismatched: "
                    + PromptTemplateIdCodec.toValue(config.getPromptTemplateId()));
        }
        PromptVersion promptVersion = promptRepository.getCurrentVersion(config.getPromptTemplateId());
        if (promptVersion == null) {
            throw new BizException("AI prompt current version is not configured: "
                    + PromptTemplateIdCodec.toValue(config.getPromptTemplateId()));
        }
        return promptVersion;
    }

    private List<PromptVariable> resolvePromptVariables(AiBusinessConfig config, PromptVersion promptVersion) {
        String variablesSnapshotJson = promptVersion.getVariablesSnapshotJson();
        if (isBlank(variablesSnapshotJson)) {
            return promptRepository.listVariables(config.getPromptTemplateId());
        }
        try {
            JsonNode variablesSnapshot = objectMapper.readTree(variablesSnapshotJson);
            if (!variablesSnapshot.isArray()) {
                throw new BizException("AI prompt variables snapshot must be a JSON array");
            }
            List<PromptVariable> variables = new ArrayList<>();
            for (JsonNode variableSnapshot : variablesSnapshot) {
                if (variableSnapshot == null || !variableSnapshot.isObject()) {
                    continue;
                }
                PromptVariable variable = new PromptVariable();
                variable.setTemplateId(config.getPromptTemplateId());
                variable.setVariableName(variableNameValue(variableSnapshot));
                variable.setRequired(!variableSnapshot.has("required")
                        || variableSnapshot.get("required").asBoolean(true));
                variable.setDescription(textValue(variableSnapshot, "description"));
                variable.setPriority(variableSnapshot.path("priority").asInt(0));
                variables.add(variable);
            }
            return variables;
        } catch (JsonProcessingException ex) {
            throw new BizException("AI prompt variables snapshot is not valid JSON");
        }
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String variableNameValue(JsonNode node) {
        String variableName = textValue(node, "variableName");
        return isBlank(variableName) ? textValue(node, "name") : variableName;
    }

    private ObjectNode parseInputPayload(String inputPayloadJson) {
        if (isBlank(inputPayloadJson)) {
            throw new BizException("AI input payload is required");
        }
        try {
            JsonNode payload = objectMapper.readTree(inputPayloadJson);
            if (!payload.isObject()) {
                throw new BizException("AI input payload must be a JSON object");
            }
            return (ObjectNode) payload;
        } catch (JsonProcessingException ex) {
            throw new BizException("AI input payload is not valid JSON");
        }
    }

    private ObjectNode buildPromptVariables(List<PromptVariable> variables, ObjectNode inputPayload) {
        List<String> providedNames = new ArrayList<>();
        Iterator<String> fieldNames = inputPayload.fieldNames();
        while (fieldNames.hasNext()) {
            providedNames.add(fieldNames.next());
        }
        List<String> missingNames = PromptVariable.findMissingRequiredVariables(variables, providedNames);
        if (!missingNames.isEmpty()) {
            throw new BizException("Prompt required variables are missing: " + missingNames);
        }

        ObjectNode promptVariables = objectMapper.createObjectNode();
        if (variables == null || variables.isEmpty()) {
            promptVariables.setAll(inputPayload);
            return promptVariables;
        }
        for (PromptVariable variable : variables) {
            if (variable == null || isBlank(variable.getVariableName())) {
                continue;
            }
            JsonNode value = inputPayload.get(variable.getVariableName());
            promptVariables.set(
                    variable.getVariableName(), value == null || value.isNull() ? objectMapper.nullNode() : value);
        }
        return promptVariables;
    }

    private ObjectNode withCommandMetadata(ObjectNode inputPayload, AiInvokeCommand command) {
        ObjectNode payload = inputPayload.deepCopy();
        putIfAbsent(payload, "scope", command.getScope());
        putIfAbsent(payload, "capability", command.getCapability().value());
        putIfAbsent(
                payload,
                "contentType",
                command.getContentRef() == null ? null : command.getContentRef().contentType());
        putIfAbsent(
                payload,
                "contentId",
                command.getContentRef() == null ? null : command.getContentRef().contentId());
        putIfAbsent(
                payload,
                "objectId",
                command.getTargetObjectId() == null
                        ? null
                        : command.getTargetObjectId().value());
        putIfAbsent(payload, "locale", command.getLocale());
        return payload;
    }

    private void putIfAbsent(ObjectNode payload, String fieldName, String value) {
        if (isBlank(value) || payload.has(fieldName)) {
            return;
        }
        payload.put(fieldName, value);
    }

    private void putIfAbsent(ObjectNode payload, String fieldName, Long value) {
        if (value == null || payload.has(fieldName)) {
            return;
        }
        payload.put(fieldName, value);
    }

    private String renderPromptMessages(String messageTemplatesJson, ObjectNode variables) {
        if (isBlank(messageTemplatesJson)) {
            throw new BizException("AI prompt message templates are required");
        }
        try {
            JsonNode messageTemplates = objectMapper.readTree(messageTemplatesJson);
            return toJson(renderNode(messageTemplates, variables), "AI prompt messages is not valid JSON");
        } catch (JsonProcessingException ex) {
            throw new BizException("AI prompt message templates is not valid JSON");
        }
    }

    private JsonNode renderNode(JsonNode node, ObjectNode variables) {
        if (node == null || node.isNull()) {
            return objectMapper.nullNode();
        }
        if (node.isTextual()) {
            return objectMapper.getNodeFactory().textNode(renderText(node.asText(), variables));
        }
        if (node.isArray()) {
            ArrayNode array = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                array.add(renderNode(item, variables));
            }
            return array;
        }
        if (node.isObject()) {
            ObjectNode object = objectMapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = node.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                object.set(field.getKey(), renderNode(field.getValue(), variables));
            }
            return object;
        }
        return node.deepCopy();
    }

    private String renderText(String template, ObjectNode variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer rendered = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(renderValue(variables.get(matcher.group(1)))));
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    private String renderValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isTextual()) {
            return value.asText();
        }
        if (value.isValueNode()) {
            return value.asText();
        }
        return toJson(value, "AI prompt variable value is not valid JSON");
    }

    private String toJson(JsonNode node, String errorMessage) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new BizException(errorMessage);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
