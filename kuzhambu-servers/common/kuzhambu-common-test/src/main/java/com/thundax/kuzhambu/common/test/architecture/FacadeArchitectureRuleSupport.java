package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class FacadeArchitectureRuleSupport {

    private static final Pattern FACADE_SETTER_PATTERN =
            Pattern.compile("\\bpublic\\s+void\\s+set[A-Z][A-Za-z0-9_]*\\s*\\(");
    private static final String PRIVATE_ALL_ARGS_CONSTRUCTOR = "@AllArgsConstructor(access = AccessLevel.PRIVATE)";

    private FacadeArchitectureRuleSupport() {}

    public static void assertFacadePlacement(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();
        String facadePackage = basePackage + ".facade";
        String requestPackage = facadePackage + ".request";
        String responsePackage = facadePackage + ".response";
        String dtoPackage = facadePackage + ".dto";

        for (JavaClass javaClass : classes) {
            if (javaClass.getName().contains("$")) {
                continue;
            }
            String simpleName = javaClass.getSimpleName();
            String packageName = javaClass.getPackageName();

            if (simpleName.endsWith("FacadeRequest")) {
                if (!requestPackage.equals(packageName)) {
                    violations.add(javaClass.getName());
                }
                continue;
            }
            if (simpleName.endsWith("FacadeResponse")) {
                if (!responsePackage.equals(packageName)) {
                    violations.add(javaClass.getName());
                }
                continue;
            }
            if (simpleName.endsWith("FacadeDto")) {
                if (!dtoPackage.equals(packageName)) {
                    violations.add(javaClass.getName());
                }
                continue;
            }
            if (simpleName.endsWith("Facade")) {
                if (!facadePackage.equals(packageName)) {
                    violations.add(javaClass.getName());
                }
                continue;
            }

            if (packageName.equals(requestPackage)
                    || packageName.equals(responsePackage)
                    || packageName.equals(dtoPackage)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "Facade protocol classes must stay in facade/request/response/dto packages with matching suffixes: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertFacadeProtocolModelsImmutable(String sourceRootRelativePath) throws IOException {
        Path sourceRoot = ArchitectureSourceSupport.repositoryRoot()
                .resolve(sourceRootRelativePath)
                .normalize();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(FacadeArchitectureRuleSupport::isFacadeProtocolSource)
                    .forEach(path -> collectImmutableProtocolViolations(path, violations));
        }

        assertTrue(
                "Facade protocol models must use Getter + Builder + private all-args constructor and no setter: "
                        + violations,
                violations.isEmpty());
    }

    private static void collectImmutableProtocolViolations(Path path, List<String> violations) {
        Set<String> annotations =
                new LinkedHashSet<String>(NamingArchitectureRuleSupport.sourceClassAnnotationSimpleNames(path));
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        String relativePath =
                ArchitectureSourceSupport.repositoryPath(ArchitectureSourceSupport.repositoryRoot(), path);

        if (ArchitectureSourceSupport.isEnumSource(source)) {
            return;
        }

        if (!annotations.contains("Getter")
                || !annotations.contains("Builder")
                || !annotations.contains("AllArgsConstructor")) {
            violations.add(relativePath + " missing required annotations");
        }
        if (annotations.contains("Setter")) {
            violations.add(relativePath + " must not use @Setter");
        }
        if (!source.contains(PRIVATE_ALL_ARGS_CONSTRUCTOR)) {
            violations.add(relativePath + " must use private all-args constructor");
        }
        if (FACADE_SETTER_PATTERN.matcher(source).find()) {
            violations.add(relativePath + " must not declare public setter");
        }
    }

    private static boolean isFacadeProtocolSource(Path path) {
        String normalized = ArchitectureSourceSupport.normalizePath(path);
        return normalized.endsWith(".java")
                && (normalized.contains("/facade/request/")
                        || normalized.contains("/facade/response/")
                        || normalized.contains("/facade/dto/"));
    }
}
