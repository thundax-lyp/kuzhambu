package com.thundax.kuzhambu.classics.facade;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.FacadeArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ClassicsFacadeArchitectureTest extends AbstractArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.classics";
    private static final String SOURCE_ROOT = "kuzhambu-servers/biz/classics/kuzhambu-classics-facade/src/main/java";

    @Test
    void facadeProtocolShouldKeepPlacementAndImmutableStyle() throws IOException {
        JavaClasses classes = importPackages(BASE_PACKAGE + ".facade");

        FacadeArchitectureRuleSupport.assertFacadePlacement(classes, BASE_PACKAGE);
        FacadeArchitectureRuleSupport.assertFacadeProtocolModelsImmutable(SOURCE_ROOT);
    }
}
