package com.thundax.kuzhambu.common.test.architecture;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RepositorySourceHardRulesArchitectureTest {

    private static final Path SERVER_SOURCE_ROOT = Path.of("../..");

    @Test
    void productionSourceShouldKeepJsonAndPackageHardRules() throws Exception {
        SourceHardRuleArchitectureRuleSupport.assertProductionSourcesUseJacksonJsonOnly(SERVER_SOURCE_ROOT);
        SourceHardRuleArchitectureRuleSupport.assertBusinessLayersDoNotUseTopLevelToolPackages(SERVER_SOURCE_ROOT);
        SourceHardRuleArchitectureRuleSupport
                .assertApplicationAndRepositoryImplementationsDoNotUseIllegalArgumentException(SERVER_SOURCE_ROOT);
        TransactionArchitectureRuleSupport.assertServerTransactionalOnlyOnApplicationServiceOrFacadeUseCases(
                SERVER_SOURCE_ROOT);
    }
}
