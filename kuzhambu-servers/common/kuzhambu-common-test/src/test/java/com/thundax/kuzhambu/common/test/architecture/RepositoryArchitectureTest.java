package com.thundax.kuzhambu.common.test.architecture;

import org.junit.jupiter.api.Test;

class RepositoryArchitectureTest {

    @Test
    void repositoryShouldKeepServerModuleLayout() throws Exception {
        RepositoryArchitectureRuleSupport.assertServerModuleLayout();
        RepositoryArchitectureRuleSupport.assertMavenModuleNames();
        RepositoryArchitectureRuleSupport.assertJavaPackagesMatchModuleLayout();
        RepositoryArchitectureRuleSupport.assertMavenDependenciesStayInsideAllowedBoundaries();
    }
}
