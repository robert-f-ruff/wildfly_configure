package io.github.robert_f_ruff.wildfly_configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a template file, a name and path for the configuration file, and the path where the
 * secrets are mounted, this class will generate the WildFly configuration yaml file.
 * @author Robert F. Ruff
 * @version 1.0
 */
public class WildFlyConfigure {
  /**
   * Error occurred while reading a secret file.
   * @author Robert F. Ruff
   * @version 1.0
   */
  public class SecretException extends Exception {
    /**
     * New instance of SecretException.
     * @param message Text describing the error
     */
    public SecretException(String message) {
      super(message);
    }
  }

  private File templateFile;
  private File configFile;
  private File secretPath;
  private String dbServerAddress;
  private String dbServerPort;
  private String dbUserName;
  private String dbUserPassword;
  
  /**
   * Returns the database server's address retrieved from the secret file.
   * @return Database server address
   */
  public String getdbServerAddress() {
    return dbServerAddress;
  }

  /**
   * Returns the database server's port number retrieved from the secret file.
   * @return Database server port number
   */
  public String getdbServerPort() {
    return dbServerPort;
  }

  /**
   * Returns the database server's user name retrieved from the secret file.
   * @return Database server user name
   */
  public String getdbUserName() {
    return dbUserName;
  }

  /**
   * Returns the password for the database server's user account retrieved from the secret file.
   * @return Password for the database server user account
   */
  public String getdbUserPassword() {
    return dbUserPassword;
  }

  /**
   * Generates the WildFly yaml configuration file.
   * @since 1.0
   * @throws FileNotFoundException Template file or secrets path no longer exists since this class was instantiated
   * @throws IOException Error while reading from or writing to a file
   * @throws SecretException Error while reading the contents of a secret file
   */
  public void substitute() throws FileNotFoundException, IOException, SecretException {
    Pattern secretName = Pattern.compile("\\$\\{(.+?)\\}");
    try (BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter configWriter = new BufferedWriter(new FileWriter(configFile))) {
      String line;
      while ((line = templateReader.readLine()) != null) {
        Matcher secretMatcher = secretName.matcher(line);
        while (secretMatcher.find()) {
          String secret = "";
          try (BufferedReader secretReader = new BufferedReader(new FileReader(
              new File(secretPath, secretMatcher.group(1))))) {
            secret = secretReader.readLine();
          } catch (FileNotFoundException error) {
            throw new SecretException("Missing secret file: " + secretMatcher.group(1));
          }
          if (secret != null && secret != "") {
            switch (secretMatcher.group(1)) {
              case "db_host":
                dbServerAddress = secret;
                break;
              case "db_host_port":
                dbServerPort = secret;
                break;
              case "db_user_name":
                dbUserName = secret;
                break;
              case "db_user_password":
                dbUserPassword = secret;
                break;
            }
            line = line.replaceAll("\\$\\{" + secretMatcher.group(1) + "\\}", secret);
          } else {
            throw new SecretException("Undefined secret: " + secretMatcher.group(1));
          }
        }
        configWriter.write(line, 0, line.length());
        configWriter.newLine();
      }
    }
  }

  /**
   * New instance of WildFlyConfigure.
   * @param templateFile Path and name of the template file
   * @param configFile Path and name of the configuration file to generate
   * @param secretPath Path where the secrets are mounted
   * @throws FileNotFoundException Template file, configuration file path or secrets path does not exist
   */
  public WildFlyConfigure(File templateFile, File configFile, File secretPath) throws FileNotFoundException {
    if (templateFile.exists()) {
      this.templateFile = templateFile;
    } else {
      throw new FileNotFoundException("Template file " + templateFile.getAbsolutePath() + " does not exist.");
    }
    if (configFile.getParent() != null) {
      if (configFile.getParentFile().exists()) {
        this.configFile = configFile;
      } else {
        throw new FileNotFoundException("Configuration file path " + configFile.getParent() + " does not exist.");
      }
    } else {
      this.configFile = configFile;
    }
    if (secretPath.exists()) {
      this.secretPath = secretPath;
    } else {
      throw new FileNotFoundException("Secrets path " + secretPath.getAbsolutePath() + " does not exist.");
    }
    dbServerAddress = "";
    dbUserName = "";
    dbUserPassword = "";
  }
}
