package org.codehaus.plexus.classworlds.strategy;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OsgiBundleStrategyTest extends AbstractClassWorldsTestCase {

    private ClassRealm realm;
    private OsgiBundleStrategy strategy;

    @BeforeEach
    void setUp() throws Exception {
        ClassWorld world = new ClassWorld();
        this.realm = world.newRealm("testRealm");
        this.strategy = new OsgiBundleStrategy(realm);
        realm.addURL(getJarUrl("component0-1.0.jar"));
    }

    @Test
    void testConstructor() {
        assertNotNull(strategy);
        assertSame(realm, strategy.getRealm());
    }

    @Test
    void testConstructorWithNullRealm() {
        OsgiBundleStrategy strategy = new OsgiBundleStrategy(null);
        assertNotNull(strategy);
    }

    @Test
    void testLoadClassFromSelf() throws Exception {
        Class<?> clazz = strategy.loadClass("org.codehaus.plexus.Component0");
        assertNotNull(clazz);
    }

    @Test
    void testLoadClassNonExistent() {
        try {
            strategy.loadClass("org.codehaus.plexus.NonExistent");
            fail("Should throw ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertEquals("org.codehaus.plexus.NonExistent", e.getMessage());
        }
    }

    @Test
    void testLoadClassFromImportPriorityOrder() throws Exception {
        realm.importFromParent("org.codehaus.plexus");
        realm.addURL(getJarUrl("component1-1.0.jar"));

        Class<?> clazz = strategy.loadClass("org.codehaus.plexus.Component0");
        assertNotNull(clazz);
    }

    @Test
    void testGetResourceFromImport() throws Exception {
        realm.importFromParent("META-INF");
        URL resource = strategy.getResource("META-INF/plexus/components.xml");
        assertNotNull(resource);
    }

    @Test
    void testGetResourceFromSelf() throws Exception {
        URL resource = strategy.getResource("META-INF/plexus/components.xml");
        assertNotNull(resource);
    }

    @Test
    void testGetResourceWithWildcardImport() throws Exception {
        realm.importFromParent("org.codehaus.plexus");
        URL resource = strategy.getResource("org/codehaus/plexus/Component0.class");
        assertNotNull(resource, "Should find resource from import");
    }

    @Test
    void testGetResourceNonExistent() {
        URL resource = strategy.getResource("non-existent-resource.txt");
        assertNull(resource);
    }

    @Test
    void testGetResourcesFromAllSources() throws Exception {
        realm.importFromParent("META-INF");
        realm.addURL(getJarUrl("component1-1.0.jar"));

        Enumeration<URL> resources = strategy.getResources("META-INF/plexus/components.xml");
        assertNotNull(resources);

        int count = 0;
        while (resources.hasMoreElements()) {
            resources.nextElement();
            count++;
        }

        assertTrue(count >= 1);
    }

    @Test
    void testGetResourcesFromSelf() throws Exception {
        realm.addURL(getJarUrl("component1-1.0.jar"));

        Enumeration<URL> resources = strategy.getResources("META-INF/plexus/components.xml");
        assertNotNull(resources);

        int count = 0;
        while (resources.hasMoreElements()) {
            resources.nextElement();
            count++;
        }

        assertTrue(count >= 2);
    }

    @Test
    void testGetResourcesNonExistent() throws Exception {
        Enumeration<URL> resources = strategy.getResources("non-existent-resource.txt");
        assertNotNull(resources);

        int count = 0;
        while (resources.hasMoreElements()) {
            resources.nextElement();
            count++;
        }

        assertEquals(0, count);
    }

    @Test
    void testLoadClassWithNullName() throws ClassNotFoundException {
        try {
            strategy.loadClass(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            succeed();
        }
    }

    @Test
    void testGetResourceWithNullName() {
        try {
            strategy.getResource(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            succeed();
        }
    }

    @Test
    void testGetResourcesWithNullName() throws IOException {
        try {
            strategy.getResources(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            succeed();
        }
    }

    @Test
    void testLoadClassWithEmptyName() {
        try {
            strategy.loadClass("");
            fail("Should throw ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            assertEquals("", e.getMessage());
        }
    }

    @Test
    void testGetResourceWithEmptyName() {
        URL resource = strategy.getResource("");
        assertNull(resource, "Empty resource name should return null");
    }

    private void succeed() {}
}
