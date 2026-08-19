package com.thundax.kuzhambu.classics.infra.sancai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class SancaiRepositoryTest {

    @Test
    void assetRepositoryShouldKeepEntryScopedImageCurrentAndSortOperations() {
        String repositorySource = readFromKnownRoots(
                "biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java");

        assertTrue(
                repositorySource.contains("listImagesByEntryId(SancaiEntryId entryId, SortDirection sortDirection)"));
        assertTrue(
                repositorySource.contains(".eq(SancaiEntryImageDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))"));
        assertTrue(repositorySource.contains("updateCurrentImagesClearedByEntryId(SancaiEntryId entryId)"));
        assertTrue(repositorySource.contains(".set(SancaiEntryImageDO::getCurrentUsed, false)"));
        assertTrue(repositorySource.contains("updateImageCurrent(SancaiEntryId entryId, SancaiEntryImageId imageId)"));
        assertTrue(
                repositorySource.contains(".eq(SancaiEntryImageDO::getId, SancaiEntryImageIdCodec.toValue(imageId))"));
        assertTrue(repositorySource.contains(".set(SancaiEntryImageDO::getCurrentUsed, true)"));
    }

    @Test
    void assetRepositoryShouldKeepShowcaseJobMetadataAndFilters() {
        String repositorySource = readFromKnownRoots(
                "biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java");

        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getCompletedAt, Instant.now())"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getStorageObjectId"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getAssetCount, assetCount)"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getFilename, filename)"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getContentType, contentType)"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getSizeBytes, sizeBytes)"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getSha256, sha256)"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getFailureType, failureType)"));
        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getFailureMessage, failureMessage)"));
        assertTrue(repositorySource.contains("appendKeyword(wrapper, keyword)"));
        assertTrue(repositorySource.contains(".like(SancaiShowcaseDO::getScopeTitle, normalizedKeyword)"));
        assertTrue(repositorySource.contains(".like(SancaiShowcaseDO::getFilename, normalizedKeyword)"));
        assertTrue(repositorySource.contains(
                ".ge(requestedAtStart != null, SancaiShowcaseDO::getRequestedAt, requestedAtStart)"));
        assertTrue(repositorySource.contains(
                ".le(requestedAtEnd != null, SancaiShowcaseDO::getRequestedAt, requestedAtEnd)"));
        assertTrue(repositorySource.contains(".orderByDesc(SancaiShowcaseDO::getRequestedAt)"));
    }

    @Test
    void pageEntriesShouldApplyCategoryVolumeFilter() {
        String repositorySource = readFromKnownRoots(
                "biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiRepositoryImpl.java");

        assertTrue(repositorySource.contains("List<Long> categoryVolumeIds = listVolumeIdsByCategory(categoryId);"));
        assertTrue(repositorySource.contains("categoryId != null && categoryVolumeIds.isEmpty()"));
        assertTrue(repositorySource.contains("!categoryVolumeIds.contains(SancaiVolumeIdCodec.toValue(volumeId))"));
        assertTrue(repositorySource.contains(
                ".in(volumeId == null && categoryId != null, SancaiEntryDO::getVolumeId, categoryVolumeIds)"));
    }

    private static String readFromKnownRoots(String path) {
        for (Path candidate : List.of(
                Path.of(path), Path.of("../" + path), Path.of("../../../" + path), Path.of("../../../../" + path))) {
            if (Files.exists(candidate)) {
                try {
                    return Files.readString(candidate);
                } catch (Exception exception) {
                    throw new AssertionError("Unable to read " + candidate, exception);
                }
            }
        }
        throw new AssertionError("Missing file: " + path);
    }
}
