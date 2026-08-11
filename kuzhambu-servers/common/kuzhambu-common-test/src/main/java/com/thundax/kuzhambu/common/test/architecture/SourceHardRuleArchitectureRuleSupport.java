package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class SourceHardRuleArchitectureRuleSupport {

    private static final Pattern NON_JACKSON_JSON_REFERENCE_PATTERN =
            Pattern.compile("\\b(?:com\\.alibaba\\.fastjson|com\\.google\\.gson|net\\.sf\\.json|org\\.json)\\.");
    private static final Pattern TOP_LEVEL_TOOL_PACKAGE_PATTERN = Pattern.compile(
            "(?m)^\\s*package\\s+com\\.thundax\\.kuzhambu\\.(?:ai|classics|discovery|knowledge|operations|"
                    + "storage|system)\\.(?:application\\.(?:misc|util|utils)|(?:domain|infra|interfaces)"
                    + "\\.(?:misc|util|utils|helper))(?:\\.|\\s*;)");
    private static final Pattern ILLEGAL_ARGUMENT_EXCEPTION_BUSINESS_EXIT_PATTERN = Pattern.compile(
            "(?ms)^\\s*package\\s+com\\.thundax\\.kuzhambu\\.(?:ai|classics|discovery|knowledge|operations|"
                    + "storage|system)\\.(?:application(?:\\.|;)|infra\\.[\\w.]+\\.repository\\.impl(?:\\.|;)).*"
                    + "throw\\s+new\\s+(?:java\\.lang\\.)?IllegalArgumentException\\s*\\(");
    private static final Pattern ILLEGAL_ARGUMENT_EXCEPTION_VARIABLE_PATTERN =
            Pattern.compile("(?:java\\.lang\\.)?IllegalArgumentException\\s+(\\w+)\\b");
    private static final Pattern THROW_VARIABLE_PATTERN = Pattern.compile("\\bthrow\\s+(\\w+)\\s*;");
    private static final Pattern DOMAIN_EXCEPTION_EXIT_PATTERN = exceptionExitPattern("DomainException");
    private static final Pattern BIZ_EXCEPTION_EXIT_PATTERN = exceptionExitPattern("BizException");
    private static final Pattern API_EXCEPTION_EXIT_PATTERN = exceptionExitPattern("ApiException");
    private static final Pattern COMMENTS_AND_LITERALS_PATTERN =
            Pattern.compile("(?s)/\\*.*?\\*/|//[^\\r\\n]*|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'");

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
                SourceHardRuleArchitectureRuleSupport::hasIllegalArgumentExceptionBusinessExit,
                "Application and repository implementation sources must not throw IllegalArgumentException");
    }

    public static void assertBusinessLayersUseBoundedExceptionTypes(Path sourceRoot) throws IOException {
        assertNoSourceMatches(
                sourceRoot,
                SourceHardRuleArchitectureRuleSupport::hasExceptionBoundaryViolation,
                "Business production sources must throw DomainException only from domain, BizException only from application, and ApiException only from interfaces");
    }

    private static void assertNoSourceMatches(Path sourceRoot, Pattern pattern, String message) throws IOException {
        assertNoSourceMatches(sourceRoot, path -> sourceMatches(path, pattern), message);
    }

    private static void assertNoSourceMatches(Path sourceRoot, SourceMatcher matcher, String message)
            throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(SourceHardRuleArchitectureRuleSupport::isProductionJavaSource)
                    .filter(matcher::matches)
                    .map(path -> ArchitectureSourceSupport.repositoryPath(root, path))
                    .forEach(violations::add);
        }

        assertTrue(message + ": " + violations, violations.isEmpty());
    }

    private static boolean sourceMatches(Path path, Pattern pattern) {
        try {
            return pattern.matcher(withoutCommentsAndLiterals(Files.readString(path)))
                    .find();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read source file " + path, exception);
        }
    }

    private static boolean isProductionJavaSource(Path path) {
        return ArchitectureSourceSupport.normalizePath(path.toAbsolutePath()).contains("/src/main/java/");
    }

    private static boolean hasIllegalArgumentExceptionBusinessExit(Path path) {
        try {
            String source = withoutCommentsAndLiterals(Files.readString(path));
            if (ILLEGAL_ARGUMENT_EXCEPTION_BUSINESS_EXIT_PATTERN.matcher(source).find()) {
                return true;
            }
            if (!isApplicationOrRepositoryImplementationSource(source)) {
                return false;
            }
            Set<String> exceptionVariables = new HashSet<String>();
            Matcher declarationMatcher = ILLEGAL_ARGUMENT_EXCEPTION_VARIABLE_PATTERN.matcher(source);
            while (declarationMatcher.find()) {
                exceptionVariables.add(declarationMatcher.group(1));
            }
            Matcher throwMatcher = THROW_VARIABLE_PATTERN.matcher(source);
            while (throwMatcher.find()) {
                if (exceptionVariables.contains(throwMatcher.group(1))) {
                    return true;
                }
            }
            return false;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read source file " + path, exception);
        }
    }

    private static boolean hasExceptionBoundaryViolation(Path path) {
        try {
            String source = withoutCommentsAndLiterals(Files.readString(path));
            return (isBusinessLayerSource(source, "domain")
                            && (hasExceptionExit(source, "BizException", BIZ_EXCEPTION_EXIT_PATTERN)
                                    || hasExceptionExit(source, "ApiException", API_EXCEPTION_EXIT_PATTERN)))
                    || (isBusinessLayerSource(source, "application")
                            && (hasExceptionExit(source, "DomainException", DOMAIN_EXCEPTION_EXIT_PATTERN)
                                    || hasExceptionExit(source, "ApiException", API_EXCEPTION_EXIT_PATTERN)))
                    || (isBusinessLayerSource(source, "interfaces")
                            && (hasExceptionExit(source, "DomainException", DOMAIN_EXCEPTION_EXIT_PATTERN)
                                    || hasExceptionExit(source, "BizException", BIZ_EXCEPTION_EXIT_PATTERN)));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read source file " + path, exception);
        }
    }

    private static boolean isApplicationOrRepositoryImplementationSource(String source) {
        return source.matches(
                "(?s)^\\s*package\\s+com\\.thundax\\.kuzhambu\\.(?:ai|classics|discovery|knowledge|operations|"
                        + "storage|system)\\.(?:application(?:\\.|;)|infra\\.[\\w.]+\\.repository\\.impl(?:\\.|;)).*");
    }

    private static boolean isBusinessLayerSource(String source, String layer) {
        return source.matches(
                "(?s)^\\s*package\\s+com\\.thundax\\.kuzhambu\\.(?:ai|classics|discovery|knowledge|operations|"
                        + "storage|system)\\."
                        + layer
                        + "(?:\\.|;).*");
    }

    private static Pattern exceptionExitPattern(String exceptionType) {
        return Pattern.compile("\\bthrow\\s+new\\s+(?:(?:[A-Za-z_$][\\w$]*\\.)*)?" + exceptionType + "\\s*\\(");
    }

    private static boolean hasExceptionExit(String source, String exceptionType, Pattern directExitPattern) {
        if (directExitPattern.matcher(source).find()) {
            return true;
        }
        Pattern variablePattern =
                Pattern.compile("\\bcatch\\s*\\(\\s*(?:(?:[A-Za-z_$][\\w$]*\\.)*)?" + exceptionType + "\\s+(\\w+)\\b");
        Set<String> exceptionVariables = new HashSet<String>();
        Matcher declarationMatcher = variablePattern.matcher(source);
        while (declarationMatcher.find()) {
            exceptionVariables.add(declarationMatcher.group(1));
        }
        Matcher throwMatcher = THROW_VARIABLE_PATTERN.matcher(source);
        while (throwMatcher.find()) {
            if (exceptionVariables.contains(throwMatcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    private static String withoutCommentsAndLiterals(String source) {
        return COMMENTS_AND_LITERALS_PATTERN.matcher(source).replaceAll(" ");
    }

    @FunctionalInterface
    private interface SourceMatcher {

        boolean matches(Path path);
    }
}
