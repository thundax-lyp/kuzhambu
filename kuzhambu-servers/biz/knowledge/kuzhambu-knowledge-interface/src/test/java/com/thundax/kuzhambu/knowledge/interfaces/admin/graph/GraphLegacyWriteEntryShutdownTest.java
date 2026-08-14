package com.thundax.kuzhambu.knowledge.interfaces.admin.graph;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GraphLegacyWriteEntryShutdownTest {

    @Test
    void knowledgeInterfaceShouldNotExposeLegacyGraphRoutes() throws IOException {
        String source = Files.readString(interfaceSourceRoot().resolve("admin/graph/GraphController.java"))
                + Files.readString(interfaceSourceRoot().resolve("portal/graph/GraphPortalController.java"));

        assertThat(source)
                .doesNotContain("/knowledge/graph-extraction")
                .doesNotContain("/knowledge/graph-result")
                .doesNotContain("/knowledge/refinement")
                .doesNotContain("/portal/knowledge/atlas/get");
        assertThat(source).contains("@RequestMapping(\"/api/knowledge/graph\")");
        assertThat(source).contains("@RequestMapping(\"/api/portal/knowledge/graph\")");
    }

    private static Path interfaceSourceRoot() {
        return repoRoot()
                .resolve("kuzhambu-servers/biz/knowledge/kuzhambu-knowledge-interface/src/main/java")
                .resolve("com/thundax/kuzhambu/knowledge/interfaces");
    }

    private static Path repoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("db/schema/knowledge.sql"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Repository root not found");
    }
}
