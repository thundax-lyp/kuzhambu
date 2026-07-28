package com.thundax.kuzhambu.classics.application.content.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.codec.MingCustomsEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsContentFormat;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.enums.MingCustomsVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryImageIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntryImage;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.storage.facade.dto.StorageObjectFacadeDto;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsContentSnapshotAssemblerTest {
    private final ClassicsContentSnapshotAssembler assembler = new ClassicsContentSnapshotAssembler();

    @Test
    void shouldAssembleSancaiEntryVersionSnapshotJson() {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(100L));
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(2L));
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
                        + "\"visualAssetStatus\":\"PROCESSING\",\"refinementStatus\":\"COMPLETE\",\"priority\":7,"
                        + "\"images\":[]}",
                snapshotJson);
    }

    @Test
    void shouldAssembleAllSancaiImagesInPriorityOrder() {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(100L));
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(2L));
        entry.setTitle("三才");
        entry.setPriority(7);

        String snapshotJson = assembler.toSnapshotJson(
                entry,
                List.of(
                        image(21L, 801L, "旧图", false, 1),
                        image(22L, 802L, "次图", true, 2),
                        image(23L, 803L, "首图", true, 1)));

        assertEquals(
                "{\"contentType\":\"SANCAI_ENTRY\",\"contentId\":100,\"contentUpdatedAt\":null,"
                        + "\"volumeId\":2,\"title\":\"三才\",\"originalText\":null,\"translationText\":null,"
                        + "\"summary\":null,\"lifecycleStatus\":null,\"visibility\":null,"
                        + "\"translationStatus\":null,\"imageStatus\":null,\"visualAssetStatus\":null,"
                        + "\"refinementStatus\":null,\"priority\":7,"
                        + "\"images\":["
                        + "{\"imageId\":21,\"storageObjectId\":801,\"originalFilename\":null,"
                        + "\"contentType\":null,\"size\":null,\"imageType\":\"ORIGINAL\","
                        + "\"title\":\"旧图\",\"currentUsed\":false,\"priority\":1},"
                        + "{\"imageId\":23,\"storageObjectId\":803,\"originalFilename\":null,"
                        + "\"contentType\":null,\"size\":null,\"imageType\":\"ORIGINAL\","
                        + "\"title\":\"首图\",\"currentUsed\":true,\"priority\":1},"
                        + "{\"imageId\":22,\"storageObjectId\":802,\"originalFilename\":null,"
                        + "\"contentType\":null,\"size\":null,\"imageType\":\"ORIGINAL\","
                        + "\"title\":\"次图\",\"currentUsed\":true,\"priority\":2}"
                        + "]}",
                snapshotJson);
    }

    @Test
    void shouldAssembleSancaiImagesWithStorageMetadata() {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(100L));
        entry.setTitle("三才");
        entry.setPriority(7);
        SancaiEntryImage currentImage = image(22L, 802L, "次图", true, 2);
        SancaiEntryImage inactiveImage = image(21L, 801L, "旧图", false, 1);

        String snapshotJson = assembler.toSnapshotJsonWithImageResources(
                entry,
                List.of(
                        SancaiEntryVersionSnapshot.ImageResource.from(currentImage, storage()),
                        SancaiEntryVersionSnapshot.ImageResource.from(inactiveImage, null)));

        assertEquals(
                "{\"contentType\":\"SANCAI_ENTRY\",\"contentId\":100,\"contentUpdatedAt\":null,"
                        + "\"volumeId\":null,\"title\":\"三才\",\"originalText\":null,\"translationText\":null,"
                        + "\"summary\":null,\"lifecycleStatus\":null,\"visibility\":null,"
                        + "\"translationStatus\":null,\"imageStatus\":null,\"visualAssetStatus\":null,"
                        + "\"refinementStatus\":null,\"priority\":7,"
                        + "\"images\":["
                        + "{\"imageId\":21,\"storageObjectId\":801,\"originalFilename\":null,"
                        + "\"contentType\":null,\"size\":null,\"imageType\":\"ORIGINAL\","
                        + "\"title\":\"旧图\",\"currentUsed\":false,\"priority\":1},"
                        + "{\"imageId\":22,\"storageObjectId\":802,\"originalFilename\":\"三才图.png\","
                        + "\"contentType\":\"image/png\",\"size\":9,\"imageType\":\"ORIGINAL\","
                        + "\"title\":\"次图\",\"currentUsed\":true,\"priority\":2}"
                        + "]}",
                snapshotJson);
    }

    @Test
    void shouldAssembleWangqiDocumentVersionSnapshotJson() {
        WangqiDocument document = new WangqiDocument();
        document.setId(WangqiDocumentIdCodec.toDomain(200L));
        document.setTitle("王圻");
        document.setSummary("摘要");
        document.setContentFormat(WangqiContentFormat.MARKDOWN);
        document.setContent("正文");
        document.setDocumentTime(new Date(2_000L));
        document.setStorageObjectId(StorageObjectIdCodec.toDomain(8L));
        document.setVisibility(WangqiDocumentVisibility.PUBLIC);
        document.setContentUpdatedAt(new Date(1_000L));

        String snapshotJson = assembler.toSnapshotJson(document);

        assertEquals(
                "{\"contentType\":\"WANGQI_DOCUMENT\",\"contentId\":200,"
                        + "\"contentUpdatedAt\":\"1970-01-01T00:00:01Z\",\"title\":\"王圻\","
                        + "\"summary\":\"摘要\",\"contentFormat\":\"MARKDOWN\",\"content\":\"正文\","
                        + "\"documentTime\":\"1970-01-01T00:00:02Z\",\"storageObjectId\":8,"
                        + "\"visibility\":\"PUBLIC\",\"tags\":[],\"qaPairs\":[]}",
                snapshotJson);
    }

    @Test
    void shouldAssembleMingCustomsVersionSnapshotJson() {
        MingCustomsEntry entry = new MingCustomsEntry();
        entry.setId(MingCustomsEntryIdCodec.toDomain(300L));
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
                        + "\"originalExcerpts\":\"摘录\",\"visibility\":\"PUBLIC\",\"tags\":[],\"qaPairs\":[]}",
                snapshotJson);
    }

    private static SancaiEntryImage image(
            Long imageId, Long storageObjectId, String title, boolean currentUsed, int priority) {
        SancaiEntryImage image = new SancaiEntryImage();
        image.setId(SancaiEntryImageIdCodec.toDomain(imageId));
        image.setEntryId(SancaiEntryIdCodec.toDomain(100L));
        image.setStorageObjectId(StorageObjectIdCodec.toDomain(storageObjectId));
        image.setImageType(SancaiEntryImageType.ORIGINAL);
        image.setTitle(title);
        image.setCurrentUsed(currentUsed);
        image.setPriority(priority);
        return image;
    }

    private static StorageObjectFacadeDto storage() {
        return StorageObjectFacadeDto.builder()
                .id(802L)
                .originalFilename("三才图.png")
                .contentType("image/png")
                .size(9L)
                .build();
    }
}
