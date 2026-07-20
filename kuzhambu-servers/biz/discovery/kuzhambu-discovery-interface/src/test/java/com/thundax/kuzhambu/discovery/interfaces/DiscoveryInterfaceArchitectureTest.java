package com.thundax.kuzhambu.discovery.interfaces;

import com.thundax.kuzhambu.common.test.architecture.ApiSurfaceArchitectureRuleSupport;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DiscoveryInterfaceArchitectureTest {

    @Test
    void interfaceApiSurfaceShouldKeepContractShape() throws Exception {
        ApiSurfaceArchitectureRuleSupport.assertApiModelsDoNotExposePriority(Path.of("src/main/java"));
        ApiSurfaceArchitectureRuleSupport.assertSortRequestsUseOrderedIdsOnly(Path.of("src/main/java"));
    }
}
