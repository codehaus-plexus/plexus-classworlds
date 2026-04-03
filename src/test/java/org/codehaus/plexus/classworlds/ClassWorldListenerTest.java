package org.codehaus.plexus.classworlds;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassWorldListenerTest {

    @Test
    void testRealmCreatedNotification() throws Exception {
        ClassWorld world = new ClassWorld();
        TestListener listener = new TestListener();
        world.addListener(listener);

        ClassRealm realm = world.newRealm("testRealm");

        assertEquals(1, listener.realmCreatedCount);
        assertSame(realm, listener.lastCreatedRealm);
        assertEquals(0, listener.realmDisposedCount);
    }

    @Test
    void testRealmDisposedNotification() throws Exception {
        ClassWorld world = new ClassWorld();
        TestListener listener = new TestListener();
        world.addListener(listener);

        ClassRealm realm = world.newRealm("testRealm");
        world.disposeRealm("testRealm");

        assertEquals(1, listener.realmCreatedCount);
        assertEquals(1, listener.realmDisposedCount);
        assertSame(realm, listener.lastDisposedRealm);
    }

    @Test
    void testMultipleListeners() throws Exception {
        ClassWorld world = new ClassWorld();
        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();

        world.addListener(listener1);
        world.addListener(listener2);

        ClassRealm realm = world.newRealm("testRealm");

        assertEquals(1, listener1.realmCreatedCount);
        assertEquals(1, listener2.realmCreatedCount);
        assertSame(realm, listener1.lastCreatedRealm);
        assertSame(realm, listener2.lastCreatedRealm);
    }

    @Test
    void testListenerNotifiedOnClose() throws Exception {
        ClassWorld world = new ClassWorld();
        TestListener listener = new TestListener();
        world.addListener(listener);

        world.newRealm("realm1");
        world.newRealm("realm2");
        world.newRealm("realm3");

        assertEquals(3, listener.realmCreatedCount);

        world.close();

        assertEquals(3, listener.realmDisposedCount);
    }

    @Test
    void testRemoveListener() throws Exception {
        ClassWorld world = new ClassWorld();
        TestListener listener = new TestListener();
        world.addListener(listener);

        ClassRealm realm1 = world.newRealm("realm1");
        assertEquals(1, listener.realmCreatedCount);

        world.removeListener(listener);

        ClassRealm realm2 = world.newRealm("realm2");
        assertEquals(1, listener.realmCreatedCount);
        assertSame(realm1, listener.lastCreatedRealm);
    }

    @Test
    void testAddDuplicateListener() throws Exception {
        ClassWorld world = new ClassWorld();
        TestListener listener = new TestListener();

        world.addListener(listener);
        world.addListener(listener);

        ClassRealm realm = world.newRealm("testRealm");

        assertEquals(1, listener.realmCreatedCount);
    }

    @Test
    void testListenerWithNoSuchRealmException() {
        ClassWorld world = new ClassWorld();
        TestListener listener = new TestListener();
        world.addListener(listener);

        assertThrows(NoSuchRealmException.class, () -> world.disposeRealm("nonExistent"));

        assertEquals(0, listener.realmDisposedCount);
    }

    static class TestListener implements ClassWorldListener {
        int realmCreatedCount = 0;
        int realmDisposedCount = 0;
        ClassRealm lastCreatedRealm;
        ClassRealm lastDisposedRealm;

        @Override
        public void realmCreated(ClassRealm realm) {
            realmCreatedCount++;
            lastCreatedRealm = realm;
        }

        @Override
        public void realmDisposed(ClassRealm realm) {
            realmDisposedCount++;
            lastDisposedRealm = realm;
        }
    }
}
