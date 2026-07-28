package com.thundax.kuzhambu.discovery.application;

import com.thundax.kuzhambu.common.test.architecture.AbstractArchitectureTest;
import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DiscoveryApplicationArchitectureTest extends AbstractArchitectureTest {

    @Test
    void applicationCommandAndQuerySourcesShouldDeclareFieldsOnly() {
        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(Path.of("src/main/java"));
    }
}
