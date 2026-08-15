package org.jboss.jws.diag.bundle;

import org.jboss.jws.diag.common.RedactionLevel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BundleContextTest {

    @Test
    void shouldExposeAllConstructorValues() {
        Path catalinaBase = Paths.get("/opt/tomcat");
        Path catalinaHome = Paths.get("/opt/tomcat-home");
        Path stagingDir = Paths.get("/tmp/staging");

        BundleContext context = new BundleContext(catalinaBase, catalinaHome, stagingDir, RedactionLevel.STRICT);

        assertThat(context.getCatalinaBase()).isEqualTo(catalinaBase);
        assertThat(context.getCatalinaHome()).isEqualTo(catalinaHome);
        assertThat(context.getStagingDir()).isEqualTo(stagingDir);
        assertThat(context.getRedactionLevel()).isEqualTo(RedactionLevel.STRICT);
    }

    @Test
    void shouldThrowWhenCatalinaBaseIsNull() {
        assertThatThrownBy(() -> new BundleContext(null, Paths.get("/opt/tomcat"), Paths.get("/tmp"), RedactionLevel.DEFAULT))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("catalinaBase");
    }

    @Test
    void shouldThrowWhenCatalinaHomeIsNull() {
        assertThatThrownBy(() -> new BundleContext(Paths.get("/opt/tomcat"), null, Paths.get("/tmp"), RedactionLevel.DEFAULT))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("catalinaHome");
    }

    @Test
    void shouldThrowWhenStagingDirIsNull() {
        assertThatThrownBy(() -> new BundleContext(Paths.get("/opt/tomcat"), Paths.get("/opt/tomcat"), null, RedactionLevel.DEFAULT))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("stagingDir");
    }

    @Test
    void shouldThrowWhenRedactionLevelIsNull() {
        assertThatThrownBy(() -> new BundleContext(Paths.get("/opt/tomcat"), Paths.get("/opt/tomcat"), Paths.get("/tmp"), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("redactionLevel");
    }
}