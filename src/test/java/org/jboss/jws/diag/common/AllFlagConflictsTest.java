package org.jboss.jws.diag.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AllFlagConflictsTest {

    private static final Path SOME_PATH = Path.of("/opt/tomcat");

    @Test
    void withoutAll_pathOptionsNeverConflict() {
        String message = AllFlagConflicts.when(false)
                .option("--catalina-home", SOME_PATH)
                .option("--catalina-base", SOME_PATH)
                .message();

        assertThat(message).isNull();
    }

    @Test
    void withAll_unsetOptionsDoNotConflict() {
        String message = AllFlagConflicts.when(true)
                .option("--catalina-home", null)
                .option("--catalina-base", null)
                .message();

        assertThat(message).isNull();
    }

    @Test
    void withAll_singleOptionIsNamed() {
        String message = AllFlagConflicts.when(true)
                .option("--catalina-home", null)
                .option("--catalina-base", SOME_PATH)
                .message();

        assertThat(message).isEqualTo("ERROR: --catalina-base cannot be combined with --all. "
                + "Each instance's paths come from its own running process.");
    }

    @Test
    void withAll_twoOptionsAreJoinedWithAnd() {
        String message = AllFlagConflicts.when(true)
                .option("--left", SOME_PATH)
                .option("--right", SOME_PATH)
                .message();

        assertThat(message).startsWith("ERROR: --left and --right cannot be combined with --all.");
    }

    @Test
    void withAll_threeOptionsAreListedInOrder() {
        String message = AllFlagConflicts.when(true)
                .option("--log-file", SOME_PATH)
                .option("--catalina-home", SOME_PATH)
                .option("--catalina-base", SOME_PATH)
                .message();

        assertThat(message)
                .startsWith("ERROR: --log-file, --catalina-home and --catalina-base cannot be combined with --all.");
    }
}
