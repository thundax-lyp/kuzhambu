package com.thundax.kuzhambu.discovery.interfaces;

import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import com.thundax.kuzhambu.common.test.architecture.ConcurrencyArchitectureRuleSupport;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DiscoveryInterfaceArchitectureTest {

    private static final String BASE_PACKAGE = "com.thundax.kuzhambu.discovery";

    @Test
    void interfaceApiSurfaceShouldKeepContractShape() throws Exception {
        JavaClasses classes = ConcurrencyArchitectureRuleSupport.importMainClasses(BASE_PACKAGE + ".interfaces");
        ConcurrencyArchitectureRuleSupport.shouldNotUseCompletableFutureAsyncWithoutExecutor(BASE_PACKAGE)
                .check(classes);
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
    }
}
