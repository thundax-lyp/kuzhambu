package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static final Pattern CLASS_EXTENDS_PATTERN = Pattern.compile(
            "\\bclass\\s+([A-Za-z_$][\\w$]*)\\s+extends\\s+(?:(?:[A-Za-z_$][\\w$]*\\.)*)([A-Za-z_$][\\w$]*)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+([A-Za-z_$][\\w$]*)\\b");
    private static final Pattern STATIC_METHOD_PATTERN = Pattern.compile(
            "(?m)^\\s*(?:public|protected|private)?\\s*static\\s+(?:(?:[A-Za-z_$][\\w$]*\\.)*)([A-Za-z_$][\\w$]*)\\s+([A-Za-z_$][\\w$]*)\\s*\\(");
    private static final Pattern THROW_NEW_EXCEPTION_PATTERN =
            Pattern.compile("\\bthrow\\s+new\\s+(?:(?:[A-Za-z_$][\\w$]*\\.)*)([A-Za-z_$][\\w$]*)\\s*\\(");
    private static final Pattern THROW_FACTORY_PATTERN = Pattern.compile(
            "\\bthrow\\s+(?:(?:[A-Za-z_$][\\w$]*\\.)*)([A-Za-z_$][\\w$]*)\\.([A-Za-z_$][\\w$]*)\\s*\\(");
    private static final Pattern EXCEPTION_VARIABLE_PATTERN = Pattern.compile(
            "\\b(?:(?:[A-Za-z_$][\\w$]*\\.)*)([A-Za-z_$][\\w$]*(?:Exception|Error))\\s+([A-Za-z_$][\\w$]*)\\b");

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
        ExceptionTypeIndex exceptionTypes = ExceptionTypeIndex.from(sourceRoot);
        assertNoSourceMatches(
                sourceRoot,
                path -> hasExceptionBoundaryViolation(path, exceptionTypes),
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

    private static boolean hasExceptionBoundaryViolation(Path path, ExceptionTypeIndex exceptionTypes) {
        try {
            String source = withoutCommentsAndLiterals(Files.readString(path));
            return (isBusinessLayerSource(source, "domain")
                            && (hasExceptionExit(source, "BizException", BIZ_EXCEPTION_EXIT_PATTERN)
                                    || hasExceptionExit(source, "ApiException", API_EXCEPTION_EXIT_PATTERN)))
                    || (isBusinessLayerSource(source, "application")
                            && (hasExceptionExit(source, "DomainException", DOMAIN_EXCEPTION_EXIT_PATTERN)
                                    || hasExceptionExit(source, "ApiException", API_EXCEPTION_EXIT_PATTERN)))
                    || (isBusinessLayerSource(source, "interfaces")
                            && hasExceptionExitNotAssignableTo(source, "ApiException", exceptionTypes));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read source file " + path, exception);
        }
    }

    private static boolean hasExceptionExitNotAssignableTo(
            String source, String allowedExceptionType, ExceptionTypeIndex exceptionTypes) {
        Matcher directMatcher = THROW_NEW_EXCEPTION_PATTERN.matcher(source);
        while (directMatcher.find()) {
            if (!exceptionTypes.isAssignableTo(directMatcher.group(1), allowedExceptionType)) {
                return true;
            }
        }

        Matcher factoryMatcher = THROW_FACTORY_PATTERN.matcher(source);
        while (factoryMatcher.find()) {
            String returnType = exceptionTypes.factoryReturnType(factoryMatcher.group(1), factoryMatcher.group(2));
            if (returnType == null || !exceptionTypes.isAssignableTo(returnType, allowedExceptionType)) {
                return true;
            }
        }

        Map<String, String> variableTypes = new HashMap<String, String>();
        Matcher variableMatcher = EXCEPTION_VARIABLE_PATTERN.matcher(source);
        while (variableMatcher.find()) {
            variableTypes.put(variableMatcher.group(2), variableMatcher.group(1));
        }
        Matcher throwMatcher = THROW_VARIABLE_PATTERN.matcher(source);
        while (throwMatcher.find()) {
            String variableType = variableTypes.get(throwMatcher.group(1));
            if (variableType == null || !exceptionTypes.isAssignableTo(variableType, allowedExceptionType)) {
                return true;
            }
        }
        return false;
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
        StringBuilder sanitized = new StringBuilder(source.length());
        int index = 0;
        while (index < source.length()) {
            char current = source.charAt(index);
            if (current == '/' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                index = appendMaskedLineComment(source, sanitized, index);
            } else if (current == '/' && index + 1 < source.length() && source.charAt(index + 1) == '*') {
                index = appendMaskedBlockComment(source, sanitized, index);
            } else if (current == '\"' && startsTextBlock(source, index)) {
                index = appendMaskedTextBlock(source, sanitized, index);
            } else if (current == '\"') {
                index = appendMaskedQuotedLiteral(source, sanitized, index, '\"');
            } else if (current == '\'') {
                index = appendMaskedQuotedLiteral(source, sanitized, index, '\'');
            } else {
                sanitized.append(current);
                index++;
            }
        }
        return sanitized.toString();
    }

    private static int appendMaskedLineComment(String source, StringBuilder sanitized, int index) {
        while (index < source.length() && source.charAt(index) != '\n') {
            sanitized.append(' ');
            index++;
        }
        return index;
    }

    private static int appendMaskedBlockComment(String source, StringBuilder sanitized, int index) {
        sanitized.append("  ");
        index += 2;
        while (index < source.length()) {
            if (source.charAt(index) == '*' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                sanitized.append("  ");
                return index + 2;
            }
            sanitized.append(source.charAt(index) == '\n' ? '\n' : ' ');
            index++;
        }
        return index;
    }

    private static boolean startsTextBlock(String source, int index) {
        return index + 2 < source.length() && source.charAt(index + 1) == '\"' && source.charAt(index + 2) == '\"';
    }

    private static int appendMaskedTextBlock(String source, StringBuilder sanitized, int index) {
        sanitized.append("   ");
        index += 3;
        while (index < source.length()) {
            if (startsTextBlock(source, index)) {
                sanitized.append("   ");
                return index + 3;
            }
            sanitized.append(source.charAt(index) == '\n' ? '\n' : ' ');
            index++;
        }
        return index;
    }

    private static int appendMaskedQuotedLiteral(String source, StringBuilder sanitized, int index, char quote) {
        sanitized.append(' ');
        index++;
        while (index < source.length()) {
            char current = source.charAt(index);
            sanitized.append(current == '\n' ? '\n' : ' ');
            index++;
            if (current == '\\' && index < source.length()) {
                sanitized.append(source.charAt(index) == '\n' ? '\n' : ' ');
                index++;
            } else if (current == quote) {
                return index;
            }
        }
        return index;
    }

    @FunctionalInterface
    private interface SourceMatcher {

        boolean matches(Path path);
    }

    private record ExceptionTypeIndex(Map<String, String> parentTypes, Map<String, String> factoryReturnTypes) {

        private static ExceptionTypeIndex from(Path sourceRoot) throws IOException {
            Map<String, String> parentTypes = new HashMap<String, String>();
            Map<String, String> factoryReturnTypes = new HashMap<String, String>();
            try (Stream<Path> paths = Files.walk(sourceRoot)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .filter(SourceHardRuleArchitectureRuleSupport::isProductionJavaSource)
                        .forEach(path -> indexSource(path, parentTypes, factoryReturnTypes));
            }
            parentTypes.put("ApiException", "KuzhambuException");
            parentTypes.put("BizException", "RuntimeException");
            parentTypes.put("DomainException", "RuntimeException");
            return new ExceptionTypeIndex(parentTypes, factoryReturnTypes);
        }

        private static void indexSource(
                Path path, Map<String, String> parentTypes, Map<String, String> factoryReturnTypes) {
            try {
                String source = withoutCommentsAndLiterals(Files.readString(path));
                Matcher extendsMatcher = CLASS_EXTENDS_PATTERN.matcher(source);
                while (extendsMatcher.find()) {
                    parentTypes.put(extendsMatcher.group(1), extendsMatcher.group(2));
                }
                Matcher classMatcher = CLASS_PATTERN.matcher(source);
                if (!classMatcher.find()) {
                    return;
                }
                String className = classMatcher.group(1);
                Matcher methodMatcher = STATIC_METHOD_PATTERN.matcher(source);
                while (methodMatcher.find()) {
                    factoryReturnTypes.put(className + "." + methodMatcher.group(2), methodMatcher.group(1));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to index source file " + path, exception);
            }
        }

        private boolean isAssignableTo(String exceptionType, String allowedExceptionType) {
            String currentType = exceptionType;
            Set<String> visited = new HashSet<String>();
            while (currentType != null && visited.add(currentType)) {
                if (allowedExceptionType.equals(currentType)) {
                    return true;
                }
                currentType = parentTypes.get(currentType);
            }
            return false;
        }

        private String factoryReturnType(String className, String methodName) {
            return factoryReturnTypes.get(className + "." + methodName);
        }
    }
}
