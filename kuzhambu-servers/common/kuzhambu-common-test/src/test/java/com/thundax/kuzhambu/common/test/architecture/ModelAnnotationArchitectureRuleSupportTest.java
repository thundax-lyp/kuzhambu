package com.thundax.kuzhambu.common.test.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModelAnnotationArchitectureRuleSupportTest {

    @Test
    void sourceAnnotationTypeNamesShouldKeepAnnotationsBeforeMultilineAnnotation() {
        JavaClass item = new ClassFileImporter()
                .importClasses(ModelAnnotationArchitectureRuleSupportTest.class)
                .iterator()
                .next();

        Set<String> annotations = ModelAnnotationArchitectureRuleSupport.sourceAnnotationTypeNames(
                item,
                List.of(
                        "@lombok.Getter",
                        "@lombok.Setter",
                        "@io.swagger.v3.oas.annotations.media.Schema(",
                        "    name = \"Test (request)\",",
                        "    description = \"A multiline schema annotation\")",
                        "class ModelAnnotationArchitectureRuleSupportTest {}"));

        assertEquals(
                Set.of("lombok.Getter", "lombok.Setter", "io.swagger.v3.oas.annotations.media.Schema"), annotations);
    }
}
