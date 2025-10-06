package org.codehaus.plexus.classworlds.realm;

import java.io.File;
import java.lang.reflect.Method;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test that packages imported from other ClassLoaders are visible.
 */
class PackageVisibilityTest extends AbstractClassWorldsTestCase {

    @Test
    void getPackageForImportedPackage() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realmA = world.newRealm("realmA");
        ClassRealm realmB = world.newRealm("realmB");

        // Add log4j to realmA (using absolute path since maven copies it to target/test-lib)
        File log4jJar = new File("target/test-lib/log4j-api-2.23.1.jar");
        if (!log4jJar.exists()) {
            // Fallback if running tests outside maven
            log4jJar = new File("../../target/test-lib/log4j-api-2.23.1.jar");
        }
        realmA.addURL(log4jJar.toURI().toURL());

        // Import the package from realmA to realmB
        realmB.importFrom("realmA", "org.apache.logging.log4j");

        // Load a class to ensure the package is defined
        Class<?> loggerClass = realmB.loadClass("org.apache.logging.log4j.Logger");
        assertNotNull(loggerClass);

        // The package should be visible through the class (this is what JEXL uses)
        Package pkgViaClass = loggerClass.getPackage();
        assertNotNull(pkgViaClass, "Package should be visible via Class.getPackage()");
        assertEquals("org.apache.logging.log4j", pkgViaClass.getName());

        // Try to test the protected getPackage() method we overrode (may fail on Java 9+ due to modules)
        try {
            Method getPackageMethod = ClassLoader.class.getDeclaredMethod("getPackage", String.class);
            getPackageMethod.setAccessible(true);
            Package pkgViaLoader = (Package) getPackageMethod.invoke(realmB, "org.apache.logging.log4j");
            assertNotNull(pkgViaLoader, "Package should be visible via ClassLoader.getPackage()");
            assertEquals("org.apache.logging.log4j", pkgViaLoader.getName());
        } catch (Exception e) {
            // Skip this check on Java 9+ if module system prevents access
            System.out.println("Skipping direct getPackage() test due to module restrictions");
        }
    }

    @Test
    void getPackagesIncludesImportedPackages() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realmA = world.newRealm("realmA");
        ClassRealm realmB = world.newRealm("realmB");

        // Add log4j to realmA
        File log4jJar = new File("target/test-lib/log4j-api-2.23.1.jar");
        if (!log4jJar.exists()) {
            log4jJar = new File("../../target/test-lib/log4j-api-2.23.1.jar");
        }
        realmA.addURL(log4jJar.toURI().toURL());

        // Import the package from realmA to realmB
        realmB.importFrom("realmA", "org.apache.logging.log4j");

        // Load a class to ensure the package is defined
        realmB.loadClass("org.apache.logging.log4j.Logger");

        // Try to test the protected getPackages() method we overrode (may fail on Java 9+ due to modules)
        try {
            Method getPackagesMethod = ClassLoader.class.getDeclaredMethod("getPackages");
            getPackagesMethod.setAccessible(true);
            Package[] packages = (Package[]) getPackagesMethod.invoke(realmB);

            // Check if the imported package is included
            boolean found = false;
            for (Package pkg : packages) {
                if ("org.apache.logging.log4j".equals(pkg.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Imported package should be included in getPackages()");
        } catch (Exception e) {
            // Skip this check on Java 9+ if module system prevents access
            System.out.println("Skipping direct getPackages() test due to module restrictions");
        }
    }

    @Test
    void getPackageForParentImportedPackage() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm parent = world.newRealm("parent");
        ClassRealm child = world.newRealm("child");

        // Add log4j to parent
        File log4jJar = new File("target/test-lib/log4j-api-2.23.1.jar");
        if (!log4jJar.exists()) {
            log4jJar = new File("../../target/test-lib/log4j-api-2.23.1.jar");
        }
        parent.addURL(log4jJar.toURI().toURL());

        // Set parent and import from parent
        child.setParentRealm(parent);
        child.importFromParent("org.apache.logging.log4j");

        // Load a class to ensure the package is defined
        Class<?> loggerClass = child.loadClass("org.apache.logging.log4j.Logger");
        assertNotNull(loggerClass);

        // The package should be visible through the class
        Package pkgViaClass = loggerClass.getPackage();
        assertNotNull(pkgViaClass, "Package should be visible via Class.getPackage()");
        assertEquals("org.apache.logging.log4j", pkgViaClass.getName());

        // Try to test the protected getPackage() method (may fail on Java 9+ due to modules)
        try {
            Method getPackageMethod = ClassLoader.class.getDeclaredMethod("getPackage", String.class);
            getPackageMethod.setAccessible(true);
            Package pkgViaLoader = (Package) getPackageMethod.invoke(child, "org.apache.logging.log4j");
            assertNotNull(pkgViaLoader, "Package should be visible from parent via ClassLoader.getPackage()");
            assertEquals("org.apache.logging.log4j", pkgViaLoader.getName());
        } catch (Exception e) {
            // Skip this check on Java 9+ if module system prevents access
            System.out.println("Skipping direct getPackage() test due to module restrictions");
        }
    }

    @Test
    void multipleImportedPackages() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realmA = world.newRealm("realmA");
        ClassRealm realmB = world.newRealm("realmB");
        ClassRealm realmC = world.newRealm("realmC");

        // Add different jars to different realms
        File log4jJar = new File("target/test-lib/log4j-api-2.23.1.jar");
        File jaxbJar = new File("target/test-lib/jakarta.xml.bind-api-4.0.4.jar");
        if (!log4jJar.exists()) {
            log4jJar = new File("../../target/test-lib/log4j-api-2.23.1.jar");
            jaxbJar = new File("../../target/test-lib/jakarta.xml.bind-api-4.0.4.jar");
        }
        realmA.addURL(log4jJar.toURI().toURL());
        realmB.addURL(jaxbJar.toURI().toURL());

        // Import packages from both realms to realmC
        realmC.importFrom("realmA", "org.apache.logging.log4j");
        realmC.importFrom("realmB", "jakarta.xml.bind");

        // Load classes from both imported packages
        Class<?> loggerClass = realmC.loadClass("org.apache.logging.log4j.Logger");
        Class<?> jaxbClass = realmC.loadClass("jakarta.xml.bind.JAXBContext");

        // Both packages should be visible
        Package log4jPkg = loggerClass.getPackage();
        assertNotNull(log4jPkg);
        assertEquals("org.apache.logging.log4j", log4jPkg.getName());

        Package jaxbPkg = jaxbClass.getPackage();
        assertNotNull(jaxbPkg);
        assertEquals("jakarta.xml.bind", jaxbPkg.getName());
    }
}
