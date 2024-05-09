package io.github.robert_f_ruff.wildfly_configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
/**
 * Given the connection information for the rules database (host address, host port number, user
 * account name, and user account password), verify that the database server accepts network
 * connections.
 * @author Robert F. Ruff
 * @version 1.0
 */
public class WildFlyWait {
  /**
   * Error occurred while obtaining an instance of the JDBC driver.
   * @author Robert F. Ruff
   * @version 1.0
   */
  public class WaitException extends Exception {
    /**
     * New instance of WaitException.
     * @param message Text describing the error
     * @param cause Exception that was captured while attempting to create a JDBC driver instance
     */
    public WaitException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Every two seconds, check whether the database server is accepting network connections.
   * @param serverAddress Host server's network address
   * @param serverPort Host server's port number
   * @param userName Database user account's name
   * @param userPassword Database user account's password
   */
  public void waitForServer(String serverAddress, String serverPort, String userName,
      String userPassword) {
    if (serverPort == "") {
      serverPort = "3306";
    }
    System.out.println("Verifying connection to database:");
    Connection connection = null;
    boolean printedDots = false;
    do {
      try {
        connection = DriverManager.getConnection("jdbc:mysql://" + serverAddress + ":" + serverPort
            + "/rules", userName, userPassword);
      } catch (SQLException error) {
        System.out.print(".");
        System.out.flush();
        printedDots = true;
        try {
          TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException errorInterrupt) {
          // Don't care
        }
      }
    } while (connection == null);
    System.out.println(printedDots ? "\nConnection verified!" : "Connection verified!");
    try {
      connection.close();
    } catch (SQLException error) {
      // Don't care
    }
  }

  /**
   * New instance of WildFlyWait.
   * @param factory Factory that will instantiate the JDBC driver
   * @throws WaitException Error while instantiating the JDBC driver
   */
  public WildFlyWait(JDBCFactory factory) throws WaitException {
    try {
      factory.create();
    } catch (Exception error) {
      throw new WaitException("Could not locate the MySQL JDBC driver.", error);
    }
  }
}
