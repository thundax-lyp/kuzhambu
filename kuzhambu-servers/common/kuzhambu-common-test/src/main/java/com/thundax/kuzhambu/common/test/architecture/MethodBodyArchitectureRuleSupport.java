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

public final class MethodBodyArchitectureRuleSupport {

    private static final Pattern METHOD_DECLARATION_PATTERN =
            Pattern.compile("((?:\\s*@[^\\n]+\\n)*)\\s*(private|protected)\\s+(?:static\\s+)?(?:final\\s+)?"
                    + "(?:<[\\w, ? extends super]+>\\s+)?[\\w.$<>\\[\\], ? extends super]+\\s+"
                    + "([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;{}]*\\)\\s*(?:throws\\s+[\\w.,\\s]+)?\\{");
    private static final Pattern ALLOW_ANNOTATION_PATTERN =
            Pattern.compile("@(?:[\\w.]+\\.)?OneLineMethodAllowed\\s*\\(([^)]*)\\)");
    private static final Pattern ALLOW_REASON_PATTERN = Pattern.compile("\\breason\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern TARGET_CLASS_NAME_PATTERN =
            Pattern.compile("(Controller|ServiceImpl|DaoImpl)\\.java$");

    private MethodBodyArchitectureRuleSupport() {}

    public static void assertPrivateAndProtectedMethodsAreNotOneLine(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path ->
                            ArchitectureSourceSupport.normalizePath(path).contains("/src/main/java/"))
                    .filter(MethodBodyArchitectureRuleSupport::isTargetClass)
                    .forEach(path -> collectOneLineMethodViolations(root, path, violations));
        }

        assertTrue(
                "Controller, ServiceImpl and DaoImpl private/protected methods must not contain only one executable "
                        + "line without local reuse unless annotated with @OneLineMethodAllowed(reason = \"...\"). "
                        + "Violations: "
                        + violations,
                violations.isEmpty());
    }

    private static boolean isTargetClass(Path path) {
        return TARGET_CLASS_NAME_PATTERN.matcher(path.getFileName().toString()).find();
    }

    private static void collectOneLineMethodViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher matcher = METHOD_DECLARATION_PATTERN.matcher(content);
        while (matcher.find()) {
            String annotations = matcher.group(1);
            String visibility = matcher.group(2);
            String methodName = matcher.group(3);
            int bodyStart = matcher.end() - 1;
            int bodyEnd = methodBodyEnd(content, bodyStart);
            if (bodyEnd < 0) {
                continue;
            }
            String body = content.substring(bodyStart + 1, bodyEnd);
            if (executableLineCount(body) == 1
                    && referenceCount(content, methodName) <= 2
                    && !hasAcceptedAllowAnnotation(annotations)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path)
                        + " method="
                        + visibility
                        + " "
                        + methodName);
            }
        }
    }

    private static int methodBodyEnd(String content, int bodyStart) {
        int depth = 0;
        for (int i = bodyStart; i < content.length(); i++) {
            char current = content.charAt(i);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int executableLineCount(String body) {
        int count = 0;
        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() > 0) {
                count++;
            }
        }
        return count;
    }

    private static int referenceCount(String content, String methodName) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(");
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static boolean hasAcceptedAllowAnnotation(String annotations) {
        Matcher annotationMatcher = ALLOW_ANNOTATION_PATTERN.matcher(annotations);
        if (!annotationMatcher.find()) {
            return false;
        }
        Matcher reasonMatcher = ALLOW_REASON_PATTERN.matcher(annotationMatcher.group(1));
        return reasonMatcher.find() && reasonMatcher.group(1).trim().length() > 0;
    }
}
