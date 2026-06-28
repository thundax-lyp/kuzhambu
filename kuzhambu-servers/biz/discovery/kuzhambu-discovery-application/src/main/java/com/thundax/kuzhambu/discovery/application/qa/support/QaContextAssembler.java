package com.thundax.kuzhambu.discovery.application.qa.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.discovery.application.search.result.QueryUnderstandingResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class QaContextAssembler {

    private static final int MAX_SOURCE_COUNT = 5;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QaContext assemble(
            String question,
            QueryUnderstandingResult understandingResult,
            List<ClassicsPublicContentFacadeDto> publicContents) {
        String normalizedQuestion = normalizeQuestion(question);
        String rewrittenQuestion = understandingResult == null
                ? normalizedQuestion
                : StringUtils.defaultIfBlank(understandingResult.getRewrittenQueryText(), normalizedQuestion);
        List<String> expandedTerms =
                understandingResult == null ? List.of() : safeList(understandingResult.getExpandedSynonyms());
        List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities =
                understandingResult == null ? List.of() : safeList(understandingResult.getRecognizedEntities());
        List<ClassicsPublicContentFacadeDto> selectedSources =
                selectSources(normalizedQuestion, rewrittenQuestion, expandedTerms, recognizedEntities, publicContents);
        String contextSnapshotJson = writeJson(buildSnapshot(selectedSources));
        String promptMessagesJson =
                writeJson(buildPromptMessages(normalizedQuestion, rewrittenQuestion, contextSnapshotJson));
        String inputPayloadJson = writeJson(buildInputPayload(
                normalizedQuestion, rewrittenQuestion, expandedTerms, recognizedEntities, selectedSources));
        String outputSchemaJson = writeJson(Map.of("type", "text"));
        String filtersJson = writeJson(Map.of(
                "question", normalizedQuestion,
                "rewrittenQuestion", rewrittenQuestion,
                "sourceCount", selectedSources.size()));
        return new QaContext(
                normalizedQuestion,
                rewrittenQuestion,
                expandedTerms,
                recognizedEntities,
                selectedSources,
                contextSnapshotJson,
                promptMessagesJson,
                inputPayloadJson,
                outputSchemaJson,
                filtersJson,
                selectedSources.size());
    }

    private List<ClassicsPublicContentFacadeDto> selectSources(
            String question,
            String rewrittenQuestion,
            List<String> expandedTerms,
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities,
            List<ClassicsPublicContentFacadeDto> publicContents) {
        if (publicContents == null || publicContents.isEmpty()) {
            return List.of();
        }
        List<ClassicsSearchSourceContentScore> scoredContents = new ArrayList<>();
        for (ClassicsPublicContentFacadeDto content : publicContents) {
            int score = scoreContent(content, question, rewrittenQuestion, expandedTerms, recognizedEntities);
            if (score > 0) {
                scoredContents.add(new ClassicsSearchSourceContentScore(content, score));
            }
        }
        if (scoredContents.isEmpty()) {
            return publicContents.stream().limit(MAX_SOURCE_COUNT).toList();
        }
        return scoredContents.stream()
                .sorted(Comparator.comparingInt(ClassicsSearchSourceContentScore::score)
                        .reversed())
                .limit(MAX_SOURCE_COUNT)
                .map(ClassicsSearchSourceContentScore::content)
                .toList();
    }

    private int scoreContent(
            ClassicsPublicContentFacadeDto content,
            String question,
            String rewrittenQuestion,
            List<String> expandedTerms,
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities) {
        int score = 0;
        score += matchBonus(content.getTitle(), question, rewrittenQuestion, expandedTerms, recognizedEntities, 4);
        score += matchBonus(content.getSummary(), question, rewrittenQuestion, expandedTerms, recognizedEntities, 3);
        score += matchBonus(
                String.join(" ", safeList(content.getTextSegments())),
                question,
                rewrittenQuestion,
                expandedTerms,
                recognizedEntities,
                2);
        score += matchBonus(
                String.join(" ", safeList(content.getTagNames())),
                question,
                rewrittenQuestion,
                expandedTerms,
                recognizedEntities,
                2);
        score += matchBonus(
                content.getCategoryName(), question, rewrittenQuestion, expandedTerms, recognizedEntities, 1);
        score += matchBonus(
                content.getKnowledgeBase(), question, rewrittenQuestion, expandedTerms, recognizedEntities, 1);
        return score;
    }

    private int matchBonus(
            String text,
            String question,
            String rewrittenQuestion,
            List<String> expandedTerms,
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities,
            int weight) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        if (contains(lowerText, question) || contains(lowerText, rewrittenQuestion)) {
            return weight * 3;
        }
        for (String term : expandedTerms) {
            if (contains(lowerText, term)) {
                return weight * 2;
            }
        }
        for (QueryUnderstandingResult.RecognizedEntityResult entity : recognizedEntities) {
            if (entity != null
                    && (contains(lowerText, entity.getName()) || contains(lowerText, entity.getMatchedText()))) {
                return weight * 2;
            }
        }
        return 0;
    }

    private boolean contains(String haystack, String needle) {
        return StringUtils.isNotBlank(needle) && haystack.contains(needle.trim().toLowerCase(Locale.ROOT));
    }

    private Map<String, Object> buildSnapshot(List<ClassicsPublicContentFacadeDto> sources) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int index = 0; index < sources.size(); index++) {
            ClassicsPublicContentFacadeDto source = sources.get(index);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sourceRank", index + 1);
            item.put("contentDomain", "CLASSICS");
            item.put("contentType", source.getContentType());
            item.put("contentId", source.getContentId());
            item.put("knowledgeBase", source.getKnowledgeBase());
            item.put("titleSnapshot", source.getTitle());
            item.put(
                    "locationLabel",
                    Optional.ofNullable(source.getCategoryName()).orElse(source.getCategoryCode()));
            item.put("snippet", firstSnippet(source));
            items.add(item);
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("sources", items);
        return snapshot;
    }

    private List<Map<String, Object>> buildPromptMessages(
            String question, String rewrittenQuestion, String contextSnapshotJson) {
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("role", "system");
        system.put("content", "你是古籍知识问答助手。请基于给定来源回答，优先使用来源内容，避免编造。");
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("role", "user");
        user.put("content", "问题: " + question + "\n改写: " + rewrittenQuestion + "\n来源快照: " + contextSnapshotJson);
        return List.of(system, user);
    }

    private Map<String, Object> buildInputPayload(
            String question,
            String rewrittenQuestion,
            List<String> expandedTerms,
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities,
            List<ClassicsPublicContentFacadeDto> sources) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("question", question);
        payload.put("rewrittenQuestion", rewrittenQuestion);
        payload.put("expandedTerms", expandedTerms);
        payload.put("recognizedEntities", recognizedEntities);
        payload.put("sources", buildSnapshot(sources).get("sources"));
        return payload;
    }

    private String firstSnippet(ClassicsPublicContentFacadeDto source) {
        if (source == null) {
            return null;
        }
        if (source.getSummary() != null && !source.getSummary().isBlank()) {
            return source.getSummary().trim();
        }
        if (source.getTextSegments() != null) {
            for (String segment : source.getTextSegments()) {
                if (segment != null && !segment.isBlank()) {
                    return segment.trim();
                }
            }
        }
        return null;
    }

    private String normalizeQuestion(String question) {
        return StringUtils.trimToNull(question);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(
                    "DISCOVERY-30004",
                    "discovery.qa.context-json-build-failed",
                    "QA context json build failed",
                    exception);
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    public record QaContext(
            String normalizedQuestion,
            String rewrittenQuestion,
            List<String> expandedTerms,
            List<QueryUnderstandingResult.RecognizedEntityResult> recognizedEntities,
            List<ClassicsPublicContentFacadeDto> sourceContents,
            String contextSnapshotJson,
            String promptMessagesJson,
            String inputPayloadJson,
            String outputSchemaJson,
            String filtersJson,
            Integer candidateCount) {}

    private record ClassicsSearchSourceContentScore(ClassicsPublicContentFacadeDto content, int score) {}
}
