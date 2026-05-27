package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SortableArchitectureRuleSupport {

    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "\\bprivate\\s+(?:static\\s+final\\s+)?[A-Za-z0-9_<>, ?\\.\\[\\]]+\\s+([A-Za-z][A-Za-z0-9_]*)\\s*(?:=[^;]*)?;");
    private static final Pattern SORT_METHOD_PATTERN = Pattern.compile(
            "((?:\\s*@[^\\n]+\\n)+)\\s*public\\s+void\\s+sort\\s*\\(\\s*([A-Za-z0-9_]+SortCommand)\\s+command\\s*\\)");
    private static final Pattern UPDATE_METHOD_PATTERN =
            Pattern.compile("public\\s+int\\s+update\\s*\\([^)]*\\)\\s*\\{(.*?)\\n\\s*\\}", Pattern.DOTALL);
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`([^`]+)`\\s*\\((.*?)\\)\\s*ENGINE",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private SortableArchitectureRuleSupport() {}

    public static void assertSortCommandsUseOrderedIdsOnly(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("SortCommand.java"))
                    .forEach(path -> collectSortCommandFieldViolations(root, path, violations));
        }

        assertTrue("Sort commands must only expose orderedIds and sortDirection: " + violations, violations.isEmpty());
    }

    public static void assertSortMethodsAreTransactional(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("ServiceImpl.java"))
                    .forEach(path -> collectSortMethodTransactionViolations(root, path, violations));
        }

        assertTrue("Sort service methods must be transactional: " + violations, violations.isEmpty());
    }

    public static void assertFlatSortSchemasDeclarePriorityUnique(Path schemaRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();
        String schemaContent = readSchemaContent(schemaRoot);
        Matcher matcher = TABLE_PATTERN.matcher(schemaContent);
        while (matcher.find()) {
            String tableName = matcher.group(1);
            String tableBody = matcher.group(2);
            if (tableBody.contains("`priority`") && !tableBody.contains("UNIQUE KEY")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, schemaRoot) + " table=" + tableName);
            }
        }

        assertTrue("FlatSort tables must declare unique priority keys: " + violations, violations.isEmpty());
    }

    public static void assertSortableRepositoryNormalUpdateDoesNotSetPriority(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("RepositoryImpl.java"))
                    .forEach(path -> collectSortableRepositoryUpdatePriorityViolations(root, path, violations));
        }

        assertTrue("Sortable Repository normal update must not set priority: " + violations, violations.isEmpty());
    }

    private static void collectSortCommandFieldViolations(Path root, Path path, List<String> violations) {
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

    private static void collectSortMethodTransactionViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher matcher = SORT_METHOD_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String commandName = matcher.group(2);
            if (!annotations.contains("@Transactional")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " command=" + commandName);
            }
        }
    }

    private static void collectSortableRepositoryUpdatePriorityViolations(
            Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        if (!content.contains("updatePriority")) {
            return;
        }
        Matcher matcher = UPDATE_METHOD_PATTERN.matcher(content);
        while (matcher.find()) {
            String methodBody = matcher.group(1);
            if (methodBody.contains("::getPriority")) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path));
            }
        }
    }

    private static String readSchemaContent(Path schemaRoot) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> paths = Files.walk(schemaRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .forEach(path -> builder.append(ArchitectureSourceSupport.readSource(path))
                            .append('\n'));
        }
        return builder.toString();
    }
}
