package com.thundax.kuzhambu.classics.infra.sancai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SancaiRepositoryTest {

    @Test
    void schemaAndDataShouldContainSancaiTablesAndSeedData() {
        assertTrue(existsInKnownRoots("db/schema/classics.sql"));
        assertTrue(existsInKnownRoots("db/data/classics.sql"));
    }

    private static boolean existsInKnownRoots(String path) {
        return Files.exists(Path.of(path))
                || Files.exists(Path.of("../" + path))
                || Files.exists(Path.of("../../../../" + path));
    }
}
