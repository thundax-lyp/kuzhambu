package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SourceHardRuleArchitectureRuleSupport {

    private static final Pattern NON_JACKSON_JSON_REFERENCE_PATTERN =
            Pattern.compile("\\b(?:com\\.alibaba\\.fastjson|com\\.google\\.gson|net\\.sf\\.json|org\\.json)\\.");
    private static final Pattern TOP_LEVEL_TOOL_PACKAGE_PATTERN = Pattern.compile(
            "(?m)^\\s*package\\s+com\\.thundax\\.kuzhambu\\.(?:ai|classics|discovery|knowledge|operations|"
                    + "storage|system)\\.(?:application\\.(?:misc|util|utils)|(?:domain|infra|interfaces)"
                    + "\\.(?:misc|util|utils|helper))\\s*;");
    private static final Pattern ILLEGAL_ARGUMENT_EXCEPTION_BUSINESS_EXIT_PATTERN = Pattern.compile(
            "(?ms)^\\s*package\\s+com\\.thundax\\.kuzhambu\\.(?:ai|classics|discovery|knowledge|operations|"
                    + "storage|system)\\.(?:application(?:\\.|;)|infra\\.[\\w.]+\\.repository\\.impl(?:\\.|;)).*"
                    + "throw\\s+new\\s+IllegalArgumentException\\s*\\(");

    private SourceHardRuleArchitectureRuleSupport() {}

    public static void assertProductionSourcesUseJacksonJsonOnly(Path sourceRoot) throws IOException {
        assertNoSourceMatches(
                sourceRoot,
                NON_JACKSON_JSON_REFERENCE_PATTERN,
                "Production sources must not directly use non-Jackson JSON libraries");
    }

    public static void assertBusinessLayersDoNotUseTopLevelToolPackages(Path sourceRoot) throws IOException {
        assertNoSourceMatches(
                sourceRoot,
                TOP_LEVEL_TOOL_PACKAGE_PATTERN,
                "Business production sources must not use top-level misc, util, utils, or helper packages");
    }

    public static void assertApplicationAndRepositoryImplementationsDoNotUseIllegalArgumentException(Path sourceRoot)
            throws IOException {
        assertNoSourceMatches(
                sourceRoot,
                ILLEGAL_ARGUMENT_EXCEPTION_BUSINESS_EXIT_PATTERN,
                "Application and repository implementation sources must not throw IllegalArgumentException");
    }

    private static void assertNoSourceMatches(Path sourceRoot, Pattern pattern, String message) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path ->
                            ArchitectureSourceSupport.normalizePath(path).contains("/src/main/java/"))
                    .filter(path -> sourceMatches(path, pattern))
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(message + ": " + violations, violations.isEmpty());
    }

    private static boolean sourceMatches(Path path, Pattern pattern) {
        try {
            return pattern.matcher(Files.readString(path)).find();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read source file " + path, exception);
        }
    }
}
