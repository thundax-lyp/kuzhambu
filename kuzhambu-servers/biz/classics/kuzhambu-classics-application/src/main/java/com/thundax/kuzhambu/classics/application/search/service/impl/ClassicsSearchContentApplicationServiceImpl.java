package com.thundax.kuzhambu.classics.application.search.service.impl;

import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentTagStatus;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsKeyword;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ClassicsSearchContentApplicationServiceImpl implements ClassicsSearchContentApplicationService {

    private static final int FETCH_PAGE_SIZE = 200;

    private final SancaiApplicationService sancaiApplicationService;
    private final WangqiDocumentApplicationService wangqiDocumentApplicationService;
    private final MingCustomsApplicationService mingCustomsApplicationService;
    private final ClassicsContentApplicationService classicsContentApplicationService;

    public ClassicsSearchContentApplicationServiceImpl(
            SancaiApplicationService sancaiApplicationService,
            WangqiDocumentApplicationService wangqiDocumentApplicationService,
            MingCustomsApplicationService mingCustomsApplicationService,
            ClassicsContentApplicationService classicsContentApplicationService) {
        this.sancaiApplicationService = sancaiApplicationService;
        this.wangqiDocumentApplicationService = wangqiDocumentApplicationService;
        this.mingCustomsApplicationService = mingCustomsApplicationService;
        this.classicsContentApplicationService = classicsContentApplicationService;
    }

    @Override
    public List<ClassicsSearchSourceContent> listPublicContents() {
        List<ClassicsSearchSourceContent> contents = new ArrayList<>();
        contents.addAll(listPublicSancaiEntries());
        contents.addAll(listPublicWangqiDocuments());
        contents.addAll(listPublicMingCustomsEntries());
        return contents;
    }

    @Override
    public ClassicsSearchSourceContent getPublicContent(String contentType, String contentId) {
        Long idValue = Long.valueOf(contentId);
        return switch (ClassicsContentType.from(contentType)) {
            case SANCAI_ENTRY ->
                toPublicSancaiEntry(
                        sancaiApplicationService.getEntry(SancaiEntryIdCodec.toDomain(idValue)),
                        listSancaiCategoryMap(),
                        listSancaiCategoryIdByVolumeId(),
                        listSancaiVolumeMap());
            case WANGQI_DOCUMENT ->
                toPublicWangqiDocument(wangqiDocumentApplicationService.get(WangqiDocumentIdCodec.toDomain(idValue)));
            case MING_CUSTOMS ->
                toPublicMingCustomsEntry(mingCustomsApplicationService.get(MingCustomsEntryIdCodec.toDomain(idValue)));
        };
    }

    @Override
    public List<ClassicsSearchSourceContent> listWorkbenchCategoryContents() {
        List<SancaiCategory> categories = sancaiApplicationService.listCategories();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream()
                .filter(category -> category != null && category.getId() != null)
                .map(category -> new ClassicsSearchSourceContent(
                        ClassicsContentType.SANCAI_ENTRY.value(),
                        null,
                        "classics",
                        String.valueOf(category.getId().value()),
                        category.getTitle(),
                        null,
                        null,
                        category.getTitle(),
                        null,
                        List.of(),
                        List.of(),
                        null,
                        null,
                        null,
                        null))
                .toList();
    }

    @Override
    public List<ClassicsSearchSourceContent> listWorkbenchVolumeContents() {
        List<SancaiCategory> categories = sancaiApplicationService.listCategories();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClassicsSearchSourceContent> results = new ArrayList<>();
        for (SancaiCategory category : categories) {
            if (category == null || category.getId() == null) {
                continue;
            }
            List<SancaiVolume> volumes = sancaiApplicationService.listVolumes(category.getId());
            if (volumes == null || volumes.isEmpty()) {
                continue;
            }
            for (SancaiVolume volume : volumes) {
                if (volume == null || volume.getId() == null) {
                    continue;
                }
                results.add(new ClassicsSearchSourceContent(
                        ClassicsContentType.SANCAI_ENTRY.value(),
                        String.valueOf(volume.getId().value()),
                        "classics",
                        String.valueOf(category.getId().value()),
                        category.getTitle(),
                        String.valueOf(volume.getId().value()),
                        volume.getTitle(),
                        volume.getTitle(),
                        null,
                        List.of(),
                        List.of(),
                        null,
                        null,
                        null,
                        null));
            }
        }
        return results;
    }

    @Override
    public List<ClassicsSearchSourceContent> listWorkbenchContents() {
        List<ClassicsSearchSourceContent> contents = new ArrayList<>();
        contents.addAll(listWorkbenchSancaiEntries());
        return contents;
    }

    @Override
    public List<ClassicsSearchSourceContent> listWorkbenchContents(String categoryCode, String volumeCode) {
        if (volumeCode == null || volumeCode.isBlank()) {
            return listWorkbenchContents();
        }
        Long categoryId = parseId(categoryCode);
        Long volumeId = parseId(volumeCode);
        if (volumeId == null) {
            return Collections.emptyList();
        }
        return listWorkbenchSancaiEntries(categoryId, volumeId);
    }

    @Override
    public ClassicsSearchSourceContent getWorkbenchContent(String contentType, String contentId) {
        Long idValue = Long.valueOf(contentId);
        return switch (ClassicsContentType.from(contentType)) {
            case SANCAI_ENTRY ->
                toWorkbenchSancaiEntry(
                        sancaiApplicationService.getEntry(SancaiEntryIdCodec.toDomain(idValue)),
                        listSancaiCategoryMap(),
                        listSancaiCategoryIdByVolumeId(),
                        listSancaiVolumeMap());
            case WANGQI_DOCUMENT, MING_CUSTOMS -> getPublicContent(contentType, contentId);
        };
    }

    private List<ClassicsSearchSourceContent> listPublicSancaiEntries() {
        Map<Long, SancaiCategory> categoryById = listSancaiCategoryMap();
        Map<Long, Long> categoryIdByVolumeId = listSancaiCategoryIdByVolumeId();
        Map<Long, SancaiVolume> volumeById = listSancaiVolumeMap();
        List<SancaiEntry> entries = sancaiApplicationService.listEntries(new SancaiEntryPageQuery(
                null, null, null, SancaiEntryLifecycleStatus.PUBLISHED, null, null, null, null, SortDirection.ASC));
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClassicsSearchSourceContent> results = new ArrayList<>(entries.size());
        for (SancaiEntry entry : entries) {
            ClassicsSearchSourceContent content =
                    toPublicSancaiEntry(entry, categoryById, categoryIdByVolumeId, volumeById);
            if (content != null) {
                results.add(content);
            }
        }
        return results;
    }

    private List<ClassicsSearchSourceContent> listWorkbenchSancaiEntries() {
        return listWorkbenchSancaiEntries(null, null);
    }

    private List<ClassicsSearchSourceContent> listWorkbenchSancaiEntries(Long categoryId, Long volumeId) {
        Map<Long, SancaiCategory> categoryById = listSancaiCategoryMap();
        Map<Long, Long> categoryIdByVolumeId = listSancaiCategoryIdByVolumeId();
        Map<Long, SancaiVolume> volumeById = listSancaiVolumeMap();
        List<SancaiEntry> entries = sancaiApplicationService.listEntries(
                new SancaiEntryPageQuery(categoryId, volumeId, null, null, null, null, null, null, SortDirection.ASC));
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClassicsSearchSourceContent> results = new ArrayList<>(entries.size());
        for (SancaiEntry entry : entries) {
            ClassicsSearchSourceContent content =
                    toWorkbenchSancaiEntry(entry, categoryById, categoryIdByVolumeId, volumeById);
            if (content != null) {
                results.add(content);
            }
        }
        return results;
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<ClassicsSearchSourceContent> listPublicWangqiDocuments() {
        List<WangqiDocument> documents =
                wangqiDocumentApplicationService.listTimeline(new WangqiDocumentPageQuery(null, SortDirection.ASC));
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClassicsSearchSourceContent> results = new ArrayList<>(documents.size());
        for (WangqiDocument document : documents) {
            ClassicsSearchSourceContent content = toPublicWangqiDocument(document);
            if (content != null) {
                results.add(content);
            }
        }
        return results;
    }

    private List<ClassicsSearchSourceContent> listPublicMingCustomsEntries() {
        List<ClassicsSearchSourceContent> results = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            var pageResult = mingCustomsApplicationService.page(
                    new MingCustomsQuery(null, null, null, SortDirection.ASC), new PageQuery(pageNo, FETCH_PAGE_SIZE));
            if (pageResult == null
                    || pageResult.getRecords() == null
                    || pageResult.getRecords().isEmpty()) {
                return results;
            }
            for (MingCustomsEntry entry : pageResult.getRecords()) {
                ClassicsSearchSourceContent content = toPublicMingCustomsEntry(entry);
                if (content != null) {
                    results.add(content);
                }
            }
            if (pageResult.getRecords().size() < FETCH_PAGE_SIZE) {
                return results;
            }
            pageNo++;
        }
    }

    private Map<Long, SancaiCategory> listSancaiCategoryMap() {
        List<SancaiCategory> categories = sancaiApplicationService.listCategories();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, SancaiCategory> categoryById = new HashMap<>(categories.size());
        for (SancaiCategory category : categories) {
            if (category != null && category.getId() != null) {
                categoryById.put(category.getId().value(), category);
            }
        }
        return categoryById;
    }

    private Map<Long, Long> listSancaiCategoryIdByVolumeId() {
        List<SancaiCategory> categories = sancaiApplicationService.listCategories();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> categoryIdByVolumeId = new HashMap<>();
        for (SancaiCategory category : categories) {
            if (category == null || category.getId() == null) {
                continue;
            }
            List<SancaiVolume> volumes = sancaiApplicationService.listVolumes(category.getId());
            if (volumes == null || volumes.isEmpty()) {
                continue;
            }
            for (SancaiVolume volume : volumes) {
                if (volume != null && volume.getId() != null) {
                    categoryIdByVolumeId.put(
                            volume.getId().value(), category.getId().value());
                }
            }
        }
        return categoryIdByVolumeId;
    }

    private Map<Long, SancaiVolume> listSancaiVolumeMap() {
        List<SancaiCategory> categories = sancaiApplicationService.listCategories();
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, SancaiVolume> volumeById = new HashMap<>();
        for (SancaiCategory category : categories) {
            if (category == null || category.getId() == null) {
                continue;
            }
            List<SancaiVolume> volumes = sancaiApplicationService.listVolumes(category.getId());
            if (volumes == null || volumes.isEmpty()) {
                continue;
            }
            for (SancaiVolume volume : volumes) {
                if (volume != null && volume.getId() != null) {
                    volumeById.put(volume.getId().value(), volume);
                }
            }
        }
        return volumeById;
    }

    private List<String> nonBlankSegments(String... values) {
        List<String> segments = new ArrayList<>();
        if (values == null) {
            return segments;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                segments.add(value.trim());
            }
        }
        return segments;
    }

    private ClassicsSearchSourceContent toPublicSancaiEntry(
            SancaiEntry entry,
            Map<Long, SancaiCategory> categoryById,
            Map<Long, Long> categoryIdByVolumeId,
            Map<Long, SancaiVolume> volumeById) {
        if (entry == null
                || entry.getId() == null
                || entry.getLifecycleStatus() != SancaiEntryLifecycleStatus.PUBLISHED) {
            return null;
        }
        return toSancaiEntry(entry, categoryById, categoryIdByVolumeId, volumeById);
    }

    private ClassicsSearchSourceContent toWorkbenchSancaiEntry(
            SancaiEntry entry,
            Map<Long, SancaiCategory> categoryById,
            Map<Long, Long> categoryIdByVolumeId,
            Map<Long, SancaiVolume> volumeById) {
        if (entry == null || entry.getId() == null) {
            return null;
        }
        return toSancaiEntry(entry, categoryById, categoryIdByVolumeId, volumeById);
    }

    private ClassicsSearchSourceContent toSancaiEntry(
            SancaiEntry entry,
            Map<Long, SancaiCategory> categoryById,
            Map<Long, Long> categoryIdByVolumeId,
            Map<Long, SancaiVolume> volumeById) {
        Long volumeId = entry.getVolumeId() == null ? null : entry.getVolumeId().value();
        Long categoryId = volumeId == null ? null : categoryIdByVolumeId.get(volumeId);
        SancaiCategory category = categoryId == null ? null : categoryById.get(categoryId);
        SancaiVolume volume = volumeId == null ? null : volumeById.get(volumeId);
        String contentType = ClassicsContentType.SANCAI_ENTRY.value();
        String contentId = String.valueOf(entry.getId().value());
        List<String> textSegments =
                nonBlankSegments(entry.getOriginalText(), entry.getTranslationText(), entry.getSummary());
        textSegments.addAll(qaTextSegments(contentType, contentId));
        return new ClassicsSearchSourceContent(
                contentType,
                contentId,
                contentType,
                categoryId == null ? null : String.valueOf(categoryId),
                category == null ? null : category.getTitle(),
                volumeId == null ? null : String.valueOf(volumeId),
                volume == null ? null : volume.getTitle(),
                entry.getTitle(),
                entry.getSummary(),
                textSegments,
                tagNames(contentType, contentId),
                entry.getLifecycleStatus().value(),
                null,
                entry.getCurrentVersionNo(),
                entry.getContentUpdatedAt(),
                entry.getContentUpdatedAt());
    }

    private ClassicsSearchSourceContent toPublicWangqiDocument(WangqiDocument document) {
        if (document == null
                || document.getId() == null
                || document.getLifecycleStatus() != ClassicsPublicationLifecycleStatus.PUBLISHED) {
            return null;
        }
        String contentType = ClassicsContentType.WANGQI_DOCUMENT.value();
        String contentId = String.valueOf(document.getId().value());
        List<String> textSegments = nonBlankSegments(document.getTitle(), document.getSummary(), document.getContent());
        textSegments.addAll(qaTextSegments(contentType, contentId));
        return new ClassicsSearchSourceContent(
                contentType,
                contentId,
                contentType,
                null,
                null,
                null,
                null,
                document.getTitle(),
                document.getSummary(),
                textSegments,
                tagNames(contentType, contentId),
                "PUBLISHED",
                null,
                document.getCurrentVersionNo(),
                document.getDocumentTime() == null ? document.getContentUpdatedAt() : document.getDocumentTime(),
                document.getContentUpdatedAt());
    }

    private ClassicsSearchSourceContent toPublicMingCustomsEntry(MingCustomsEntry entry) {
        if (entry == null
                || entry.getId() == null
                || entry.getLifecycleStatus() != ClassicsPublicationLifecycleStatus.PUBLISHED) {
            return null;
        }
        String contentType = ClassicsContentType.MING_CUSTOMS.value();
        String contentId = String.valueOf(entry.getId().value());
        List<String> textSegments =
                nonBlankSegments(entry.getTitle(), entry.getSummary(), entry.getContent(), entry.getOriginalExcerpts());
        textSegments.addAll(keywordTextSegments(entry));
        textSegments.addAll(qaTextSegments(contentType, contentId));
        return new ClassicsSearchSourceContent(
                contentType,
                contentId,
                contentType,
                entry.getCategory(),
                entry.getCategory(),
                null,
                null,
                entry.getTitle(),
                entry.getSummary(),
                textSegments,
                tagNames(contentType, contentId),
                "PUBLISHED",
                null,
                entry.getCurrentVersionNo(),
                entry.getContentUpdatedAt(),
                entry.getContentUpdatedAt());
    }

    private List<String> tagNames(String contentType, String contentId) {
        if (classicsContentApplicationService == null) {
            return Collections.emptyList();
        }
        List<ClassicsContentTag> tags = classicsContentApplicationService.listTags(
                contentType, ClassicsContentIdCodec.toDomain(Long.valueOf(contentId)));
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return tags.stream()
                .filter(tag -> tag != null
                        && (tag.getStatus() == null || tag.getStatus() == ClassicsContentTagStatus.ACTIVE)
                        && tag.getTagNameSnapshot() != null
                        && !tag.getTagNameSnapshot().isBlank())
                .map(tag -> tag.getTagNameSnapshot().trim())
                .distinct()
                .toList();
    }

    private List<String> qaTextSegments(String contentType, String contentId) {
        if (classicsContentApplicationService == null) {
            return Collections.emptyList();
        }
        List<ClassicsContentQaPair> qaPairs = classicsContentApplicationService.listQaPairs(
                contentType, ClassicsContentIdCodec.toDomain(Long.valueOf(contentId)));
        if (qaPairs == null || qaPairs.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> segments = new ArrayList<>();
        for (ClassicsContentQaPair pair : qaPairs) {
            if (pair == null) {
                continue;
            }
            segments.addAll(nonBlankSegments(pair.getQuestion(), pair.getAnswer()));
        }
        return segments;
    }

    private List<String> keywordTextSegments(MingCustomsEntry entry) {
        if (entry == null || entry.getId() == null) {
            return Collections.emptyList();
        }
        List<MingCustomsKeyword> keywords = mingCustomsApplicationService.listKeywords(entry.getId());
        if (keywords == null || keywords.isEmpty()) {
            return Collections.emptyList();
        }
        return keywords.stream()
                .filter(keyword -> keyword != null
                        && keyword.getKeyword() != null
                        && !keyword.getKeyword().isBlank())
                .map(keyword -> keyword.getKeyword().trim())
                .distinct()
                .toList();
    }
}
