package io.github.robert_f_ruff.wildfly_configuration;

import java.lang.reflect.InvocationTargetException;

/**
 * Instantiates a MySQL JDBC driver.
 * @author Robert F. Ruff
 * @version 1.0
 */
public final class DriverFactory implements JDBCFactory {

  /**
   * Create an instance of the MySQL JDBC driver.
   * @throws InstantiationException Driver is an abstract class
   * @throws IllegalAccessException Driver's constructor is not accessible
   * @throws IllegalArgumentException Missing or wrong type of constructor arguments 
   * @throws InvocationTargetException Constructor generated an exception
   * @throws NoSuchMethodException No constructor defined
   * @throws SecurityException Security manager error while obtaining the driver's constructor
   * @throws ClassNotFoundException Driver class could not be located
   */
  @Override
  public void create()
      throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
    Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
  }

  /**
   * Create a new instance of DriverFactory.
   */
  public DriverFactory() {

  }
  
}
