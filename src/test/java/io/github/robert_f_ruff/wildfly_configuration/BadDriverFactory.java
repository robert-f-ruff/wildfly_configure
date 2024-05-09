package io.github.robert_f_ruff.wildfly_configuration;

import java.lang.reflect.InvocationTargetException;

public class BadDriverFactory implements JDBCFactory{

  @Override
  public void create() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
    throw new NoSuchMethodException("Class doesn't contain a public contstructor.");
  }

}
