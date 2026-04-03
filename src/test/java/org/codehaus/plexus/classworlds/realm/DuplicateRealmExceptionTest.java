package org.codehaus.plexus.classworlds.realm;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DuplicateRealmExceptionTest {

    @Test
    void testConstructorAndGetters() {
        ClassWorld world = new ClassWorld();
        String realmId = "testRealm";
        DuplicateRealmException exception = new DuplicateRealmException(world, realmId);
        assertSame(world, exception.getWorld());
        assertEquals(realmId, exception.getId());
        assertEquals(realmId, exception.getMessage());
    }

    @Test
    void testConstructorWithNullWorld() {
        String realmId = "testRealm";
        DuplicateRealmException exception = new DuplicateRealmException(null, realmId);
        assertNull(exception.getWorld());
        assertEquals(realmId, exception.getId());
        assertEquals(realmId, exception.getMessage());
    }

    @Test
    void testConstructorWithNullId() {
        ClassWorld world = new ClassWorld();
        DuplicateRealmException exception = new DuplicateRealmException(world, null);
        assertSame(world, exception.getWorld());
        assertNull(exception.getId());
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithBothNull() {
        DuplicateRealmException exception = new DuplicateRealmException(null, null);
        assertNull(exception.getWorld());
        assertNull(exception.getId());
        assertNull(exception.getMessage());
    }
}
