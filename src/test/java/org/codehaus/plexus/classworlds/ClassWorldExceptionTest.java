package org.codehaus.plexus.classworlds;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ClassWorldExceptionTest {

    @Test
    void testConstructorWithWorld() {
        ClassWorld world = new ClassWorld();
        ClassWorldException exception = new ClassWorldException(world);
        assertSame(world, exception.getWorld());
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithWorldAndMessage() {
        ClassWorld world = new ClassWorld();
        String message = "Test exception message";
        ClassWorldException exception = new ClassWorldException(world, message);
        assertSame(world, exception.getWorld());
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testConstructorWithNullWorldAndNullMessage() {
        ClassWorldException exception = new ClassWorldException(null, null);
        assertNull(exception.getWorld());
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithNullWorld() {
        ClassWorldException exception = new ClassWorldException(null);
        assertNull(exception.getWorld());
        assertNull(exception.getMessage());
    }
}
