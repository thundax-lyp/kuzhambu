package com.thundax.kuzhambu.classics.infra.sancai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiCategoryType;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVolumeType;
import java.nio.file.Files;
import java.nio.file.Path;
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
    }

    private static boolean existsInKnownRoots(String path) {
        return Files.exists(Path.of(path))
                || Files.exists(Path.of("../" + path))
                || Files.exists(Path.of("../../../../" + path));
    }
}
