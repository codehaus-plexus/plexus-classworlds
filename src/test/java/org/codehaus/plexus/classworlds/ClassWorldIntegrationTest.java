package org.codehaus.plexus.classworlds;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassWorldIntegrationTest {

    private ClassWorld world;

    @BeforeEach
    void setUp() {
        world = new ClassWorld();
    }

    @Test
    void testMultipleRealmsWithSameClassLoader() throws Exception {
        ClassLoader loader = new URLClassLoader(new URL[0]);
        ClassRealm realm1 = world.newRealm("realm1", loader);
        ClassRealm realm2 = world.newRealm("realm2", loader);

        assertNotNull(realm1);
        assertNotNull(realm2);
        assertEquals(2, world.getRealms().size());
    }

    @Test
    void testRealmOrderPreserved() throws Exception {
        world.newRealm("realm1");
        world.newRealm("realm2");
        world.newRealm("realm3");

        Collection<ClassRealm> realms = world.getRealms();
        java.util.Iterator<ClassRealm> iterator = realms.iterator();
        assertEquals("realm1", iterator.next().getId());
        assertEquals("realm2", iterator.next().getId());
        assertEquals("realm3", iterator.next().getId());
    }

    @Test
    void testRealmWithUnicodeId() throws Exception {
        ClassRealm realm = world.newRealm("rälm-αβγ");
        assertNotNull(realm);
        assertEquals("rälm-αβγ", realm.getId());
    }

    @Test
    void testRealmWithEmptyStringId() throws Exception {
        ClassRealm realm = world.newRealm("");
        assertNotNull(realm);
        assertEquals("", realm.getId());
    }

    @Test
    void testRealmWithSpecialCharactersId() throws Exception {
        ClassRealm realm = world.newRealm("realm_test-123.special");
        assertNotNull(realm);
        assertEquals("realm_test-123.special", realm.getId());
    }

    @Test
    void testDisposeRealmAndCreateAgain() throws Exception {
        world.newRealm("temp");
        world.disposeRealm("temp");
        ClassRealm newRealm = world.newRealm("temp");
        assertNotNull(newRealm);
        assertEquals(1, world.getRealms().size());
    }

    @Test
    void testCloseAndCreateNewRealms() throws Exception {
        world.newRealm("realm1");
        world.newRealm("realm2");
        world.close();

        assertEquals(0, world.getRealms().size());

        world.newRealm("realm3");
        assertEquals(1, world.getRealms().size());
    }

    @Test
    void testLargeNumberOfRealms() throws Exception {
        for (int i = 0; i < 100; i++) {
            world.newRealm("realm" + i);
        }
        assertEquals(100, world.getRealms().size());
    }

    @Test
    void testConcurrentRealmCreation() throws Exception {
        world.newRealm("realm1");

        Runnable createTask = () -> {
            try {
                for (int i = 0; i < 10; i++) {
                    world.newRealm("thread-realm-" + Thread.currentThread().getId() + "-" + i);
                }
            } catch (DuplicateRealmException e) {
                fail("Unexpected duplicate realm exception");
            }
        };

        Thread thread1 = new Thread(createTask);
        Thread thread2 = new Thread(createTask);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertFalse(world.getRealms().isEmpty());
    }
}
