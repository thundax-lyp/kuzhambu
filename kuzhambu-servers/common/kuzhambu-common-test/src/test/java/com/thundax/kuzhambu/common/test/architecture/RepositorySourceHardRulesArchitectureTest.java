package com.thundax.kuzhambu.common.test.architecture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositorySourceHardRulesArchitectureTest {

    private static final Path SERVER_SOURCE_ROOT = Path.of("../..");

    @Test
    void productionSourceShouldKeepJsonAndPackageHardRules() throws Exception {
        SourceHardRuleArchitectureRuleSupport.assertProductionSourcesUseJacksonJsonOnly(SERVER_SOURCE_ROOT);
        SourceHardRuleArchitectureRuleSupport.assertBusinessLayersDoNotUseTopLevelToolPackages(SERVER_SOURCE_ROOT);
        SourceHardRuleArchitectureRuleSupport
                .assertApplicationAndRepositoryImplementationsDoNotUseIllegalArgumentException(SERVER_SOURCE_ROOT);
    }

    @Test
    void configurationPropertiesShouldNotDeclareBusinessControlFlow() throws Exception {
        SourceHardRuleArchitectureRuleSupport.assertConfigurationPropertiesDoNotDeclareBusinessControlFlow(
                SERVER_SOURCE_ROOT, legacyConfigurationPropertiesBusinessControlFlowAllowances());
    }

    @Test
    void configurationPropertiesControlFlowRuleShouldIgnoreTypesOutsidePropertiesClass(@TempDir Path temporaryDirectory)
            throws Exception {
        Path sourceRoot = temporaryDirectory.resolve("src/main/java");
        Path sourceFile = sourceRoot.resolve("sample/SampleProperties.java");
        Files.createDirectories(sourceFile.getParent());
        Files.writeString(
                sourceFile,
                "@ConfigurationProperties(prefix = \"sample\")\n"
                        + "class SampleProperties {}\n"
                        + "final class UnrelatedType {\n"
                        + "    void execute() { if (true) {} }\n"
                        + "}\n");

        assertDoesNotThrow(() ->
                SourceHardRuleArchitectureRuleSupport.assertConfigurationPropertiesDoNotDeclareBusinessControlFlow(
                        sourceRoot));
    }

    private static List<ArchitectureRuleAllowance> legacyConfigurationPropertiesBusinessControlFlowAllowances() {
        return List.of(
                ArchitectureRuleAllowance.of(
                        SourceHardRuleArchitectureRuleSupport.configurationPropertiesBusinessControlFlowAllowanceKey(
                                "kuzhambu-servers/biz/system/kuzhambu-system-interface/src/main/java/com/thundax/"
                                        + "kuzhambu/system/interfaces/admin/configure/KuzhambuProperties.java"),
                        "KuzhambuProperties combines legacy nested configuration binding with defaults and normalization for upload, logging, and access-token filtering.",
                        "Split the nested bindings into plain configuration properties and move each defaulting or normalization rule to its owning upload, logging, or security policy before removing this allowance."));
    }
}
