package com.thundax.kuzhambu.common.test.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class IntegrationOssCleanerTest {

    @TempDir
    private Path tempDir;

    @Test
    public void shouldCleanChildrenButKeepRootDirectory() throws IOException {
        Path nestedDirectory = tempDir.resolve("bucket").resolve("nested");
        Files.createDirectories(nestedDirectory);
        Files.writeString(nestedDirectory.resolve("object.txt"), "content");

        new IntegrationOssCleaner(tempDir).clean();

        assertTrue(Files.exists(tempDir));
        assertFalse(Files.exists(tempDir.resolve("bucket")));
    }
}
