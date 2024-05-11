package io.github.robert_f_ruff.wildfly_configuration;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import io.github.robert_f_ruff.wildfly_configuration.WildFlyWait.WaitException;

@TestInstance(Lifecycle.PER_CLASS)
public class WildFlyWaitTest {
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errorContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalError = System.err;
  
  @BeforeAll
  void setup() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errorContent));
  }

  @BeforeEach
  void clean() {
    outContent.reset();
    errorContent.reset();
  }

  @AfterAll
  void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalError);
  }

  @Test
  void testImmediateConnection() throws SQLException, WaitException {
    final String serverAddress = "127.0.0.1";
    final String serverPort = "3306";
    Connection connection = mock(Connection.class);
    try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
      mocked.when(() -> 
            DriverManager.getConnection(anyString(), anyString(), anyString())
          ).thenReturn(connection);

      WildFlyWait tested = new WildFlyWait(new DriverFactory());
      tested.waitForServer(serverAddress, "", "rules", "password");

      assertEquals("Verifying connection to database:\nConnection verified!\n",
          outContent.toString());
      ArgumentCaptor<String> serverAddressCaptor = ArgumentCaptor.forClass(String.class);
      mocked.verify(() -> DriverManager.getConnection(serverAddressCaptor.capture(), anyString(),
          anyString()), times(2));
      assertEquals("jdbc:mysql://" + serverAddress + ":" + serverPort + "/rules",
          serverAddressCaptor.getValue());
    }
  }

  @Test
  void testDelayedConnection() throws SQLException, WaitException, InterruptedException {
    try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
      mocked.when(() ->
            DriverManager.getConnection(anyString(), anyString(), anyString())
          ).thenAnswer(new ConnectionMock());

      WildFlyWait tested = new WildFlyWait(new DriverFactory());
      tested.waitForServer("127.0.0.1", "3306", "rules", "password");

      String[] output = outContent.toString().split("\n");
      assertEquals("Verifying connection to database:", output[0]);
      assertTrue(output[1].contains("."));
      assertEquals("Connection verified!", output[2]);
    }
  }

  @Test
  void testWaitException() throws WaitException {
    Exception exception = assertThrows(WaitException.class, () -> new WildFlyWait(new BadDriverFactory()));
    assertEquals("Could not locate the MySQL JDBC driver.", exception.getMessage());
    assertEquals(NoSuchMethodException.class, exception.getCause().getClass());
  }

  @Test
  void testCloseException() throws WaitException, SQLException {
    final String serverAddress = "127.0.0.1";
    final String serverPort = "5506";
    Connection connection = mock(Connection.class);
    Mockito.doThrow(new SQLException()).when(connection).close();
    try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
      mocked.when(() ->
            DriverManager.getConnection(anyString(), anyString(), anyString())
          ).thenReturn(connection);

      WildFlyWait tested = new WildFlyWait(new DriverFactory());
      tested.waitForServer(serverAddress, serverPort, "rules", "password");

      assertEquals("Verifying connection to database:\nConnection verified!\n",
          outContent.toString());
      ArgumentCaptor<String> serverAddressCaptor = ArgumentCaptor.forClass(String.class);
      mocked.verify(() -> 
          DriverManager.getConnection(serverAddressCaptor.capture(), anyString(),anyString()), times(2));
      assertEquals("jdbc:mysql://" + serverAddress + ":" + serverPort + "/rules",
          serverAddressCaptor.getValue());
    }
  }

  @Test
  void testFirstInterruptedException() throws InterruptedException {
    AtomicBoolean successfulResult = new AtomicBoolean();
    Runnable toBeInterrupted = () -> {
      try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
        mocked.when(() ->
              DriverManager.getConnection(anyString(), anyString(), anyString())
            ).thenAnswer(new ConnectionMock());
  
        WildFlyWait tested = new WildFlyWait(new DriverFactory());
        tested.waitForServer("127.0.0.1", "3306", "rules", "password");
  
        String[] output = outContent.toString().split("\n");
        if (output[0].equals("Verifying connection to database:") && output[1].contains(".") && output[2].equals("Connection verified!")) {
          successfulResult.set(true);
        }
      } catch (WaitException error) {
        error.printStackTrace();
        successfulResult.set(false);
      }
    };
    Thread testThread = new Thread(toBeInterrupted);
    testThread.start();
    while (true) {
      if (testThread.getState() == Thread.State.TIMED_WAITING) {
        testThread.interrupt();
        break;
      }
    }
    testThread.join();
    assertTrue(successfulResult.get());
  }

  @Test
  void testSecondInterruptedException() throws InterruptedException {
    AtomicBoolean successfulResult = new AtomicBoolean();
    ConnectionMock mocker = new ConnectionMock();
    Runnable toBeInterrupted = () -> {
      try (MockedStatic<DriverManager> mocked = mockStatic(DriverManager.class)) {
        mocked.when(() ->
              DriverManager.getConnection(anyString(), anyString(), anyString())
            ).thenAnswer(mocker);
  
        WildFlyWait tested = new WildFlyWait(new DriverFactory());
        tested.waitForServer("127.0.0.1", "3306", "rules", "password");
  
        String[] output = outContent.toString().split("\n");
        if (output[0].equals("Verifying connection to database:") && output[1].contains(".") && output[2].equals("Connection verified!")) {
          successfulResult.set(true);
        }
      } catch (WaitException error) {
        error.printStackTrace();
        successfulResult.set(false);
      }
    };
    Thread testThread = new Thread(toBeInterrupted);
    testThread.start();
    while (true) {
      if (testThread.getState() == Thread.State.TIMED_WAITING && mocker.getServerReset()) {
        testThread.interrupt();
        break;
      }
    }
    testThread.join();
    assertTrue(successfulResult.get());
  }
}
