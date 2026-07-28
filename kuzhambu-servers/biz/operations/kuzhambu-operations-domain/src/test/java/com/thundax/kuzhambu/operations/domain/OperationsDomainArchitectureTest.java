package com.thundax.kuzhambu.operations.domain;

import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OperationsDomainArchitectureTest {

    @Test
    void valueObjectIdsShouldDeclareNoStaticMethods() {
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
    }
}
