package io.github.robert_f_ruff.wildfly_configuration;

import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.SQLException;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ConnectionMock implements Answer<Connection> {
  private int calledCounter;

  @Override
  public Connection answer(InvocationOnMock invocation) throws Throwable {
    calledCounter++;
    if (calledCounter < 3) {
      throw new SQLException();
    } else {
      return mock(Connection.class);
    }
  }

  public ConnectionMock() {
    calledCounter = 0;
  }
  
}
