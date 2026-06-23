package com.thundax.kuzhambu.knowledge.application.graph.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.thundax.kuzhambu.ai.domain.invocation.model.entity.AiCandidate;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphExtractionTask;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.GraphVersion;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeEntity;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageNode;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeLineageRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.model.entity.KnowledgeRelation;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.GraphVersionRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeEntityRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageNodeRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeLineageRelationRepository;
import com.thundax.kuzhambu.knowledge.domain.graph.repository.KnowledgeRelationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
    private static final String TASK_TYPE_RELATION = "RELATION";
    private static final String TASK_TYPE_GRAPH = "GRAPH";
    private static final String TASK_TYPE_LINEAGE = "LINEAGE";

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

    public GraphVersion apply(GraphExtractionTask task, AiCandidate candidate) {
        if (task == null || task.getTaskId() == null || candidate == null || candidate.getCandidateId() == null) {
            throw new BizException("Knowledge graph apply target is incomplete");
        }
        if (StringUtils.isBlank(candidate.getResultPayload())) {
            throw new BizException("Knowledge graph candidate payload is empty");
        }
        Date appliedAt = Date.from(Instant.now());
        GraphVersion version = ensureVersion(task, candidate, appliedAt);
        JsonNode payload = parsePayload(candidate.getResultPayload());
        if (TASK_TYPE_LINEAGE.equals(task.getTaskType())) {
            applyLineageNodes(version, payload, appliedAt);
            applyLineageRelations(version, payload, appliedAt);
        } else if (TASK_TYPE_RELATION.equals(task.getTaskType()) || TASK_TYPE_GRAPH.equals(task.getTaskType())) {
            applyEntities(version, payload, appliedAt);
            applyRelations(version, payload, appliedAt);
        } else {
            throw new BizException("Unsupported graph extraction task type: " + task.getTaskType());
        }
        return version;
    }

    private GraphVersion ensureVersion(GraphExtractionTask task, AiCandidate candidate, Date appliedAt) {
        GraphVersion existing = graphVersionRepository.getByTaskCandidate(task.getTaskId(), candidate.getCandidateId());
        if (existing != null) {
            return existing;
        }
        GraphVersion latest = graphVersionRepository.findLatest(
                task.getTaskType(), task.getSourceContentType(), task.getSourceContentId());
        GraphVersion version = new GraphVersion();
        version.setTaskId(task.getTaskId());
        version.setCandidateId(candidate.getCandidateId());
        version.setTaskType(task.getTaskType());
        version.setScopeType(task.getScopeType());
        version.setScopeJson(task.getScopeJson());
        version.setSourceContentType(task.getSourceContentType());
        version.setSourceContentId(task.getSourceContentId());
        version.setVersionNo(latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1);
        version.setStatus(STATUS_APPLIED);
        version.setAppliedAt(appliedAt);
        version.setVersionId(graphVersionRepository.save(version));
        return version;
    }

    private void applyEntities(GraphVersion version, JsonNode payload, Date appliedAt) {
        ArrayNode entityNodes = arrayOf(payload, "entities");
        String sourceRefsJson = sharedSourceRefsJson(payload);
        List<KnowledgeEntity> incoming = new ArrayList<>();
        for (JsonNode node : entityNodes) {
            String name = requiredText(node, "name");
            String entityType = firstNonBlank(node, "entityType", "type", "category");
            if (StringUtils.isBlank(entityType)) {
                throw new BizException("Knowledge entity type is required");
            }
            KnowledgeEntity entity = new KnowledgeEntity();
            entity.setEntityKey(entityKey(name, entityType));
            entity.setName(name);
            entity.setEntityType(entityType);
            entity.setDescription(optionalText(node, "description", "summary"));
            entity.setConfirmationStatus(STATUS_AI_EXTRACTED);
            entity.setLatestVersionId(version.getVersionId());
            entity.setSourceRefsJson(sourceRefsJson);
            entity.setFirstExtractedAt(appliedAt);
            entity.setLastExtractedAt(appliedAt);
            incoming.add(entity);
        }
        mergeEntities(incoming, appliedAt);
    }

    private void applyRelations(GraphVersion version, JsonNode payload, Date appliedAt) {
        ArrayNode relationNodes = arrayOf(payload, "relations");
        String sourceRefsJson = sharedSourceRefsJson(payload);
        List<KnowledgeRelation> incoming = new ArrayList<>();
        for (JsonNode node : relationNodes) {
            String sourceName = firstNonBlank(node, "sourceName", "source");
            String targetName = firstNonBlank(node, "targetName", "target");
            String relationType = firstNonBlank(node, "relationType", "type", "label");
            if (StringUtils.isAnyBlank(sourceName, targetName, relationType)) {
                throw new BizException("Knowledge relation is incomplete");
            }
            KnowledgeRelation relation = new KnowledgeRelation();
            relation.setSourceEntityKey(entityKey(sourceName, "AUTO"));
            relation.setTargetEntityKey(entityKey(targetName, "AUTO"));
            relation.setRelationKey(
                    relationKey(relation.getSourceEntityKey(), relation.getTargetEntityKey(), relationType));
            relation.setSourceName(sourceName);
            relation.setTargetName(targetName);
            relation.setRelationType(relationType);
            relation.setEvidence(optionalText(node, "evidence", "summary"));
            relation.setConfirmationStatus(STATUS_AI_EXTRACTED);
            relation.setLatestVersionId(version.getVersionId());
            relation.setSourceRefsJson(sourceRefsJson);
            relation.setFirstExtractedAt(appliedAt);
            relation.setLastExtractedAt(appliedAt);
            incoming.add(relation);
        }
        mergeRelations(incoming, appliedAt);
    }

    private void applyLineageNodes(GraphVersion version, JsonNode payload, Date appliedAt) {
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
            lineageNode.setNodeKey(nodeKey(name, nodeType));
            lineageNode.setName(name);
            lineageNode.setNodeType(nodeType);
            lineageNode.setGeneration(integerValue(node.get("generation")));
            lineageNode.setGender(optionalText(node, "gender"));
            lineageNode.setConfirmationStatus(STATUS_AI_EXTRACTED);
            lineageNode.setLatestVersionId(version.getVersionId());
            lineageNode.setSourceRefsJson(sourceRefsJson);
            lineageNode.setFirstExtractedAt(appliedAt);
            lineageNode.setLastExtractedAt(appliedAt);
            incoming.add(lineageNode);
        }
        mergeLineageNodes(incoming, appliedAt);
    }

    private void applyLineageRelations(GraphVersion version, JsonNode payload, Date appliedAt) {
        ArrayNode relationNodes = arrayOf(payload, "relations");
        String sourceRefsJson = sharedSourceRefsJson(payload);
        List<KnowledgeLineageRelation> incoming = new ArrayList<>();
        for (JsonNode node : relationNodes) {
            String sourceName = firstNonBlank(node, "sourceName", "source");
            String targetName = firstNonBlank(node, "targetName", "target");
            String relationType = firstNonBlank(node, "relationType", "type", "label");
            if (StringUtils.isAnyBlank(sourceName, targetName, relationType)) {
                throw new BizException("Knowledge lineage relation is incomplete");
            }
            KnowledgeLineageRelation relation = new KnowledgeLineageRelation();
            relation.setSourceNodeKey(nodeKey(sourceName, "AUTO"));
            relation.setTargetNodeKey(nodeKey(targetName, "AUTO"));
            relation.setRelationKey(
                    relationKey(relation.getSourceNodeKey(), relation.getTargetNodeKey(), relationType));
            relation.setSourceName(sourceName);
            relation.setTargetName(targetName);
            relation.setRelationType(relationType);
            relation.setEvidence(optionalText(node, "evidence", "summary"));
            relation.setConfirmationStatus(STATUS_AI_EXTRACTED);
            relation.setLatestVersionId(version.getVersionId());
            relation.setSourceRefsJson(sourceRefsJson);
            relation.setFirstExtractedAt(appliedAt);
            relation.setLastExtractedAt(appliedAt);
            incoming.add(relation);
        }
        mergeLineageRelations(incoming, appliedAt);
    }

    private void mergeEntities(List<KnowledgeEntity> incoming, Date appliedAt) {
        Map<String, KnowledgeEntity> existingByKey = mapByKey(
                knowledgeEntityRepository.listByEntityKeys(keys(incoming, KnowledgeEntity::getEntityKey)),
                KnowledgeEntity::getEntityKey);
        for (KnowledgeEntity entity : incoming) {
            KnowledgeEntity existing = existingByKey.get(entity.getEntityKey());
            if (existing == null) {
                continue;
            }
            entity.setEntityId(existing.getEntityId());
            entity.setId(existing.getId());
            entity.setFirstExtractedAt(
                    existing.getFirstExtractedAt() == null ? appliedAt : existing.getFirstExtractedAt());
            entity.setConfirmedAt(existing.getConfirmedAt());
            entity.setSourceRefsJson(mergeJsonArrays(existing.getSourceRefsJson(), entity.getSourceRefsJson()));
            if (STATUS_MANUAL_CONFIRMED.equals(existing.getConfirmationStatus())) {
                entity.setConfirmationStatus(existing.getConfirmationStatus());
                entity.setDescription(StringUtils.defaultIfBlank(existing.getDescription(), entity.getDescription()));
            }
        }
        knowledgeEntityRepository.saveOrUpdateBatch(incoming);
    }

    private void mergeRelations(List<KnowledgeRelation> incoming, Date appliedAt) {
        Map<String, KnowledgeRelation> existingByKey = mapByKey(
                knowledgeRelationRepository.listByRelationKeys(keys(incoming, KnowledgeRelation::getRelationKey)),
                KnowledgeRelation::getRelationKey);
        for (KnowledgeRelation relation : incoming) {
            KnowledgeRelation existing = existingByKey.get(relation.getRelationKey());
            if (existing == null) {
                continue;
            }
            relation.setRelationId(existing.getRelationId());
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

    private void mergeLineageNodes(List<KnowledgeLineageNode> incoming, Date appliedAt) {
        Map<String, KnowledgeLineageNode> existingByKey = mapByKey(
                knowledgeLineageNodeRepository.listByNodeKeys(keys(incoming, KnowledgeLineageNode::getNodeKey)),
                KnowledgeLineageNode::getNodeKey);
        for (KnowledgeLineageNode node : incoming) {
            KnowledgeLineageNode existing = existingByKey.get(node.getNodeKey());
            if (existing == null) {
                continue;
            }
            node.setNodeId(existing.getNodeId());
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

    private void mergeLineageRelations(List<KnowledgeLineageRelation> incoming, Date appliedAt) {
        Map<String, KnowledgeLineageRelation> existingByKey = mapByKey(
                knowledgeLineageRelationRepository.listByRelationKeys(
                        keys(incoming, KnowledgeLineageRelation::getRelationKey)),
                KnowledgeLineageRelation::getRelationKey);
        for (KnowledgeLineageRelation relation : incoming) {
            KnowledgeLineageRelation existing = existingByKey.get(relation.getRelationKey());
            if (existing == null) {
                continue;
            }
            relation.setRelationId(existing.getRelationId());
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

    private String entityKey(String name, String entityType) {
        return normalize(entityType) + ":" + normalize(name);
    }

    private String nodeKey(String name, String nodeType) {
        return normalize(nodeType) + ":" + normalize(name);
    }

    private String relationKey(String sourceKey, String targetKey, String relationType) {
        return sourceKey + "->" + targetKey + ":" + normalize(relationType);
    }

    private String normalize(String value) {
        return StringUtils.defaultString(value).trim().replaceAll("\\s+", "_").toLowerCase(Locale.ROOT);
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
}
