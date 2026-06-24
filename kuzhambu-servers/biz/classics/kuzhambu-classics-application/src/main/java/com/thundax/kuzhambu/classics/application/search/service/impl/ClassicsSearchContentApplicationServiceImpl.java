package com.thundax.kuzhambu.classics.application.search.service.impl;

import com.thundax.kuzhambu.classics.application.mingcustoms.query.MingCustomsPageQuery;
import com.thundax.kuzhambu.classics.application.mingcustoms.service.MingCustomsApplicationService;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryPageQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.SancaiApplicationService;
import com.thundax.kuzhambu.classics.application.search.result.ClassicsSearchSourceContent;
import com.thundax.kuzhambu.classics.application.search.service.ClassicsSearchContentApplicationService;
import com.thundax.kuzhambu.classics.application.wangqi.query.WangqiDocumentPageQuery;
import com.thundax.kuzhambu.classics.application.wangqi.service.WangqiDocumentApplicationService;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategory;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
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

    public ClassicsSearchContentApplicationServiceImpl(
            SancaiApplicationService sancaiApplicationService,
            WangqiDocumentApplicationService wangqiDocumentApplicationService,
            MingCustomsApplicationService mingCustomsApplicationService) {
        this.sancaiApplicationService = sancaiApplicationService;
        this.wangqiDocumentApplicationService = wangqiDocumentApplicationService;
        this.mingCustomsApplicationService = mingCustomsApplicationService;
    }

    @Override
    public List<ClassicsSearchSourceContent> listPublicContents() {
        List<ClassicsSearchSourceContent> contents = new ArrayList<>();
        contents.addAll(listPublicSancaiEntries());
        contents.addAll(listPublicWangqiDocuments());
        contents.addAll(listPublicMingCustomsEntries());
        return contents;
    }

    private List<ClassicsSearchSourceContent> listPublicSancaiEntries() {
        Map<Long, SancaiCategory> categoryById = listSancaiCategoryMap();
        Map<Long, Long> categoryIdByVolumeId = listSancaiCategoryIdByVolumeId();
        List<SancaiEntry> entries = sancaiApplicationService.listEntries(new SancaiEntryPageQuery(
                null,
                null,
                null,
                SancaiEntryLifecycleStatus.PUBLISHED,
                SancaiEntryVisibility.PUBLIC,
                null,
                null,
                null,
                null,
                SortDirection.ASC));
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClassicsSearchSourceContent> results = new ArrayList<>(entries.size());
        for (SancaiEntry entry : entries) {
            if (entry == null || entry.getId() == null) {
                continue;
            }
            Long volumeId =
                    entry.getVolumeId() == null ? null : entry.getVolumeId().value();
            Long categoryId = volumeId == null ? null : categoryIdByVolumeId.get(volumeId);
            SancaiCategory category = categoryId == null ? null : categoryById.get(categoryId);
            results.add(new ClassicsSearchSourceContent(
                    "SANCAI_ENTRY",
                    String.valueOf(entry.getId().value()),
                    "SANCAI_ENTRY",
                    categoryId == null ? null : String.valueOf(categoryId),
                    category == null ? null : category.getTitle(),
                    entry.getTitle(),
                    entry.getSummary(),
                    nonBlankSegments(entry.getOriginalText(), entry.getTranslationText(), entry.getSummary()),
                    Collections.emptyList(),
                    entry.getLifecycleStatus() == null
                            ? null
                            : entry.getLifecycleStatus().value(),
                    entry.getVisibility() == null ? null : entry.getVisibility().value(),
                    entry.getContentUpdatedAt(),
                    entry.getContentUpdatedAt()));
        }
        return results;
    }

    private List<ClassicsSearchSourceContent> listPublicWangqiDocuments() {
        List<WangqiDocument> documents = wangqiDocumentApplicationService.listTimeline(
                new WangqiDocumentPageQuery(null, WangqiDocumentVisibility.PUBLIC, SortDirection.ASC));
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }
        List<ClassicsSearchSourceContent> results = new ArrayList<>(documents.size());
        for (WangqiDocument document : documents) {
            if (document == null || document.getId() == null) {
                continue;
            }
            results.add(new ClassicsSearchSourceContent(
                    "WANGQI_DOCUMENT",
                    String.valueOf(document.getId().value()),
                    "WANGQI_DOCUMENT",
                    null,
                    null,
                    document.getTitle(),
                    document.getSummary(),
                    nonBlankSegments(document.getTitle(), document.getSummary(), document.getContent()),
                    Collections.emptyList(),
                    "PUBLISHED",
                    document.getVisibility() == null
                            ? null
                            : document.getVisibility().value(),
                    document.getDocumentTime() == null ? document.getContentUpdatedAt() : document.getDocumentTime(),
                    document.getContentUpdatedAt()));
        }
        return results;
    }

    private List<ClassicsSearchSourceContent> listPublicMingCustomsEntries() {
        List<ClassicsSearchSourceContent> results = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            var pageResult = mingCustomsApplicationService.page(
                    new MingCustomsPageQuery(null, null, null, MingCustomsVisibility.PUBLIC, SortDirection.ASC),
                    new PageQuery(pageNo, FETCH_PAGE_SIZE));
            if (pageResult == null
                    || pageResult.getRecords() == null
                    || pageResult.getRecords().isEmpty()) {
                return results;
            }
            for (MingCustomsEntry entry : pageResult.getRecords()) {
                if (entry == null || entry.getId() == null) {
                    continue;
                }
                results.add(new ClassicsSearchSourceContent(
                        "MING_CUSTOMS",
                        String.valueOf(entry.getId().value()),
                        "MING_CUSTOMS",
                        entry.getCategory(),
                        entry.getCategory(),
                        entry.getTitle(),
                        entry.getSummary(),
                        nonBlankSegments(
                                entry.getTitle(), entry.getSummary(), entry.getContent(), entry.getOriginalExcerpts()),
                        Collections.emptyList(),
                        "PUBLISHED",
                        entry.getVisibility() == null
                                ? null
                                : entry.getVisibility().value(),
                        entry.getContentUpdatedAt(),
                        entry.getContentUpdatedAt()));
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
}
