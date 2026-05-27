package com.thundax.kuzhambu.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;

public final class DeployedModuleArchitectureRuleSupport {

    private DeployedModuleArchitectureRuleSupport() {}

    public static void assertApplicationLayerBoundary(JavaClasses classes, String basePackage) {
        ModuleAndDependencyArchitectureRuleSupport.assertApplicationLayerBoundary(classes, basePackage);
        AnnotationBoundaryArchitectureRuleSupport.assertApplicationNoHttpAnnotations(classes, basePackage);
    }

    public static void assertInterfaceLayerBoundary(JavaClasses classes, String basePackage) {
        ModuleAndDependencyArchitectureRuleSupport.assertInterfaceLayerBoundary(classes, basePackage);
        InterfaceBoundaryArchitectureRuleSupport.assertInterfaceNoPersistenceDependency(classes, basePackage);
    }

    public static void assertInfraLayerBoundary(JavaClasses classes, String basePackage) {
        ModuleAndDependencyArchitectureRuleSupport.assertInfraLayerBoundary(classes, basePackage);
        AnnotationBoundaryArchitectureRuleSupport.assertInfraAnnotationBoundary(classes, basePackage);
    }
}
