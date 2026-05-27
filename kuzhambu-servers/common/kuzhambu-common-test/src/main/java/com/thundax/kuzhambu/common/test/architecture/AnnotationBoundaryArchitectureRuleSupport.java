package com.thundax.kuzhambu.common.test.architecture;

import static com.thundax.kuzhambu.common.test.architecture.ArchitectureAssertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AnnotationBoundaryArchitectureRuleSupport {

    private static final List<String> HTTP_ANNOTATIONS = Arrays.asList(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping",
            "org.springframework.web.bind.annotation.RestController");
    private static final List<String> OPENAPI_ANNOTATIONS =
            Arrays.asList("io.swagger.v3.oas.annotations.Operation", "io.swagger.v3.oas.annotations.tags.Tag");
    private static final List<String> TABLE_ANNOTATIONS = Arrays.asList(
            "com.baomidou.mybatisplus.annotation.TableName",
            "com.baomidou.mybatisplus.annotation.TableId",
            "com.baomidou.mybatisplus.annotation.TableField");

    private AnnotationBoundaryArchitectureRuleSupport() {}

    public static void assertApplicationNoHttpAnnotations(JavaClasses classes, String basePackage) {
        assertNoAnnotations(classes, basePackage + ".application", merge(HTTP_ANNOTATIONS, OPENAPI_ANNOTATIONS));
    }

    public static void assertInfraAnnotationBoundary(JavaClasses classes, String basePackage) {
        assertNoAnnotations(classes, basePackage + ".infra", merge(HTTP_ANNOTATIONS, OPENAPI_ANNOTATIONS));
        assertMapperAnnotationsInMapperPackage(classes, basePackage);
        assertTableAnnotationsInDataObjectPackage(classes, basePackage);
    }

    public static void assertMapperAnnotationsInMapperPackage(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();
        for (JavaClass javaClass : classes) {
            if (!javaClass.isAnnotatedWith("org.apache.ibatis.annotations.Mapper")) {
                continue;
            }
            if (!javaClass.getPackageName().startsWith(basePackage + ".infra")
                    || !javaClass.getPackageName().contains(".persistence.mapper")) {
                violations.add(javaClass.getName());
            }
        }
        assertTrue("@Mapper must stay in infra persistence mapper packages: " + violations, violations.isEmpty());
    }

    public static void assertTableAnnotationsInDataObjectPackage(JavaClasses classes, String basePackage) {
        List<String> violations = new ArrayList<String>();
        for (JavaClass javaClass : classes) {
            if (!hasAnyClassAnnotation(javaClass, TABLE_ANNOTATIONS)
                    && !hasAnyFieldAnnotation(javaClass, TABLE_ANNOTATIONS)) {
                continue;
            }
            if (!javaClass.getPackageName().startsWith(basePackage + ".infra")
                    || !javaClass.getPackageName().contains(".persistence.dataobject")) {
                violations.add(javaClass.getName());
            }
        }
        assertTrue(
                "MyBatis table annotations must stay in infra persistence dataobject packages: " + violations,
                violations.isEmpty());
    }

    private static void assertNoAnnotations(JavaClasses classes, String packagePrefix, List<String> annotations) {
        List<String> violations = new ArrayList<String>();
        for (JavaClass javaClass : classes) {
            if (!javaClass.getPackageName().startsWith(packagePrefix)) {
                continue;
            }
            if (hasAnyClassAnnotation(javaClass, annotations) || hasAnyFieldAnnotation(javaClass, annotations)) {
                violations.add(javaClass.getName());
            }
        }
        assertTrue("Layer contains forbidden annotations: " + violations, violations.isEmpty());
    }

    private static boolean hasAnyClassAnnotation(JavaClass javaClass, List<String> annotations) {
        for (JavaAnnotation<JavaClass> annotation : javaClass.getAnnotations()) {
            if (annotations.contains(annotation.getRawType().getFullName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyFieldAnnotation(JavaClass javaClass, List<String> annotations) {
        for (JavaField field : javaClass.getFields()) {
            for (JavaAnnotation<JavaField> annotation : field.getAnnotations()) {
                if (annotations.contains(annotation.getRawType().getFullName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> merge(List<String> first, List<String> second) {
        List<String> merged = new ArrayList<String>(first);
        merged.addAll(second);
        return merged;
    }
}
