package org.codehaus.plexus.classworlds.launcher;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Invalid configuration";
        ConfigurationException exception = new ConfigurationException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageLineNoAndLine() {
        String message = "Syntax error";
        int lineNo = 42;
        String line = "invalid line content";
        ConfigurationException exception = new ConfigurationException(message, lineNo, line);

        String expectedMessage = message + " (" + lineNo + "): " + line;
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithCause() {
        IOException cause = new IOException("Resource not found");
        ConfigurationException exception = new ConfigurationException(cause);
        assertNotNull(exception.getCause());
        assertInstanceOf(IOException.class, exception.getCause());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithNullMessage() {
        ConfigurationException exception = new ConfigurationException((String) null);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithNullLineNoAndNullLine() {
        String message = "Test message";
        ConfigurationException exception = new ConfigurationException(message, 0, null);
        String expectedMessage = message + " (0): null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testConstructorWithNegativeLineNo() {
        String message = "Test message";
        String line = "test line";
        ConfigurationException exception = new ConfigurationException(message, -1, line);
        String expectedMessage = message + " (-1): " + line;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testConstructorWithNullCause() {
        ConfigurationException exception = new ConfigurationException((Exception) null);
        assertNull(exception.getCause());
        assertNull(exception.getMessage());
    }
}
