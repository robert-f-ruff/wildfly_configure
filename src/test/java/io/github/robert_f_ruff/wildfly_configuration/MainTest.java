package io.github.robert_f_ruff.wildfly_configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.MockedStatic;

@TestInstance(Lifecycle.PER_CLASS)
public class MainTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalError = System.err;
  private final String configFileName = "wildfly_config.yml";

  @BeforeAll
  void setup() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errorContent));
  }

  @BeforeEach
  void clean() {
    outContent.reset();
    errorContent.reset();
    URL resource = this.getClass().getResource("/" + configFileName);
    if (resource != null) {
      File output = new File(resource.getPath());
      output.delete();
    }
  }

  @AfterAll
  void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalError);
    URL resource = this.getClass().getResource("/" + configFileName);
    if (resource != null) {
      File output = new File(resource.getPath());
      output.delete();
    }
  }

  @Test
  void testValidRun() {
    String templateFile = this.getClass().getResource("/wildfly_config.yml.tmpl").getPath();
    String resourceFolder = new File(templateFile).getParent();
    String configFile = new File(resourceFolder, configFileName).getAbsolutePath();
    String secretsPath = new File(resourceFolder, "secrets").getAbsolutePath();
    String[] arguments = {templateFile, configFile, secretsPath};
    Connection connection = mock(Connection.class);
    try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
      mocked.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(connection);
      Main.main(arguments);
    }
    assertEquals("Successfully created " + configFile
        + "\nVerifying connection to database:\nConnection verified!\n", outContent.toString());
    assertEquals("", errorContent.toString());
  }

  @Test
  void testInvalidRun() {
    String templateFile = this.getClass().getResource("/missing_wildfly_config.yml.tmpl").getPath();
    String resourceFolder = new File(templateFile).getParent();
    String configFile = new File(resourceFolder, configFileName).getAbsolutePath();
    String secretsPath = new File(resourceFolder, "secrets").getAbsolutePath();
    String[] arguments = {templateFile, configFile, secretsPath};
    Connection connection = mock(Connection.class);
    try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
      mocked.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(connection);
      Main.main(arguments);
    }
    String output = """
        NAME: WildFlyConfigure

        SYNOPSIS
            java -jar WildFlyConfigure.jar <template_file> <output_file> <secrets_path>

        DESCRIPTION
            Populates a WildFly configuration YAML file template with the defined secrets.

            <template_file>: File name and path to the template file.
            <output_file>: File name and path to place the populated configuration file.
            <secrets_path>: Path where the secrets files are mounted.

        """;
    assertEquals(output, outContent.toString());
    assertEquals("Missing secret file: db_user\n", errorContent.toString());
  }
}
