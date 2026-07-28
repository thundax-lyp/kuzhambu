package com.thundax.kuzhambu.common.test.architecture;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NamingArchitectureRuleSupportTest {

    @TempDir
    private Path tempDir;

    @Test
    void valueObjectIdGateShouldRejectPackagePrivateStaticMethods() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/domain/core/model/valueobject");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleId.java"),
                """
                package com.thundax.kuzhambu.sample.domain.core.model.valueobject;

                final class SampleId {
                    static SampleId of(String value) {
                        return new SampleId();
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertValueObjectIdSourcesDeclareNoStaticMethods(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void applicationCommandQueryGateShouldRejectExplicitMethods() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                public class SampleCreateCommand {
                    private String name;

                    public String nameValue() {
                        return name;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void applicationCommandQueryGateShouldRejectPackagePrivateMethods() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                public class SampleCreateCommand {
                    private String name;

                    String nameValue() {
                        return name;
                    }
                }
                """);

        assertThrows(
                AssertionError.class,
                () -> NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                        tempDir.resolve("src/main/java")));
    }

    @Test
    void applicationCommandQueryGateShouldAllowExplicitConstructors() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                public class SampleCreateCommand {
                    private String name;

                    public SampleCreateCommand(String name) {
                        this.name = name;
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                tempDir.resolve("src/main/java"));
    }

    @Test
    void applicationCommandQueryGateShouldIgnoreMethodBodyControlFlow() throws Exception {
        Path source = tempDir.resolve("src/main/java/com/thundax/kuzhambu/sample/application/core/command");
        Files.createDirectories(source);
        Files.writeString(
                source.resolve("SampleCreateCommand.java"),
                """
                package com.thundax.kuzhambu.sample.application.core.command;

                import lombok.Getter;
                import lombok.NoArgsConstructor;
                import lombok.Setter;

                @Getter
                @Setter
                @NoArgsConstructor
                public class SampleCreateCommand {
                    private String name;

                    public SampleCreateCommand(String name) {
                        if (name == null) {
                            return;
                        }
                        this.name = name;
                    }
                }
                """);

        NamingArchitectureRuleSupport.assertApplicationCommandQuerySourcesDeclareNoMethods(
                tempDir.resolve("src/main/java"));
    }
}
