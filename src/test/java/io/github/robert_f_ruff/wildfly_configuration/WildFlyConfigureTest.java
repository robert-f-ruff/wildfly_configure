package io.github.robert_f_ruff.wildfly_configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.robert_f_ruff.wildfly_configuration.WildFlyConfigure.SecretException;

public class WildFlyConfigureTest {
  String templateFile = this.getClass().getResource("/wildfly_config.yml.tmpl").getPath();
  String resourceFolder = templateFile.replaceAll("wildfly_config\\.yml\\.tmpl", "");
  String configFile = resourceFolder + "wildfly_config.yml";
    
  @BeforeEach
  @AfterEach
  void clean() {
    File output = new File(configFile);
    if (output.exists()) {
        output.delete();
    }
  }
  
  @Test
  void testValidRun() throws IOException, SecretException {
    File output = new File(configFile);
    assertFalse(output.exists());
    WildFlyConfigure configure = new WildFlyConfigure(new File(templateFile), output, new File(resourceFolder, "/secrets"));
    configure.substitute();
    assertTrue(output.exists());
  }

  @Test
  void testInvalidTemplateFile() {
    final File BAD = new File("non_existent");
    Exception exception = assertThrows(FileNotFoundException.class, () -> new WildFlyConfigure(BAD, new File(configFile), new File(resourceFolder, "/secrets")));
    assertEquals("Template file " + BAD.getAbsolutePath() + " does not exist.", exception.getMessage());
  }

  @Test
  void testInvalidConfigFile() {
    final File BAD = new File("/this/path/should/not/exist/wildfly_config.yml");
    Exception exception = assertThrows(FileNotFoundException.class, () -> new WildFlyConfigure(new File(templateFile), BAD, new File(resourceFolder, "/secrets")));
    assertEquals("Configuration file path " + BAD.getParent() + " does not exist.", exception.getMessage());
    final File BAD2 = new File("wildfly_config.yml");
    assertDoesNotThrow(() -> new WildFlyConfigure(new File(templateFile), BAD2, new File(resourceFolder, "/secrets")));
  }
  
  @Test
  void testInvalidSecretPath() {
    File bad = new File("non_existent/");
    final File WORSE = new File(bad.getAbsolutePath(), "/secrets");
    Exception exception = assertThrows(FileNotFoundException.class, () -> new WildFlyConfigure(new File(templateFile), new File(configFile), WORSE));
    assertEquals("Secrets path " + WORSE.getAbsolutePath() + " does not exist.", exception.getMessage());
  }

  @Test
  void testMissingSecret() throws FileNotFoundException {
    File badTemplate = new File(this.getClass().getResource("/missing_wildfly_config.yml.tmpl").getPath());
    File output = new File(configFile);
    assertFalse(output.exists());
    WildFlyConfigure configure = new WildFlyConfigure(badTemplate, output, new File(resourceFolder, "/secrets"));
    Exception exception = assertThrows(SecretException.class, () -> configure.substitute());
    assertEquals("Missing secret file: db_user", exception.getMessage());
  }

  @Test
  void testUndefinedSecret() throws FileNotFoundException {
    File badTemplate = new File(this.getClass().getResource("/undefined_wildfly_config.yml.tmpl").getPath());
    File output = new File(configFile);
    assertFalse(output.exists());
    WildFlyConfigure configure = new WildFlyConfigure(badTemplate, output, new File(resourceFolder, "/secrets"));
    Exception exception = assertThrows(SecretException.class, () -> configure.substitute());
    assertEquals("Undefined secret: bad_secret", exception.getMessage());
  }
}
