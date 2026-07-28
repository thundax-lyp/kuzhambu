package com.thundax.kuzhambu.knowledge.domain;

import com.thundax.kuzhambu.common.test.architecture.NamingArchitectureRuleSupport;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class KnowledgeDomainArchitectureTest {

    @Test
    void valueObjectIdsShouldDeclareNoStaticMethods() {
        NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(Path.of("src/main/java"));
    }
}
