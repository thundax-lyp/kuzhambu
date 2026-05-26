package com.thundax.kuzhambu.common.test.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class IntegrationOssCleaner {

    private final Path rootPath;

    public IntegrationOssCleaner(Path rootPath) {
        this.rootPath = rootPath;
    }

    public void clean() {
        if (rootPath == null) {
            throw new IllegalArgumentException("OSS integration root path must not be null.");
        }
        try {
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
                return;
            }
            Files.list(rootPath).forEach(this::deleteRecursively);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clean integration OSS root: " + rootPath, e);
        }
    }

    private void deleteRecursively(Path path) {
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).forEach(this::deleteIfExists);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clean integration OSS path: " + path, e);
        }
    }

    private void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete integration OSS path: " + path, e);
        }
    }
}
