package com.thundax.kuzhambu.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class ModelAnnotationArchitectureRuleSupport {

    public static final String NAME_REQUEST_REQUIRED_ANNOTATIONS = "MODEL_REQUEST_REQUIRED_ANNOTATIONS";
    public static final String NAME_RESPONSE_REQUIRED_ANNOTATIONS = "MODEL_RESPONSE_REQUIRED_ANNOTATIONS";
    public static final String NAME_DATA_OBJECT_REQUIRED_ANNOTATIONS = "MODEL_DATA_OBJECT_REQUIRED_ANNOTATIONS";

    private static final Set<String> REQUEST_REQUIRED_ANNOTATIONS = new LinkedHashSet<String>(Arrays.asList(
            "lombok.Getter",
            "lombok.Setter",
            "io.swagger.v3.oas.annotations.media.Schema",
            "com.fasterxml.jackson.annotation.JsonInclude",
            "com.fasterxml.jackson.annotation.JsonIgnoreProperties"));

    private static final Set<String> RESPONSE_REQUIRED_ANNOTATIONS = new LinkedHashSet<String>(Arrays.asList(
            "lombok.Getter",
            "lombok.Builder",
            "io.swagger.v3.oas.annotations.media.Schema",
            "com.fasterxml.jackson.annotation.JsonInclude",
            "com.fasterxml.jackson.annotation.JsonIgnoreProperties"));

    private static final Set<String> DATA_OBJECT_REQUIRED_ANNOTATIONS = new LinkedHashSet<String>(Arrays.asList(
            "lombok.Getter",
            "lombok.Setter",
            "lombok.NoArgsConstructor",
            "lombok.AllArgsConstructor",
            "com.baomidou.mybatisplus.annotation.TableName"));

    private static final String SOURCE_ROOTS_PROPERTY = "kuzhambu.architecture.sourceRoots";

    private ModelAnnotationArchitectureRuleSupport() {}

    public static ArchRule requestClassAnnotationsRequired(String basePackage) {
        return modelClassAnnotationsRequired(
                basePackage, ".request", "Request", REQUEST_REQUIRED_ANNOTATIONS, NAME_REQUEST_REQUIRED_ANNOTATIONS);
    }

    public static ArchRule responseClassAnnotationsRequired(String basePackage) {
        return modelClassAnnotationsRequired(
                basePackage,
                ".response",
                "Response",
                RESPONSE_REQUIRED_ANNOTATIONS,
                NAME_RESPONSE_REQUIRED_ANNOTATIONS);
    }

    public static ArchRule dataObjectClassAnnotationsRequired(String basePackage, String... excludedClassNames) {
        return ArchRuleDefinition.classes()
                .should(new ArchCondition<JavaClass>("declare required data object class annotations only") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (!isDataObjectClassUnder(item, basePackage)
                                || Arrays.asList(excludedClassNames).contains(item.getFullName())) {
                            return;
                        }
                        Set<String> actualAnnotations = annotationTypeNames(item);
                        Set<String> missingAnnotations =
                                missingAnnotations(actualAnnotations, DATA_OBJECT_REQUIRED_ANNOTATIONS);
                        Set<String> extraAnnotations =
                                missingAnnotations(DATA_OBJECT_REQUIRED_ANNOTATIONS, actualAnnotations);

                        if (!missingAnnotations.isEmpty() || !extraAnnotations.isEmpty()) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    "["
                                            + NAME_DATA_OBJECT_REQUIRED_ANNOTATIONS
                                            + "] "
                                            + item.getFullName()
                                            + " violates data object class annotations: missing="
                                            + missingAnnotations
                                            + ", extra="
                                            + extraAnnotations));
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE " + NAME_DATA_OBJECT_REQUIRED_ANNOTATIONS);
    }

    private static ArchRule modelClassAnnotationsRequired(
            final String basePackage,
            final String packageSegment,
            final String simpleNameSuffix,
            final Set<String> requiredAnnotations,
            final String ruleName) {
        return ArchRuleDefinition.classes()
                .should(new ArchCondition<JavaClass>("declare required model class annotations only") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (!isModelClassUnder(item, basePackage, packageSegment, simpleNameSuffix)) {
                            return;
                        }
                        Set<String> actualAnnotations = annotationTypeNames(item);
                        Set<String> missingAnnotations = missingAnnotations(actualAnnotations, requiredAnnotations);
                        Set<String> extraAnnotations = missingAnnotations(requiredAnnotations, actualAnnotations);

                        if (!missingAnnotations.isEmpty() || !extraAnnotations.isEmpty()) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    "["
                                            + ruleName
                                            + "] "
                                            + item.getFullName()
                                            + " violates model class annotations: missing="
                                            + missingAnnotations
                                            + ", extra="
                                            + extraAnnotations));
                        }
                    }
                })
                .allowEmptyShould(true)
                .because("RULE " + ruleName);
    }

    private static boolean isModelClassUnder(
            JavaClass item, String basePackage, String packageSegment, String simpleNameSuffix) {
        return item.getPackageName().startsWith(basePackage + ".")
                && item.getPackageName().contains(packageSegment)
                && item.getSimpleName().endsWith(simpleNameSuffix);
    }

    private static boolean isDataObjectClassUnder(JavaClass item, String basePackage) {
        return item.getPackageName().startsWith(basePackage + ".")
                && item.getPackageName().contains(".persistence.dataobject")
                && (item.getSimpleName().endsWith("DO") || item.getSimpleName().endsWith("DataObject"));
    }

    private static Set<String> annotationTypeNames(JavaClass item) {
        Set<String> annotationTypeNames = new LinkedHashSet<String>();
        for (JavaAnnotation<JavaClass> annotation : item.getAnnotations()) {
            annotationTypeNames.add(annotation.getRawType().getFullName());
        }
        annotationTypeNames.addAll(sourceAnnotationTypeNames(item));
        return annotationTypeNames;
    }

    private static Set<String> missingAnnotations(Set<String> actualAnnotations, Set<String> expectedAnnotations) {
        Set<String> missingAnnotations = new LinkedHashSet<>(expectedAnnotations);
        missingAnnotations.removeAll(actualAnnotations);
        return missingAnnotations;
    }

    private static Set<String> sourceAnnotationTypeNames(JavaClass item) {
        Path sourcePath = sourcePath(item);
        if (sourcePath == null) {
            return new LinkedHashSet<>();
        }
        try {
            List<String> lines = Files.readAllLines(sourcePath, StandardCharsets.UTF_8);
            return sourceAnnotationTypeNames(item, lines);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read source file: " + sourcePath, ex);
        }
    }

    private static Path sourcePath(JavaClass item) {
        String sourcePath = item.getFullName().replace('.', '/') + ".java";
        for (String sourceRoot : sourceRoots()) {
            Path path = Paths.get(sourceRoot, sourcePath);
            if (Files.exists(path)) {
                return path;
            }
        }
        Path workspaceSourcePath = workspaceSourcePath(sourcePath);
        if (workspaceSourcePath != null) {
            return workspaceSourcePath;
        }
        return null;
    }

    private static Path workspaceSourcePath(String sourcePath) {
        try {
            Path currentDirectory = Paths.get(".");
            List<String> sourceRootSuffixes =
                    Arrays.asList("src/main/java/" + sourcePath, "src/test/java/" + sourcePath);
            try (Stream<Path> paths = Files.walk(currentDirectory)) {
                return paths.filter(Files::isRegularFile)
                        .filter(path -> matchesAnySuffix(normalizePath(path), sourceRootSuffixes))
                        .findFirst()
                        .orElse(null);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to search source file: " + sourcePath, ex);
        }
    }

    private static boolean matchesAnySuffix(String path, List<String> suffixes) {
        for (String suffix : suffixes) {
            if (path.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizePath(Path path) {
        return path.normalize().toString().replace('\\', '/');
    }

    private static List<String> sourceRoots() {
        String configuredSourceRoots = System.getProperty(SOURCE_ROOTS_PROPERTY, "src/main/java,src/test/java");
        return Arrays.asList(configuredSourceRoots.split(","));
    }

    private static Set<String> sourceAnnotationTypeNames(JavaClass item, List<String> lines) {
        Map<String, String> imports = imports(lines);
        Set<String> annotationTypeNames = new LinkedHashSet<String>();
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.contains(" class " + item.getSimpleName())
                    || trimmedLine.contains(" interface " + item.getSimpleName())
                    || trimmedLine.contains(" enum " + item.getSimpleName())) {
                break;
            }
            if (trimmedLine.startsWith("@")) {
                annotationTypeNames.add(resolveAnnotationName(item, imports, annotationSimpleName(trimmedLine)));
            }
        }
        return annotationTypeNames;
    }

    private static Map<String, String> imports(List<String> lines) {
        Map<String, String> imports = new HashMap<String, String>();
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("import ") || trimmedLine.startsWith("import static ")) {
                continue;
            }
            String importedClass = trimmedLine.substring("import ".length(), trimmedLine.length() - 1);
            imports.put(importedClass.substring(importedClass.lastIndexOf('.') + 1), importedClass);
        }
        return imports;
    }

    private static String annotationSimpleName(String line) {
        int endIndex = 1;
        while (endIndex < line.length()) {
            char ch = line.charAt(endIndex);
            if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
                break;
            }
            endIndex++;
        }
        return line.substring(1, endIndex);
    }

    private static String resolveAnnotationName(JavaClass item, Map<String, String> imports, String annotationName) {
        if (annotationName.contains(".")) {
            return annotationName;
        }
        if (imports.containsKey(annotationName)) {
            return imports.get(annotationName);
        }
        return item.getPackageName() + "." + annotationName;
    }
}
