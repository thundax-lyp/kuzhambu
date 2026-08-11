package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class TransactionArchitectureRuleSupport {

    private static final String TRANSACTIONAL_ANNOTATION = "org.springframework.transaction.annotation.Transactional";
    private static final Pattern TRANSACTIONAL_PATTERN = Pattern.compile(
            "(?m)^\\s*@(?:org\\.springframework\\.transaction\\.annotation\\.)?Transactional(?:\\s*\\([^)]*\\))?\\s*$");
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern TYPE_PATTERN = Pattern.compile("\\b(?:class|interface|record|enum)\\s+(\\w+)\\b");

    private TransactionArchitectureRuleSupport() {}

    public static void assertTransactionalOnlyOnApplicationServiceUseCases(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (javaClass.isAnnotatedWith(TRANSACTIONAL_ANNOTATION)
                    && !isApplicationServiceClass(javaClass, basePackage)) {
                violations.add(javaClass.getName());
            }
            for (JavaMethod method : javaClass.getMethods()) {
                if (!method.isAnnotatedWith(TRANSACTIONAL_ANNOTATION)) {
                    continue;
                }
                if (!isApplicationServiceClass(javaClass, basePackage)
                        || !method.getModifiers().contains(JavaModifier.PUBLIC)) {
                    violations.add(method.getFullName());
                }
            }
        }

        assertTrue(
                "@Transactional must stay on application service/facade classes or public use-case methods: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertServerTransactionalOnlyOnApplicationServiceOrFacadeUseCases(Path sourceRoot)
            throws IOException {
        Path repositoryRoot = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(TransactionArchitectureRuleSupport::isProductionJavaSource)
                    .forEach(path -> collectSourceViolations(repositoryRoot, path, violations));
        }

        assertTrue(
                "@Transactional must stay on application service/facade classes or public use-case methods: "
                        + violations,
                violations.isEmpty());
    }

    private static boolean isApplicationServiceClass(JavaClass javaClass, String basePackage) {
        String packageName = javaClass.getPackageName();
        String simpleName = javaClass.getSimpleName();
        if (!packageName.startsWith(basePackage + ".application.")) {
            return false;
        }
        boolean applicationService = packageName.endsWith(".impl") && simpleName.endsWith("ApplicationServiceImpl");
        boolean facade = packageName.contains(".facade.impl") && simpleName.endsWith("FacadeImpl");
        return applicationService || facade;
    }

    private static void collectSourceViolations(Path repositoryRoot, Path path, List<String> violations) {
        String source = ArchitectureSourceSupport.readSourceWithoutComments(path);
        Matcher annotationMatcher = TRANSACTIONAL_PATTERN.matcher(source);
        if (!annotationMatcher.find()) {
            return;
        }

        String packageName = packageName(source);
        String simpleName = typeName(source);
        boolean allowedType = isApplicationServiceOrFacade(packageName, simpleName);
        do {
            if (!allowedType) {
                violations.add(ArchitectureSourceSupport.repositoryPath(repositoryRoot, path));
                return;
            }
        } while (annotationMatcher.find());
    }

    private static String packageName(String source) {
        Matcher matcher = PACKAGE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String typeName(String source) {
        Matcher matcher = TYPE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static boolean isApplicationServiceOrFacade(String packageName, String simpleName) {
        if (!packageName.matches(
                "com\\.thundax\\.kuzhambu\\.(?:ai|classics|discovery|knowledge|operations|storage|system)\\.application(?:\\..*)?")) {
            return false;
        }
        return simpleName.endsWith("ApplicationServiceImpl") || simpleName.endsWith("FacadeImpl");
    }

    private static boolean isProductionJavaSource(Path path) {
        return ArchitectureSourceSupport.normalizePath(path.toAbsolutePath()).contains("/src/main/java/");
    }
}
