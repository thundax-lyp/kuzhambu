package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphExtractionAiCandidateIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.codec.GraphVersionIdCodec;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphExtractionTaskType;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.GraphVersionStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.model.enums.KnowledgeConfirmationStatus;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeGraphCandidateApplySupport {

    private static final String STATUS_AI_EXTRACTED = "AI_EXTRACTED";
    private static final String STATUS_MANUAL_CONFIRMED = "MANUAL_CONFIRMED";
    private static final String STATUS_APPLIED = "APPLIED";
    private static final String APPLY_MODE_APPEND = "APPEND";
    private static final String APPLY_MODE_MERGE = "MERGE";
    private static final String APPLY_MODE_OVERWRITE = "OVERWRITE";
    private static final int SOURCE_CATEGORY_CODE_MAX_LENGTH = 64;
    private static final int SOURCE_CATEGORY_NAME_MAX_LENGTH = 128;

    private final GraphVersionRepository graphVersionRepository;
    private final KnowledgeEntityRepository knowledgeEntityRepository;
    private final KnowledgeRelationRepository knowledgeRelationRepository;
    private final KnowledgeLineageNodeRepository knowledgeLineageNodeRepository;
    private final KnowledgeLineageRelationRepository knowledgeLineageRelationRepository;
    private final ObjectMapper objectMapper;

    public KnowledgeGraphCandidateApplySupport(
            GraphVersionRepository graphVersionRepository,
            KnowledgeEntityRepository knowledgeEntityRepository,
            KnowledgeRelationRepository knowledgeRelationRepository,
            KnowledgeLineageNodeRepository knowledgeLineageNodeRepository,
            KnowledgeLineageRelationRepository knowledgeLineageRelationRepository) {
        this.graphVersionRepository = graphVersionRepository;
        this.knowledgeEntityRepository = knowledgeEntityRepository;
        this.knowledgeRelationRepository = knowledgeRelationRepository;
        this.knowledgeLineageNodeRepository = knowledgeLineageNodeRepository;
        this.knowledgeLineageRelationRepository = knowledgeLineageRelationRepository;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public GraphVersion apply(GraphExtractionTask task, AiCandidateFacadeDto candidate) {
        return apply(task, candidate, APPLY_MODE_MERGE);
    }

    public GraphVersion apply(GraphExtractionTask task, AiCandidateFacadeDto candidate, String applyMode) {
        if (task == null || task.getId() == null || candidate == null || candidate.getCandidateId() == null) {
            throw new BizException("Knowledge graph apply target is incomplete");
        }
        if ("REJECTED".equals(candidate.getStatus())) {
            throw new BizException("Knowledge graph candidate has been rejected");
        }
        if (StringUtils.isBlank(candidate.getResultPayload())) {
            throw new BizException("Knowledge graph candidate payload is empty");
        }
        String resolvedApplyMode = normalizeApplyMode(applyMode);
        Instant appliedAt = Instant.now();
        GraphVersion version = ensureVersion(task, candidate, appliedAt);
        JsonNode payload = parsePayload(candidate.getResultPayload());
        if (APPLY_MODE_OVERWRITE.equals(resolvedApplyMode)) {
            clearVersionFacts(version, task);
        }
        if (GraphExtractionTaskType.LINEAGE.equals(task.getTaskType())) {
            applyLineageNodes(version, payload, appliedAt, resolvedApplyMode);
            applyLineageRelations(version, payload, appliedAt, resolvedApplyMode);
        } else if (GraphExtractionTaskType.RELATION.equals(task.getTaskType())
                || GraphExtractionTaskType.GRAPH.equals(task.getTaskType())) {
            applyEntities(version, payload, appliedAt, resolvedApplyMode);
            applyRelations(version, payload, appliedAt, resolvedApplyMode);
        } else {
            throw new BizException("Unsupported graph extraction task type: " + taskTypeValue(task));
        }
        return version;
    }

    private String normalizeApplyMode(String applyMode) {
        String normalized =
                StringUtils.defaultIfBlank(applyMode, APPLY_MODE_MERGE).trim().toUpperCase(Locale.ROOT);
        if (APPLY_MODE_MERGE.equals(normalized)
                || APPLY_MODE_APPEND.equals(normalized)
                || APPLY_MODE_OVERWRITE.equals(normalized)) {
            return normalized;
        }
        throw new BizException("Unsupported knowledge graph candidate apply mode: " + applyMode);
    }

    private void clearVersionFacts(GraphVersion version, GraphExtractionTask task) {
        if (version == null || version.getId() == null) {
            return;
        }
        if (GraphExtractionTaskType.LINEAGE.equals(task.getTaskType())) {
            Collection<String> nodeKeys = keys(
                    knowledgeLineageNodeRepository.listByVersionId(GraphVersionIdCodec.toValue(version.getId())),
                    KnowledgeLineageNode::getNodeKey);
            if (!nodeKeys.isEmpty()) {
                knowledgeLineageNodeRepository.deleteByNodeKeys(nodeKeys);
            }
            Collection<String> relationKeys = keys(
                    knowledgeLineageRelationRepository.listByVersionId(GraphVersionIdCodec.toValue(version.getId())),
                    KnowledgeLineageRelation::getRelationKey);
            if (!relationKeys.isEmpty()) {
                knowledgeLineageRelationRepository.deleteByRelationKeys(relationKeys);
            }
            return;
        }
        if (GraphExtractionTaskType.RELATION.equals(task.getTaskType())
                || GraphExtractionTaskType.GRAPH.equals(task.getTaskType())) {
            Collection<String> entityKeys =
                    keys(knowledgeEntityRepository.listByVersionId(version.getId()), KnowledgeEntity::getEntityKey);
            if (!entityKeys.isEmpty()) {
                knowledgeEntityRepository.deleteByEntityKeys(entityKeys);
            }
            Collection<String> relationKeys = keys(
                    knowledgeRelationRepository.listByVersionId(GraphVersionIdCodec.toValue(version.getId())),
                    KnowledgeRelation::getRelationKey);
            if (!relationKeys.isEmpty()) {
                knowledgeRelationRepository.deleteByRelationKeys(relationKeys);
            }
        }
    }

    private GraphVersion ensureVersion(GraphExtractionTask task, AiCandidateFacadeDto candidate, Instant appliedAt) {
        GraphVersion existing = graphVersionRepository.getByTaskCandidate(
                task.getId(), GraphExtractionAiCandidateIdCodec.toDomain(candidate.getCandidateId()));
        if (existing != null) {
            return existing;
        }
        GraphVersion latest = graphVersionRepository.findLatest(
                task.getTaskType(), task.getSourceContentType(), task.getSourceContentId());
        SourceCategory sourceCategory = resolveSourceCategory(task);
        GraphVersion version = new GraphVersion();
        version.setTaskId(task.getId());
        version.setCandidateId(GraphExtractionAiCandidateIdCodec.toDomain(candidate.getCandidateId()));
        version.setTaskType(task.getTaskType());
        version.setScopeType(task.getScopeType());
        version.setScopeJson(task.getScopeJson());
        version.setSourceContentType(task.getSourceContentType());
        version.setSourceContentId(task.getSourceContentId());
        version.setSourceCategoryCode(sourceCategory.code());
        version.setSourceCategoryName(sourceCategory.name());
        version.setVersionNo(latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1);
        version.setStatus(GraphVersionStatus.APPLIED);
        version.setAppliedAt(appliedAt);
        version.setId(graphVersionRepository.save(version));
        return version;
    }

    private SourceCategory resolveSourceCategory(GraphExtractionTask task) {
        JsonNode inputPayload = parseOptionalPayload(task.getInputPayloadJson());
        String categoryCode = firstNonBlank(inputPayload, "sourceCategoryCode", "categoryCode");
        JsonNode source = inputPayload == null ? null : inputPayload.get("source");
        if (StringUtils.isBlank(categoryCode)) {
            categoryCode = firstNonBlank(source, "sourceCategoryCode", "categoryCode");
        }
        String categoryName = firstNonBlank(inputPayload, "sourceCategoryName", "categoryName", "lineageHint");
        if (StringUtils.isBlank(categoryName)) {
            categoryName = firstNonBlank(source, "sourceCategoryName", "categoryName", "categoryPath");
        }
        if (StringUtils.isBlank(categoryName)) {
            categoryName = firstNonBlank(parseOptionalPayload(task.getScopeJson()), "sourceCategoryName", "sourcePath");
        }
        if (StringUtils.isBlank(categoryCode)) {
            categoryCode = StringUtils.defaultIfBlank(normalize(categoryName), task.getSourceContentType());
        }
        if (StringUtils.isBlank(categoryName)) {
            categoryName = StringUtils.defaultIfBlank(task.getSourceContentType(), "UNKNOWN");
        }
        return new SourceCategory(
                fit(categoryCode, SOURCE_CATEGORY_CODE_MAX_LENGTH), fit(categoryName, SOURCE_CATEGORY_NAME_MAX_LENGTH));
    }

    private String taskTypeValue(GraphExtractionTask task) {
        return task == null || task.getTaskType() == null
                ? null
                : task.getTaskType().value();
    }

    private void applyEntities(GraphVersion version, JsonNode payload, Instant appliedAt, String applyMode) {
        ArrayNode entityNodes = arrayOf(payload, "entities");
        String sourceRefsJson = sharedSourceRefsJson(payload);
        List<KnowledgeEntity> incoming = new ArrayList<>();
        for (JsonNode node : entityNodes) {
            String name = requiredText(node, "name");
            String entityType =
                    KnowledgeGraphEntityTypes.normalize(firstNonBlank(node, "entityType", "type", "category"));
            KnowledgeEntity entity = new KnowledgeEntity();
            entity.setEntityKey(textKey(name));
            entity.setName(name);
            entity.setEntityType(entityType);
            entity.setDescription(optionalText(node, "description", "summary"));
            entity.setConfirmationStatus(KnowledgeConfirmationStatus.AI_EXTRACTED);
            entity.setLatestVersionId(version.getId());
            entity.setSourceRefsJson(sourceRefsJson);
            entity.setFirstExtractedAt(appliedAt);
            entity.setLastExtractedAt(appliedAt);
            incoming.add(entity);
        }
        mergeEntities(incoming, appliedAt, applyMode);
    }

    private void applyRelations(GraphVersion version, JsonNode payload, Instant appliedAt, String applyMode) {
        ArrayNode relationNodes = arrayOf(payload, "relations");
        String sourceRefsJson = sharedSourceRefsJson(payload);
        List<KnowledgeRelation> incoming = new ArrayList<>();
        for (JsonNode node : relationNodes) {
            String sourceName = firstNonBlank(node, "subject", "source", "sourceName", "head", "from");
            String targetName = firstNonBlank(node, "object", "target", "targetName", "tail", "to");
            String relationType = firstNonBlank(node, "predicate", "relation", "relationType", "type", "label");
            if (StringUtils.isAnyBlank(sourceName, targetName, relationType)) {
                throw new BizException("Knowledge relation is incomplete");
            }
            KnowledgeRelation relation = new KnowledgeRelation();
            relation.setSourceEntityKey(textKey(sourceName));
            relation.setTargetEntityKey(textKey(targetName));
            relation.setRelationKey(relationKey(sourceName, relationType, targetName));
            relation.setSourceName(sourceName);
            relation.setTargetName(targetName);
            relation.setRelationType(relationType);
            relation.setEvidence(optionalText(node, "evidence", "summary"));
            relation.setConfirmationStatus(STATUS_AI_EXTRACTED);
            relation.setLatestVersionId(GraphVersionIdCodec.toValue(version.getId()));
            relation.setSourceRefsJson(sourceRefsJson);
            relation.setFirstExtractedAt(appliedAt);
            relation.setLastExtractedAt(appliedAt);
            incoming.add(relation);
        }
        mergeRelations(incoming, appliedAt, applyMode);
    }

    private void applyLineageNodes(GraphVersion version, JsonNode payload, Instant appliedAt, String applyMode) {
        ArrayNode nodeArray = arrayOf(payload, "nodes");
        String sourceRefsJson = sharedSourceRefsJson(payload);
        List<KnowledgeLineageNode> incoming = new ArrayList<>();
        for (JsonNode node : nodeArray) {
            String name = requiredText(node, "name");
            String nodeType = firstNonBlank(node, "nodeType", "type", "category");
            if (StringUtils.isBlank(nodeType)) {
                throw new BizException("Knowledge lineage node type is required");
            }
            KnowledgeLineageNode lineageNode = new KnowledgeLineageNode();
            lineageNode.setNodeKey(textKey(name));
            lineageNode.setName(name);
            lineageNode.setNodeType(nodeType);
            lineageNode.setGeneration(integerValue(node.get("generation")));
            lineageNode.setGender(optionalText(node, "gender"));
            lineageNode.setConfirmationStatus(STATUS_AI_EXTRACTED);
            lineageNode.setLatestVersionId(GraphVersionIdCodec.toValue(version.getId()));
            lineageNode.setSourceRefsJson(sourceRefsJson);
            lineageNode.setFirstExtractedAt(appliedAt);
            lineageNode.setLastExtractedAt(appliedAt);
            incoming.add(lineageNode);
        }
        mergeLineageNodes(incoming, appliedAt, applyMode);
    }

    private void applyLineageRelations(GraphVersion version, JsonNode payload, Instant appliedAt, String applyMode) {
        ArrayNode relationNodes = arrayOf(payload, "relations");
        String sourceRefsJson = sharedSourceRefsJson(payload);
        List<KnowledgeLineageRelation> incoming = new ArrayList<>();
        for (JsonNode node : relationNodes) {
            String sourceName = firstNonBlank(node, "subject", "source", "sourceName", "head", "from");
            String targetName = firstNonBlank(node, "object", "target", "targetName", "tail", "to");
            String relationType = firstNonBlank(node, "predicate", "relation", "relationType", "type", "label");
            if (StringUtils.isAnyBlank(sourceName, targetName, relationType)) {
                throw new BizException("Knowledge lineage relation is incomplete");
            }
            KnowledgeLineageRelation relation = new KnowledgeLineageRelation();
            relation.setSourceNodeKey(textKey(sourceName));
            relation.setTargetNodeKey(textKey(targetName));
            relation.setRelationKey(relationKey(sourceName, relationType, targetName));
            relation.setSourceName(sourceName);
            relation.setTargetName(targetName);
            relation.setRelationType(relationType);
            relation.setEvidence(optionalText(node, "evidence", "summary"));
            relation.setConfirmationStatus(STATUS_AI_EXTRACTED);
            relation.setLatestVersionId(GraphVersionIdCodec.toValue(version.getId()));
            relation.setSourceRefsJson(sourceRefsJson);
            relation.setFirstExtractedAt(appliedAt);
            relation.setLastExtractedAt(appliedAt);
            incoming.add(relation);
        }
        mergeLineageRelations(incoming, appliedAt, applyMode);
    }

    private void mergeEntities(List<KnowledgeEntity> incoming, Instant appliedAt, String applyMode) {
        Map<String, KnowledgeEntity> existingByKey = mapByKey(
                knowledgeEntityRepository.listByEntityKeys(keys(incoming, KnowledgeEntity::getEntityKey)),
                KnowledgeEntity::getEntityKey);
        if (APPLY_MODE_APPEND.equals(applyMode)) {
            knowledgeEntityRepository.saveOrUpdateBatch(incoming.stream()
                    .filter(entity -> !existingByKey.containsKey(entity.getEntityKey()))
                    .toList());
            return;
        }
        if (APPLY_MODE_OVERWRITE.equals(applyMode)) {
            knowledgeEntityRepository.saveOrUpdateBatch(incoming);
            return;
        }
        for (KnowledgeEntity entity : incoming) {
            KnowledgeEntity existing = existingByKey.get(entity.getEntityKey());
            if (existing == null) {
                continue;
            }
            entity.setId(existing.getId());
            entity.setFirstExtractedAt(
                    existing.getFirstExtractedAt() == null ? appliedAt : existing.getFirstExtractedAt());
            entity.setConfirmedAt(existing.getConfirmedAt());
            entity.setSourceRefsJson(mergeJsonArrays(existing.getSourceRefsJson(), entity.getSourceRefsJson()));
            if (KnowledgeConfirmationStatus.MANUAL_CONFIRMED.equals(existing.getConfirmationStatus())) {
                entity.setConfirmationStatus(existing.getConfirmationStatus());
                entity.setDescription(StringUtils.defaultIfBlank(existing.getDescription(), entity.getDescription()));
            }
        }
        knowledgeEntityRepository.saveOrUpdateBatch(incoming);
    }

    private void mergeRelations(List<KnowledgeRelation> incoming, Instant appliedAt, String applyMode) {
        Map<String, KnowledgeRelation> existingByKey = mapByKey(
                knowledgeRelationRepository.listByRelationKeys(keys(incoming, KnowledgeRelation::getRelationKey)),
                KnowledgeRelation::getRelationKey);
        if (APPLY_MODE_APPEND.equals(applyMode)) {
            knowledgeRelationRepository.saveOrUpdateBatch(incoming.stream()
                    .filter(relation -> !existingByKey.containsKey(relation.getRelationKey()))
                    .toList());
            return;
        }
        if (APPLY_MODE_OVERWRITE.equals(applyMode)) {
            knowledgeRelationRepository.saveOrUpdateBatch(incoming);
            return;
        }
        for (KnowledgeRelation relation : incoming) {
            KnowledgeRelation existing = existingByKey.get(relation.getRelationKey());
            if (existing == null) {
                continue;
            }
            relation.setId(existing.getId());
            relation.setFirstExtractedAt(
                    existing.getFirstExtractedAt() == null ? appliedAt : existing.getFirstExtractedAt());
            relation.setConfirmedAt(existing.getConfirmedAt());
            relation.setSourceRefsJson(mergeJsonArrays(existing.getSourceRefsJson(), relation.getSourceRefsJson()));
            if (STATUS_MANUAL_CONFIRMED.equals(existing.getConfirmationStatus())) {
                relation.setConfirmationStatus(existing.getConfirmationStatus());
                relation.setEvidence(StringUtils.defaultIfBlank(existing.getEvidence(), relation.getEvidence()));
            }
        }
        knowledgeRelationRepository.saveOrUpdateBatch(incoming);
    }

    private void mergeLineageNodes(List<KnowledgeLineageNode> incoming, Instant appliedAt, String applyMode) {
        Map<String, KnowledgeLineageNode> existingByKey = mapByKey(
                knowledgeLineageNodeRepository.listByNodeKeys(keys(incoming, KnowledgeLineageNode::getNodeKey)),
                KnowledgeLineageNode::getNodeKey);
        if (APPLY_MODE_APPEND.equals(applyMode)) {
            knowledgeLineageNodeRepository.saveOrUpdateBatch(incoming.stream()
                    .filter(node -> !existingByKey.containsKey(node.getNodeKey()))
                    .toList());
            return;
        }
        if (APPLY_MODE_OVERWRITE.equals(applyMode)) {
            knowledgeLineageNodeRepository.saveOrUpdateBatch(incoming);
            return;
        }
        for (KnowledgeLineageNode node : incoming) {
            KnowledgeLineageNode existing = existingByKey.get(node.getNodeKey());
            if (existing == null) {
                continue;
            }
            node.setId(existing.getId());
            node.setFirstExtractedAt(
                    existing.getFirstExtractedAt() == null ? appliedAt : existing.getFirstExtractedAt());
            node.setConfirmedAt(existing.getConfirmedAt());
            node.setSourceRefsJson(mergeJsonArrays(existing.getSourceRefsJson(), node.getSourceRefsJson()));
            if (STATUS_MANUAL_CONFIRMED.equals(existing.getConfirmationStatus())) {
                node.setConfirmationStatus(existing.getConfirmationStatus());
            }
        }
        knowledgeLineageNodeRepository.saveOrUpdateBatch(incoming);
    }

    private void mergeLineageRelations(List<KnowledgeLineageRelation> incoming, Instant appliedAt, String applyMode) {
        Map<String, KnowledgeLineageRelation> existingByKey = mapByKey(
                knowledgeLineageRelationRepository.listByRelationKeys(
                        keys(incoming, KnowledgeLineageRelation::getRelationKey)),
                KnowledgeLineageRelation::getRelationKey);
        if (APPLY_MODE_APPEND.equals(applyMode)) {
            knowledgeLineageRelationRepository.saveOrUpdateBatch(incoming.stream()
                    .filter(relation -> !existingByKey.containsKey(relation.getRelationKey()))
                    .toList());
            return;
        }
        if (APPLY_MODE_OVERWRITE.equals(applyMode)) {
            knowledgeLineageRelationRepository.saveOrUpdateBatch(incoming);
            return;
        }
        for (KnowledgeLineageRelation relation : incoming) {
            KnowledgeLineageRelation existing = existingByKey.get(relation.getRelationKey());
            if (existing == null) {
                continue;
            }
            relation.setId(existing.getId());
            relation.setFirstExtractedAt(
                    existing.getFirstExtractedAt() == null ? appliedAt : existing.getFirstExtractedAt());
            relation.setConfirmedAt(existing.getConfirmedAt());
            relation.setSourceRefsJson(mergeJsonArrays(existing.getSourceRefsJson(), relation.getSourceRefsJson()));
            if (STATUS_MANUAL_CONFIRMED.equals(existing.getConfirmationStatus())) {
                relation.setConfirmationStatus(existing.getConfirmationStatus());
                relation.setEvidence(StringUtils.defaultIfBlank(existing.getEvidence(), relation.getEvidence()));
            }
        }
        knowledgeLineageRelationRepository.saveOrUpdateBatch(incoming);
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new BizException("Knowledge graph candidate payload is invalid JSON");
        }
    }

    private JsonNode parseOptionalPayload(String payload) {
        if (StringUtils.isBlank(payload)) {
            return null;
        }
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            return null;
        }
    }

    private ArrayNode arrayOf(JsonNode payload, String fieldName) {
        JsonNode node = payload == null ? null : payload.get(fieldName);
        return node instanceof ArrayNode arrayNode ? arrayNode : objectMapper.createArrayNode();
    }

    private String sharedSourceRefsJson(JsonNode payload) {
        ArrayNode entryRefs = arrayOf(payload, "entryRefs");
        if (!entryRefs.isEmpty()) {
            return entryRefs.toString();
        }
        ArrayNode snippets = arrayOf(payload, "sourceSnippets");
        if (!snippets.isEmpty()) {
            return snippets.toString();
        }
        ArrayNode spans = arrayOf(payload, "sourceSpans");
        return spans.isEmpty() ? "[]" : spans.toString();
    }

    private String mergeJsonArrays(String currentJson, String incomingJson) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        appendJsonArrayValues(values, currentJson);
        appendJsonArrayValues(values, incomingJson);
        ArrayNode merged = objectMapper.createArrayNode();
        for (String value : values) {
            try {
                merged.add(objectMapper.readTree(value));
            } catch (Exception ex) {
                merged.add(value);
            }
        }
        return merged.toString();
    }

    private void appendJsonArrayValues(LinkedHashSet<String> values, String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }
        JsonNode node = parsePayload(json);
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                values.add(item.toString());
            }
        }
    }

    private String requiredText(JsonNode node, String fieldName) {
        String value = firstNonBlank(node, fieldName);
        if (StringUtils.isBlank(value)) {
            throw new BizException("Knowledge graph payload field is required: " + fieldName);
        }
        return value;
    }

    private String optionalText(JsonNode node, String... fieldNames) {
        return firstNonBlank(node, fieldNames);
    }

    private String firstNonBlank(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node == null ? null : node.get(fieldName);
            if (value != null && StringUtils.isNotBlank(value.asText())) {
                return value.asText().trim();
            }
        }
        return null;
    }

    private Integer integerValue(JsonNode node) {
        return node == null || node.isNull() ? null : node.asInt();
    }

    private String textKey(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalize(text).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new BizException("SHA-256 algorithm is not available");
        }
    }

    private String relationKey(String sourceText, String predicateText, String targetText) {
        return textKey(String.join("|", sourceText, predicateText, targetText));
    }

    private String normalize(String value) {
        return StringUtils.defaultString(value).trim().replaceAll("\\s+", "_").toLowerCase(Locale.ROOT);
    }

    private String fit(String value, int maxLength) {
        String normalized = StringUtils.defaultString(value).trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    private <T> Map<String, T> mapByKey(List<T> items, KeyExtractor<T> keyExtractor) {
        Map<String, T> map = new LinkedHashMap<>();
        for (T item : items == null ? List.<T>of() : items) {
            map.put(keyExtractor.keyOf(item), item);
        }
        return map;
    }

    private <T> Collection<String> keys(List<T> items, KeyExtractor<T> keyExtractor) {
        return items == null
                ? List.of()
                : items.stream().map(keyExtractor::keyOf).toList();
    }

    @FunctionalInterface
    private interface KeyExtractor<T> {
        String keyOf(T value);
    }

    private record SourceCategory(String code, String name) {}
}
