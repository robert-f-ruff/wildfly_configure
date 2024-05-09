package io.github.robert_f_ruff.wildfly_configuration;

import java.io.File;

/**
 * <p>Execute the program logic.</p>
 * <p>WildFly Configuration performs two tasks:</p>
 * <ol>
 *   <li>Takes a wildfly_config.yml.tmpl file and the Docker container's mounted secrets and
 *       generates a wildfly_config.yml file that will be used to configure the WildFly
 *       server before the container starts it.</li>
 *   <li>Verifies that the database server is running and accepting network requests. WildFly
 *       Configuration will block until connectivity with the database server is verified.</li>
 * </ol>
 * @author Robert F. Ruff
 * @version 1.0
 */
public class Main {
  /**
   * The executable entry point into the application.
   * @param args Command line arguments.
   * <table>
   *   <caption>args Array Elements Definition</caption>
   *   <thead>
   *     <tr>
   *       <th scope="col">Array Element</th>
   *       <th scope="col">Parameter Name</th>
   *       <th scope="col">Purpose</th>
   *     </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <th scope="row">args[0]</th>
   *       <td>template_file</td>
   *       <td>File name and path to the template file (i.e., wildfly_config.yml.tmpl)</td>
   *     </tr>
   *     <tr>
   *       <th scope="row">args[1]</th>
   *       <td>output_file</td>
   *       <td>File name and path to place the populated configuration file (i.e., wildfly_config.yml)</td>
   *     </tr>
   *     <tr>
   *       <th scope="row">args[2]</th>
   *       <td>secrets_path</td>
   *       <td>Path where the secrets files are mounted (i.e., /run/secrets)</td>
   *     </tr>
   *   </tbody>
   * </table>
   */
  public static void main(String[] args) {
    try {
      WildFlyConfigure converter = new WildFlyConfigure(new File(args[0]), new File(args[1]),
          new File(args[2]));
      converter.substitute();
      System.out.println("Successfully created " + args[1]);
      WildFlyWait waiter = new WildFlyWait(new DriverFactory());
      waiter.waitForServer(converter.getdbServerAddress(), converter.getdbServerPort(),
          converter.getdbUserName(), converter.getdbUserPassword());
    } catch (Exception error) {
      System.err.println(error.getMessage());
      printUsage();
    }
  }

  private static void printUsage() {
    String help = """
        NAME: WildFlyConfigure

        SYNOPSIS
            java -jar WildFlyConfigure.jar <template_file> <output_file> <secrets_path>

        DESCRIPTION
            Populates a WildFly configuration YAML file template with the defined secrets.

            <template_file>: File name and path to the template file.
            <output_file>: File name and path to place the populated configuration file.
            <secrets_path>: Path where the secrets files are mounted.
        """;
    System.out.println(help);
  }

  // Ensure Jacoco reports accurate code coverage percentage
  private Main() {

  }
}
