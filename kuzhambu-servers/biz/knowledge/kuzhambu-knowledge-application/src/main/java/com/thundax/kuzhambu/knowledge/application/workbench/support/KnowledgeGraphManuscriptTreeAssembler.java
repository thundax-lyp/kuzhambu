package com.thundax.kuzhambu.knowledge.application.workbench.support;

import com.thundax.kuzhambu.classics.facade.dto.ClassicsPublicContentFacadeDto;
import com.thundax.kuzhambu.knowledge.application.workbench.result.KnowledgeGraphWorkbenchResults.ManuscriptTreeNodeResult;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeGraphManuscriptTreeAssembler {

    public static final String NODE_TYPE_SOURCE_ROOT = "SOURCE_ROOT";
    public static final String NODE_TYPE_CATEGORY = "CATEGORY";
    public static final String NODE_TYPE_MANUSCRIPT = "MANUSCRIPT";
    public static final String SOURCE_TYPE_SANCAI_ENTRY = "SANCAI_ENTRY";
    public static final String SOURCE_TYPE_WANGQI_DOCUMENT = "WANGQI_DOCUMENT";
    public static final String SOURCE_TYPE_MING_CUSTOMS = "MING_CUSTOMS";
    public static final String STATUS_NOT_EXTRACTED = "NOT_EXTRACTED";

    private static final List<SourceSpec> SOURCE_SPECS = List.of(
            new SourceSpec(SOURCE_TYPE_SANCAI_ENTRY, "三才图会"),
            new SourceSpec(SOURCE_TYPE_WANGQI_DOCUMENT, "王圻文档"),
            new SourceSpec(SOURCE_TYPE_MING_CUSTOMS, "明俗稿件"));
    private static final String NODE_KEY_SEPARATOR = ":";
    private static final String CATEGORY_FALLBACK = "未分类";

    public List<ManuscriptTreeNodeResult> toTree(
            List<ClassicsPublicContentFacadeDto> contents,
            String sourceContentType,
            String parentKey,
            String keyword,
            String graphStatus,
            Function<ClassicsPublicContentFacadeDto, ManuscriptGraphSnapshot> graphSnapshotResolver) {
        String normalizedParentKey = normalize(parentKey);
        List<ClassicsPublicContentFacadeDto> effectiveContents = contents == null ? List.of() : contents;
        if (normalizedParentKey == null) {
            if (normalize(keyword) != null || normalize(graphStatus) != null) {
                return manuscriptMatches(
                        effectiveContents, sourceContentType, keyword, graphStatus, graphSnapshotResolver);
            }
            return sourceRoots(sourceContentType);
        }
        NodeKey nodeKey = NodeKey.parse(normalizedParentKey);
        if (nodeKey == null || NODE_TYPE_SOURCE_ROOT.equals(nodeKey.nodeType())) {
            String type = nodeKey == null ? sourceContentType : nodeKey.sourceContentType();
            return categoryNodes(effectiveContents, type, keyword, graphStatus, graphSnapshotResolver);
        }
        if (NODE_TYPE_CATEGORY.equals(nodeKey.nodeType())) {
            return manuscriptNodes(
                    effectiveContents,
                    nodeKey.sourceContentType(),
                    nodeKey.categoryCode(),
                    keyword,
                    graphStatus,
                    graphSnapshotResolver);
        }
        return List.of();
    }

    public List<ManuscriptTreeNodeResult> manuscriptMatches(
            List<ClassicsPublicContentFacadeDto> contents,
            String sourceContentType,
            String keyword,
            String graphStatus,
            Function<ClassicsPublicContentFacadeDto, ManuscriptGraphSnapshot> graphSnapshotResolver) {
        return manuscriptNodes(contents, sourceContentType, null, keyword, graphStatus, graphSnapshotResolver);
    }

    public String sourceRootKey(String sourceContentType) {
        return NODE_TYPE_SOURCE_ROOT + NODE_KEY_SEPARATOR + sourceContentType;
    }

    public String categoryKey(String sourceContentType, String categoryCode) {
        return NODE_TYPE_CATEGORY + NODE_KEY_SEPARATOR + sourceContentType + NODE_KEY_SEPARATOR + categoryCode;
    }

    public String manuscriptKey(String sourceContentType, Long sourceContentId) {
        return NODE_TYPE_MANUSCRIPT + NODE_KEY_SEPARATOR + sourceContentType + NODE_KEY_SEPARATOR + sourceContentId;
    }

    private List<ManuscriptTreeNodeResult> sourceRoots(String sourceContentType) {
        String normalizedType = normalize(sourceContentType);
        return SOURCE_SPECS.stream()
                .filter(spec ->
                        normalizedType == null || spec.sourceContentType().equals(normalizedType))
                .map(spec -> ManuscriptTreeNodeResult.builder()
                        .nodeKey(sourceRootKey(spec.sourceContentType()))
                        .nodeType(NODE_TYPE_SOURCE_ROOT)
                        .title(spec.title())
                        .sourceContentType(spec.sourceContentType())
                        .graphStatus(STATUS_NOT_EXTRACTED)
                        .children(List.of())
                        .build())
                .toList();
    }

    private List<ManuscriptTreeNodeResult> categoryNodes(
            List<ClassicsPublicContentFacadeDto> contents,
            String sourceContentType,
            String keyword,
            String graphStatus,
            Function<ClassicsPublicContentFacadeDto, ManuscriptGraphSnapshot> graphSnapshotResolver) {
        Map<String, ClassicsPublicContentFacadeDto> categorySamples = new LinkedHashMap<>();
        for (ClassicsPublicContentFacadeDto content :
                filteredContents(contents, sourceContentType, null, keyword, graphStatus, graphSnapshotResolver)) {
            categorySamples.putIfAbsent(categoryCode(content), content);
        }
        return categorySamples.values().stream()
                .sorted(Comparator.comparing(this::categoryTitle))
                .map(content -> ManuscriptTreeNodeResult.builder()
                        .nodeKey(categoryKey(content.getContentType(), categoryCode(content)))
                        .parentKey(sourceRootKey(content.getContentType()))
                        .nodeType(NODE_TYPE_CATEGORY)
                        .title(categoryTitle(content))
                        .sourceContentType(content.getContentType())
                        .sourcePath(categoryTitle(content))
                        .graphStatus(STATUS_NOT_EXTRACTED)
                        .children(List.of())
                        .build())
                .toList();
    }

    private List<ManuscriptTreeNodeResult> manuscriptNodes(
            List<ClassicsPublicContentFacadeDto> contents,
            String sourceContentType,
            String categoryCode,
            String keyword,
            String graphStatus,
            Function<ClassicsPublicContentFacadeDto, ManuscriptGraphSnapshot> graphSnapshotResolver) {
        return filteredContents(contents, sourceContentType, categoryCode, keyword, graphStatus, graphSnapshotResolver)
                .stream()
                .sorted(Comparator.comparing(
                        ClassicsPublicContentFacadeDto::getTitle, Comparator.nullsLast(String::compareTo)))
                .map(content -> {
                    ManuscriptGraphSnapshot snapshot = resolveSnapshot(content, graphSnapshotResolver);
                    return ManuscriptTreeNodeResult.builder()
                            .nodeKey(manuscriptKey(content.getContentType(), parseContentId(content)))
                            .parentKey(categoryKey(content.getContentType(), categoryCode(content)))
                            .nodeType(NODE_TYPE_MANUSCRIPT)
                            .title(content.getTitle())
                            .sourceContentType(content.getContentType())
                            .sourceContentId(parseContentId(content))
                            .sourcePath(sourcePath(content))
                            .graphStatus(snapshot.graphStatus())
                            .latestTaskId(snapshot.latestTaskId())
                            .latestGraphVersionId(snapshot.latestGraphVersionId())
                            .children(List.of())
                            .build();
                })
                .toList();
    }

    private List<ClassicsPublicContentFacadeDto> filteredContents(
            List<ClassicsPublicContentFacadeDto> contents,
            String sourceContentType,
            String categoryCode,
            String keyword,
            String graphStatus,
            Function<ClassicsPublicContentFacadeDto, ManuscriptGraphSnapshot> graphSnapshotResolver) {
        String normalizedType = normalize(sourceContentType);
        String normalizedCategoryCode = normalize(categoryCode);
        String normalizedKeyword = normalize(keyword);
        String normalizedGraphStatus = normalize(graphStatus);
        return contents.stream()
                .filter(Objects::nonNull)
                .filter(content -> supportedSourceType(content.getContentType()))
                .filter(content -> normalizedType == null || normalizedType.equals(content.getContentType()))
                .filter(content ->
                        normalizedCategoryCode == null || normalizedCategoryCode.equals(categoryCode(content)))
                .filter(content -> normalizedKeyword == null || matchesKeyword(content, normalizedKeyword))
                .filter(content -> normalizedGraphStatus == null
                        || normalizedGraphStatus.equals(
                                resolveSnapshot(content, graphSnapshotResolver).graphStatus()))
                .toList();
    }

    private ManuscriptGraphSnapshot resolveSnapshot(
            ClassicsPublicContentFacadeDto content,
            Function<ClassicsPublicContentFacadeDto, ManuscriptGraphSnapshot> graphSnapshotResolver) {
        if (graphSnapshotResolver == null) {
            return ManuscriptGraphSnapshot.empty();
        }
        ManuscriptGraphSnapshot snapshot = graphSnapshotResolver.apply(content);
        return snapshot == null ? ManuscriptGraphSnapshot.empty() : snapshot;
    }

    private boolean matchesKeyword(ClassicsPublicContentFacadeDto content, String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        return contains(content.getTitle(), normalizedKeyword)
                || contains(content.getSummary(), normalizedKeyword)
                || contains(categoryTitle(content), normalizedKeyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean supportedSourceType(String sourceContentType) {
        return SOURCE_SPECS.stream().anyMatch(spec -> spec.sourceContentType().equals(sourceContentType));
    }

    private String categoryCode(ClassicsPublicContentFacadeDto content) {
        String categoryCode = normalize(content == null ? null : content.getCategoryCode());
        if (categoryCode != null) {
            return categoryCode;
        }
        return categoryTitle(content);
    }

    private String categoryTitle(ClassicsPublicContentFacadeDto content) {
        String categoryName = normalize(content == null ? null : content.getCategoryName());
        return categoryName == null ? CATEGORY_FALLBACK : categoryName;
    }

    private String sourcePath(ClassicsPublicContentFacadeDto content) {
        String title = normalize(content == null ? null : content.getTitle());
        String category = categoryTitle(content);
        return title == null ? category : category + " / " + title;
    }

    private Long parseContentId(ClassicsPublicContentFacadeDto content) {
        String contentId = normalize(content == null ? null : content.getContentId());
        if (contentId == null) {
            return null;
        }
        return Long.valueOf(contentId);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    public record ManuscriptGraphSnapshot(String graphStatus, Long latestTaskId, Long latestGraphVersionId) {
        public static ManuscriptGraphSnapshot empty() {
            return new ManuscriptGraphSnapshot(STATUS_NOT_EXTRACTED, null, null);
        }
    }

    private record SourceSpec(String sourceContentType, String title) {}

    private record NodeKey(String nodeType, String sourceContentType, String categoryCode) {
        private static NodeKey parse(String nodeKey) {
            String[] parts = nodeKey == null ? new String[0] : nodeKey.split(NODE_KEY_SEPARATOR, 3);
            if (parts.length < 2) {
                return null;
            }
            return new NodeKey(parts[0], parts[1], parts.length > 2 ? parts[2] : null);
        }
    }
}
