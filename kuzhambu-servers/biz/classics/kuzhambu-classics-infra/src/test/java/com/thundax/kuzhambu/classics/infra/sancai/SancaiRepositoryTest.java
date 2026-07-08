package com.thundax.kuzhambu.classics.infra.sancai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class SancaiRepositoryTest {

    @Test
    void schemaAndDataShouldContainSancaiTablesAndSeedData() {
        assertTrue(existsInKnownRoots("db/schema/classics.sql"));
        assertTrue(existsInKnownRoots("db/data/classics.sql"));
    }

    @Test
    void seedDataStatusValuesShouldBeAcceptedByDomainEnums() {
        SancaiCategoryType.from("AUXILIARY");
        SancaiCategoryType.from("FORMAL");
        SancaiVolumeType.from("AUXILIARY");
        SancaiVolumeType.from("MAIN");
        SancaiEntryTranslationStatus.from("MISSING");
        SancaiEntryTranslationStatus.from("READY");
        SancaiEntryImageStatus.from("MISSING");
        SancaiEntryImageStatus.from("READY");
        SancaiEntryVisualAssetStatus.from("MISSING");
        SancaiEntryVisualAssetStatus.from("READY");
        SancaiEntryRefinementStatus.from("RAW");
        SancaiEntryRefinementStatus.from("COMPLETE");
        SancaiShowcaseStatus.from("REQUESTED");
        SancaiShowcaseStatus.from("PROCESSING");
        SancaiShowcaseStatus.from("COMPLETED");
        SancaiShowcaseStatus.from("FAILED");
        SancaiShowcaseStatus.from("EXPIRED");
        SancaiVisibilityRiskStatus.from("PUBLIC_ONLY");
        SancaiVisibilityRiskStatus.from("CONTAINS_PRIVATE");
    }

    @Test
    void assetRepositoryShouldKeepEntryScopedImageCurrentAndSortOperations() {
        String repositorySource = readFromKnownRoots(
                "biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java");

        assertTrue(
                repositorySource.contains("listImagesByEntryId(SancaiEntryId entryId, SortDirection sortDirection)"));
        assertTrue(
                repositorySource.contains(".eq(SancaiEntryImageDO::getEntryId, SancaiEntryIdCodec.toValue(entryId))"));
        assertTrue(repositorySource.contains("clearCurrentImagesByEntryId(SancaiEntryId entryId)"));
        assertTrue(repositorySource.contains(".set(SancaiEntryImageDO::getCurrentUsed, false)"));
        assertTrue(repositorySource.contains("markImageCurrent(SancaiEntryId entryId, SancaiEntryImageId imageId)"));
        assertTrue(
                repositorySource.contains(".eq(SancaiEntryImageDO::getId, SancaiEntryImageIdCodec.toValue(imageId))"));
        assertTrue(repositorySource.contains(".set(SancaiEntryImageDO::getCurrentUsed, true)"));
    }

    @Test
    void assetRepositoryShouldKeepShowcaseJobMetadataAndFilters() {
        String repositorySource = readFromKnownRoots(
                "biz/classics/kuzhambu-classics-infra/src/main/java/com/thundax/kuzhambu/classics/infra/sancai/repository/impl/SancaiAssetRepositoryImpl.java");

        assertTrue(repositorySource.contains(".set(SancaiShowcaseDO::getCompletedAt, new Date())"));
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

    private static boolean existsInKnownRoots(String path) {
        return Files.exists(Path.of(path))
                || Files.exists(Path.of("../" + path))
                || Files.exists(Path.of("../../../../" + path));
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
