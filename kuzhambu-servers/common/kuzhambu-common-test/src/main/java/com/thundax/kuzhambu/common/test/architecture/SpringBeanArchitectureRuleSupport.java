package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SpringBeanArchitectureRuleSupport {

    private static final List<String> DIRECT_SPRING_BEAN_ANNOTATIONS = Arrays.asList(
            "org.springframework.stereotype.Component",
            "org.springframework.stereotype.Service",
            "org.springframework.stereotype.Repository",
            "org.springframework.stereotype.Controller",
            "org.springframework.web.bind.annotation.RestController",
            "org.springframework.context.annotation.Configuration");

    private SpringBeanArchitectureRuleSupport() {}

    public static void assertDirectSpringBeansHaveSingleConstructor(JavaClasses classes) {
        List<String> violations = new ArrayList<String>();
        for (JavaClass javaClass : classes) {
            if (javaClass.isAnnotation() || javaClass.isInterface() || javaClass.isEnum()) {
                continue;
            }
            if (!hasAnyDirectAnnotation(javaClass, DIRECT_SPRING_BEAN_ANNOTATIONS)) {
                continue;
            }
            int constructorCount = javaClass.getConstructors().size();
            if (constructorCount != 1) {
                violations.add(javaClass.getName() + " (" + constructorCount + " constructors)");
            }
        }
        assertTrue(
                "Directly annotated Spring beans must declare exactly one constructor: " + violations,
                violations.isEmpty());
    }

    private static boolean hasAnyDirectAnnotation(JavaClass javaClass, List<String> annotations) {
        for (JavaAnnotation<JavaClass> annotation : javaClass.getAnnotations()) {
            if (annotations.contains(annotation.getRawType().getFullName())) {
                return true;
            }
        }
        return false;
    }
}
