package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PathArchitectureRuleSupport {

    private static final String CONFIGURATION_ANNOTATION = "org.springframework.context.annotation.Configuration";
    private static final String CONFIGURATION_PROPERTIES_ANNOTATION =
            "org.springframework.boot.context.properties.ConfigurationProperties";
    private static final String REQUEST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
    private static final String REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";

    private PathArchitectureRuleSupport() {}

    public static ArchRule controllerRequestMappingShouldStartWith(String basePackage, String requiredPrefix) {
        final String normalizedPrefix = normalizePrefix(requiredPrefix);
        return ArchRuleDefinition.classes()
                .should(new ArchCondition<JavaClass>("declare request mapping with required prefix") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (!isPackageUnder(item, basePackage) || !item.isAnnotatedWith(REST_CONTROLLER_ANNOTATION)) {
                            return;
                        }
                        Optional<String> mapping = requestMappingValue(item);
                        if (!mapping.isPresent() || !mapping.get().startsWith(normalizedPrefix)) {
                            events.add(SimpleConditionEvent.violated(
                                    item,
                                    item.getFullName()
                                            + " request mapping must start with "
                                            + normalizedPrefix
                                            + ", actual="
                                            + mapping.orElse("<missing>")));
                        }
                    }
                })
                .allowEmptyShould(true);
    }

    public static void assertConfigurationClassPlacement(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();

        for (JavaClass javaClass : classes) {
            if (isNestedClass(javaClass) || !isConfigurationAnnotated(javaClass)) {
                continue;
            }
            if (!isConfigurePackage(javaClass)) {
                violations.add(javaClass.getName());
            }
        }

        assertTrue(
                "@Configuration and @ConfigurationProperties classes must be placed under a ..configure.. package: "
                        + violations,
                violations.isEmpty());
    }

    private static Optional<String> requestMappingValue(JavaClass item) {
        for (JavaAnnotation<JavaClass> annotation : item.getAnnotations()) {
            if (!REQUEST_MAPPING_ANNOTATION.equals(annotation.getRawType().getFullName())) {
                continue;
            }
            Object value = annotation.get("value").orElse(annotation.get("path").orElse(null));
            if (value instanceof String[]) {
                String[] values = (String[]) value;
                return values.length == 0 ? Optional.<String>empty() : Optional.of(values[0]);
            }
            if (value instanceof String) {
                return Optional.of((String) value);
            }
        }
        return Optional.empty();
    }

    private static String normalizePrefix(String requiredPrefix) {
        if (requiredPrefix.startsWith("/")) {
            return requiredPrefix;
        }
        return "/" + requiredPrefix;
    }

    private static boolean isPackageUnder(JavaClass item, String basePackage) {
        return item.getPackageName().equals(basePackage)
                || item.getPackageName().startsWith(basePackage + ".");
    }

    private static boolean isConfigurationAnnotated(JavaClass javaClass) {
        return javaClass.isAnnotatedWith(CONFIGURATION_ANNOTATION)
                || javaClass.isAnnotatedWith(CONFIGURATION_PROPERTIES_ANNOTATION);
    }

    private static boolean isConfigurePackage(JavaClass javaClass) {
        return javaClass.getPackageName().equals("configure")
                || javaClass.getPackageName().endsWith(".configure")
                || javaClass.getPackageName().contains(".configure.");
    }

    private static boolean isNestedClass(JavaClass javaClass) {
        return javaClass.getName().contains("$");
    }
}
