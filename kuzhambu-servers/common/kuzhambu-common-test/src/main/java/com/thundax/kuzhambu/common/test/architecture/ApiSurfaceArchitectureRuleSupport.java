package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ApiSurfaceArchitectureRuleSupport {

    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "\\bprivate\\s+(?:static\\s+final\\s+)?[A-Za-z0-9_<>, ?\\.\\[\\]]+\\s+([A-Za-z][A-Za-z0-9_]*)\\s*(?:=[^;]*)?;");
    private static final Pattern PRIORITY_PROPERTY_PATTERN = Pattern.compile(
            "\\bpriority\\b|@JsonProperty\\s*\\(\\s*\"priority\"\\s*\\)|\\bgetPriority\\s*\\(|\\bsetPriority\\s*\\(");
    private static final Pattern SORT_MAPPING_PATTERN =
            Pattern.compile("@PostMapping\\s*\\(\\s*(?:value\\s*=\\s*)?\"/?sort\"\\s*\\)");
    private static final Pattern CLASS_DECLARATION_PATTERN =
            Pattern.compile("\\bpublic\\s+class\\s+([A-Za-z0-9_]+)\\b");

    private ApiSurfaceArchitectureRuleSupport() {}

    public static void assertApiModelsDoNotExposePriority(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(ApiSurfaceArchitectureRuleSupport::isRequestOrResponseSource)
                    .forEach(path -> collectPriorityExposureViolations(root, path, violations));
        }

        assertTrue("API request/response models must not expose priority: " + violations, violations.isEmpty());
    }

    public static void assertSortRequestsUseOrderedIdsOnly(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("SortRequest.java"))
                    .forEach(path -> collectSortRequestFieldViolations(root, path, violations));
        }

        assertTrue("Sort requests must only expose orderedIds and sortDirection: " + violations, violations.isEmpty());
    }

    public static void assertSortRequestsAreAllowed(Path sourceRoot, Collection<String> allowedRequestNames)
            throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("SortRequest.java"))
                    .forEach(
                            path -> collectSortRequestAllowlistViolations(root, path, allowedRequestNames, violations));
        }

        assertTrue("Sort request classes must be explicitly allowed: " + violations, violations.isEmpty());
    }

    public static void assertSortEndpointsAreAllowed(Path sourceRoot, Collection<String> allowedControllerNames)
            throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .forEach(path ->
                            collectSortEndpointAllowlistViolations(root, path, allowedControllerNames, violations));
        }

        assertTrue("Sort endpoints must be explicitly allowed: " + violations, violations.isEmpty());
    }

    private static boolean isRequestOrResponseSource(Path path) {
        String normalized = ArchitectureSourceSupport.normalizePath(path);
        return path.getFileName().toString().endsWith(".java")
                && (normalized.contains("/controller/request/") || normalized.contains("/controller/response/"));
    }

    private static void collectPriorityExposureViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        if (PRIORITY_PROPERTY_PATTERN.matcher(content).find()) {
            violations.add(ArchitectureSourceSupport.repositoryPath(root, path));
        }
    }

    private static void collectSortRequestFieldViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher matcher = FIELD_PATTERN.matcher(content);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (!"orderedIds".equals(fieldName)
                    && !"sortDirection".equals(fieldName)
                    && !"serialVersionUID".equals(fieldName)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " field=" + fieldName);
            }
        }
    }

    private static void collectSortRequestAllowlistViolations(
            Path root, Path path, Collection<String> allowedRequestNames, List<String> violations) {
        String className = path.getFileName().toString().replace(".java", "");
        if (!allowedRequestNames.contains(className)) {
            violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
        }
    }

    private static void collectSortEndpointAllowlistViolations(
            Path root, Path path, Collection<String> allowedControllerNames, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        if (!SORT_MAPPING_PATTERN.matcher(content).find()) {
            return;
        }
        String className = className(content);
        if (!allowedControllerNames.contains(className)) {
            violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " class=" + className);
        }
    }

    private static String className(String content) {
        Matcher matcher = CLASS_DECLARATION_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : "<unknown>";
    }
}
