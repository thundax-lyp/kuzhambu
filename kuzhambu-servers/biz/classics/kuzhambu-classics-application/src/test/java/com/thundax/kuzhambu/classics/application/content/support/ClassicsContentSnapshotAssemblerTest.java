package com.thundax.kuzhambu.classics.application.content.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ClassicsContentSnapshotAssemblerTest {
    private final ClassicsContentSnapshotAssembler assembler = new ClassicsContentSnapshotAssembler();

    @Test
    void shouldAssembleSancaiEntryVersionSnapshotJson() {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setVolumeId(SancaiVolumeId.of(2L));
        entry.setTitle("三才");
        entry.setOriginalText("原文");
        entry.setTranslationText("译文");
        entry.setSummary("摘要");
        entry.setLifecycleStatus(SancaiEntryLifecycleStatus.PUBLISHED);
        entry.setVisibility(SancaiEntryVisibility.PUBLIC);
        entry.setTranslationStatus(SancaiEntryTranslationStatus.READY);
        entry.setImageStatus(SancaiEntryImageStatus.READY);
        entry.setVisualAssetStatus(SancaiEntryVisualAssetStatus.PROCESSING);
        entry.setRefinementStatus(SancaiEntryRefinementStatus.COMPLETE);
        entry.setPriority(7);
        entry.setContentUpdatedAt(new Date(1_000L));

        String snapshotJson = assembler.toSnapshotJson(entry);

        assertEquals(
                "{\"contentType\":\"SANCAI_ENTRY\",\"contentId\":100,\"contentUpdatedAt\":\"1970-01-01T00:00:01Z\","
                        + "\"volumeId\":2,\"title\":\"三才\",\"originalText\":\"原文\",\"translationText\":\"译文\","
                        + "\"summary\":\"摘要\",\"lifecycleStatus\":\"PUBLISHED\",\"visibility\":\"PUBLIC\","
                        + "\"translationStatus\":\"READY\",\"imageStatus\":\"READY\","
                        + "\"visualAssetStatus\":\"PROCESSING\",\"refinementStatus\":\"COMPLETE\",\"priority\":7}",
                snapshotJson);
    }

    @Test
    void shouldAssembleWangqiDocumentVersionSnapshotJson() {
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentId.of(200L));
        document.setTitle("王圻");
        document.setSummary("摘要");
        document.setContentFormat(WangqiContentFormat.MARKDOWN);
        document.setContent("正文");
        document.setDocumentTime(new Date(2_000L));
        document.setStorageObjectId(StorageObjectId.of(8L));
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        document.setContentUpdatedAt(new Date(1_000L));

        String snapshotJson = assembler.toSnapshotJson(document);

        assertEquals(
                "{\"contentType\":\"WANGQI_DOCUMENT\",\"contentId\":200,"
                        + "\"contentUpdatedAt\":\"1970-01-01T00:00:01Z\",\"title\":\"王圻\","
                        + "\"summary\":\"摘要\",\"contentFormat\":\"MARKDOWN\",\"content\":\"正文\","
                        + "\"documentTime\":\"1970-01-01T00:00:02Z\",\"storageObjectId\":8,"
                        + "\"visibility\":\"PUBLIC\"}",
                snapshotJson);
    }

    @Test
    void shouldAssembleMingCustomsVersionSnapshotJson() {
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryId.of(300L));
        entry.setTitle("明俗");
        entry.setCategory("岁时");
        entry.setChapter("卷一");
        entry.setSection("元日");
        entry.setSummary("摘要");
        entry.setContentFormat(MingCustomsContentFormat.TEXT);
        entry.setContent("正文");
        entry.setOriginalExcerpts("摘录");
        entry.setVisibility(MingCustomsVisibility.PUBLIC);
        entry.setContentUpdatedAt(new Date(1_000L));

        String snapshotJson = assembler.toSnapshotJson(entry);

        assertEquals(
                "{\"contentType\":\"MING_CUSTOMS\",\"contentId\":300,"
                        + "\"contentUpdatedAt\":\"1970-01-01T00:00:01Z\",\"title\":\"明俗\","
                        + "\"category\":\"岁时\",\"chapter\":\"卷一\",\"section\":\"元日\","
                        + "\"summary\":\"摘要\",\"contentFormat\":\"TEXT\",\"content\":\"正文\","
                        + "\"originalExcerpts\":\"摘录\",\"visibility\":\"PUBLIC\"}",
                snapshotJson);
    }
}
