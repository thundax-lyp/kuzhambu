package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class StarterArchitectureRuleSupport {

    private static final List<String> BUSINESS_PACKAGE_SEGMENTS = Arrays.asList(
            "controller", "service", "repository", "mapper", "domain", "application", "infra", "interfaces");
    private static final String REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";
    private static final String SPRING_BOOT_APPLICATION_ANNOTATION =
            "org.springframework.boot.autoconfigure.SpringBootApplication";

    private StarterArchitectureRuleSupport() {}

    public static void assertStarterContainsOnlyRuntimeAssembly(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (!javaClass.getPackageName().startsWith(basePackage)) {
                violations.add(javaClass.getName() + " package");
                continue;
            }
            if (javaClass.isAnnotatedWith(REST_CONTROLLER_ANNOTATION)
                    || javaClass.getSimpleName().endsWith("Controller")) {
                violations.add(javaClass.getName() + " controller");
            }
            if (containsBusinessPackageSegment(javaClass.getPackageName())) {
                violations.add(javaClass.getName() + " business-package");
            }
            if (javaClass.isAnnotatedWith(SPRING_BOOT_APPLICATION_ANNOTATION)) {
                assertApplicationClassOnlyStartsApplication(javaClass, violations);
            }
        }

        assertTrue("Starter modules must contain runtime assembly only: " + violations, violations.isEmpty());
    }

    private static void assertApplicationClassOnlyStartsApplication(JavaClass javaClass, List<String> violations) {
        if (!javaClass.getSimpleName().endsWith("Application")) {
            violations.add(javaClass.getName() + " application-name");
        }
        for (JavaMethod method : javaClass.getMethods()) {
            if (!method.getOwner().equals(javaClass)) {
                continue;
            }
            if (!"main".equals(method.getName())) {
                violations.add(method.getFullName());
            }
        }
    }

    private static boolean containsBusinessPackageSegment(String packageName) {
        List<String> segments = Arrays.asList(packageName.split("\\."));
        for (String segment : BUSINESS_PACKAGE_SEGMENTS) {
            if (segments.contains(segment)) {
                return true;
            }
        }
        return false;
    }
}
