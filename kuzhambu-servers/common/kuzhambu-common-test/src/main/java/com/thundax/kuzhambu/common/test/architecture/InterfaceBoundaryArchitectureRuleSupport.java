package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class InterfaceBoundaryArchitectureRuleSupport {

    private static final Pattern PROTOCOL_IMPORT_PATTERN = Pattern.compile(
            "import\\s+com\\.thundax\\.kuzhambu\\.[a-z]+\\.interfaces\\.(admin|portal)\\.([a-z]+)\\.(controller\\.(request|response)|assembler)\\.");
    private static final Pattern PUBLIC_METHOD_SIGNATURE_PATTERN =
            Pattern.compile("public\\s+[^;{]+\\s+[A-Za-z0-9_]+\\s*\\([^;{]*\\)", Pattern.DOTALL);
    private static final Pattern FIELD_DECLARATION_PATTERN =
            Pattern.compile("(?:private|public|protected)\\s+(?!static\\b)([^;=]+?)\\s+[A-Za-z0-9_]+\\s*(?:[;=])");

    private InterfaceBoundaryArchitectureRuleSupport() {}

    public static void assertInterfaceNoPersistenceDependency(JavaClasses classes, String basePackage) {
        noClasses()
                .that()
                .resideInAPackage(basePackage + ".interfaces..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        basePackage + ".domain.repository..",
                        basePackage + ".infra.mapper..",
                        basePackage + ".infra.dataobject..",
                        basePackage + ".infra.repository.impl..")
                .check(classes);
    }

    public static void assertInterfaceOnlyCallsApplicationServices(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getPackageName().startsWith(basePackage + ".interfaces.")) {
                continue;
            }
            for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                JavaClass targetClass = dependency.getTargetClass();
                if (!targetClass.getPackageName().startsWith(basePackage + ".application.")
                        || !targetClass.getPackageName().contains(".service")) {
                    continue;
                }
                if (targetClass.getSimpleName().endsWith("Service")
                        && !targetClass.getSimpleName().endsWith("ApplicationService")) {
                    violations.add(javaClass.getName() + " -> " + targetClass.getName());
                }
                if (targetClass.getSimpleName().endsWith("ServiceImpl")
                        && !targetClass.getSimpleName().endsWith("ApplicationServiceImpl")) {
                    violations.add(javaClass.getName() + " -> " + targetClass.getName());
                }
            }
        }

        assertTrue(
                "Interface layer must call *ApplicationService instead of generic *Service: " + violations,
                violations.isEmpty());
    }

    public static void assertInterfaceProtocolModelsStayInSameSubdomain(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith("Controller.java"))
                    .filter(InterfaceBoundaryArchitectureRuleSupport::isEntryController)
                    .forEach(path -> collectProtocolModelViolations(root, path, violations));
        }

        assertTrue(
                "Interface controllers must use request/response/assembler models from the same entry subdomain: "
                        + violations,
                violations.isEmpty());
    }

    public static void assertInterfaceProtocolsDoNotExposeDomainModels(Path sourceRoot) throws IOException {
        Path root = ArchitectureSourceSupport.repositoryRoot();
        List<String> violations = new ArrayList<String>();

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(InterfaceBoundaryArchitectureRuleSupport::isControllerOrResponseSource)
                    .forEach(path -> collectDomainModelExposureViolations(root, path, violations));
        }

        assertTrue(
                "Controller method signatures and response fields must not expose domain model types: " + violations,
                violations.isEmpty());
    }

    private static void collectProtocolModelViolations(Path root, Path path, List<String> violations) {
        String subdomain = entrySubdomain(path);
        String content = ArchitectureSourceSupport.readSource(path);
        Matcher matcher = PROTOCOL_IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            String importedSubdomain = matcher.group(2);
            if (!subdomain.equals(importedSubdomain)) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " importSubdomain="
                        + importedSubdomain + " expected=" + subdomain);
            }
        }
    }

    private static void collectDomainModelExposureViolations(Path root, Path path, List<String> violations) {
        String content = ArchitectureSourceSupport.readSource(path);
        if (isEntryController(path)) {
            Matcher matcher = PUBLIC_METHOD_SIGNATURE_PATTERN.matcher(content);
            while (matcher.find()) {
                if (containsDomainModelType(matcher.group())) {
                    violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " signature="
                            + compact(matcher.group()));
                }
            }
            return;
        }
        Matcher fieldMatcher = FIELD_DECLARATION_PATTERN.matcher(content);
        while (fieldMatcher.find()) {
            if (containsDomainModelType(fieldMatcher.group(1))) {
                violations.add(ArchitectureSourceSupport.repositoryPath(root, path) + " field="
                        + compact(fieldMatcher.group()));
            }
        }
    }

    private static boolean containsDomainModelType(String text) {
        return text.contains(".domain.") && text.contains(".model.");
    }

    private static boolean isControllerOrResponseSource(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return isEntryController(path) || normalized.contains("/controller/response/");
    }

    private static boolean isEntryController(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return normalized.endsWith("Controller.java")
                && (normalized.contains("/interfaces/admin/") || normalized.contains("/interfaces/portal/"))
                && path.getParent() != null
                && "controller".equals(path.getParent().getFileName().toString());
    }

    private static String entrySubdomain(Path path) {
        String normalized = path.toString().replace('\\', '/');
        Matcher matcher = Pattern.compile("/interfaces/(?:admin|portal)/([^/]+)/controller/")
                .matcher(normalized);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String compact(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
