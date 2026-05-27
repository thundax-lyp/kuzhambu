package com.thundax.kuzhambu.classics.infra.sancai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SancaiRepositoryTest {

    @Test
    void schemaAndDataShouldContainSancaiTablesAndSeedData() {
        assertTrue(Files.exists(Path.of("../db/schema/classics.sql")) || Files.exists(Path.of("db/schema/classics.sql")));
        assertTrue(Files.exists(Path.of("../db/data/classics.sql")) || Files.exists(Path.of("db/data/classics.sql")));
    }
}
