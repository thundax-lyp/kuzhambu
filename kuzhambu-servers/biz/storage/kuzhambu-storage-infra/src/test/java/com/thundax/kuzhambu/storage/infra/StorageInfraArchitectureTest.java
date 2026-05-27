package com.thundax.kuzhambu.storage.infra;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.AnnotationBoundaryArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ModuleAndDependencyArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Test;

class StorageInfraArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.storage";

    @Test
    void infraLayerShouldKeepArchitectureBoundary() {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".infra");

        ModuleAndDependencyArchitectureRuleSupport.assertInfraLayerBoundary(classes, BASE_PACKAGE);
        AnnotationBoundaryArchitectureRuleSupport.assertInfraAnnotationBoundary(classes, BASE_PACKAGE);
    }
}
