package org.codehaus.plexus.classworlds.launcher;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.TestUtil;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

class LauncherTest extends AbstractClassWorldsTestCase {
    private Launcher launcher;

    @BeforeEach
    void setUp() {
        this.launcher = new Launcher();

        this.launcher.setSystemClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @AfterEach
    void tearDown() {
        this.launcher = null;
    }

    @Test
    void configureValid() throws Exception {
        launcher.configure(getConfigPath("valid-launch.conf"));

        Class<?> mainClass = launcher.getMainClass();

        assertNotNull(mainClass);

        assertEquals("a.A", mainClass.getName());

        assertEquals("app", launcher.getMainRealm().getId());
    }

    @Test
    void launchValidStandard() throws Exception {
        launcher.configure(getConfigPath("valid-launch.conf"));

        launcher.launch(new String[] {});
    }

    @Test
    void launchValidStandardExitCode() throws Exception {
        launcher.configure(getConfigPath("valid-launch-exitCode.conf"));

        launcher.launch(new String[] {});

        assertEquals(15, launcher.getExitCode(), "check exit code");
    }

    @Test
    void launchValidEnhanced() throws Exception {
        launcher.configure(getConfigPath("valid-enh-launch.conf"));

        launcher.launch(new String[] {});
    }

    @Test
    void launchValidEnhancedExitCode() throws Exception {
        launcher.configure(getConfigPath("valid-enh-launch-exitCode.conf"));

        launcher.launch(new String[] {});

        assertEquals(45, launcher.getExitCode(), "check exit code");
    }

    @Test
    void launchNoSuchMethod() throws Exception {
        launcher.configure(getConfigPath("launch-nomethod.conf"));

        try {
            launcher.launch(new String[] {});
            fail("should have thrown NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            // expected and correct
        }
    }

    @Test
    void launchClassNotFound() throws Exception {
        launcher.configure(getConfigPath("launch-noclass.conf"));

        try {
            launcher.launch(new String[] {});
            fail("throw ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // expected and correct
        }
    }

    @Test
    void testGetSetSystemClassLoader() {
        ClassLoader testLoader = Thread.currentThread().getContextClassLoader();
        launcher.setSystemClassLoader(testLoader);

        ClassLoader result = launcher.getSystemClassLoader();
        assertSame(testLoader, result);
    }

    @Test
    void testDefaultSystemClassLoader() {
        assertNotNull(launcher.getSystemClassLoader());
    }

    @Test
    void testGetSetAppMain() {
        launcher.setAppMain("com.example.Main", "mainRealm");

        assertEquals("com.example.Main", launcher.getMainClassName());
        assertEquals("mainRealm", launcher.getMainRealmName());
    }

    @Test
    void testGetMainRealmWithoutConfiguration() {
        try {
            launcher.getMainRealm();
            fail("Should throw NullPointerException when world is not configured");
        } catch (NullPointerException | NoSuchRealmException e) {
            succeed();
        }
    }

    @Test
    void testGetSetWorld() {
        ClassWorld world = new ClassWorld();
        launcher.setWorld(world);

        ClassWorld result = launcher.getWorld();
        assertSame(world, result);
    }

    @Test
    void testDefaultExitCode() {
        assertEquals(0, launcher.getExitCode());
    }

    @Test
    void testConfigureInvalid() {
        try {
            launcher.configure(getConfigPath("non-existent.conf"));
            fail("Should throw FileNotFoundException");
        } catch (FileNotFoundException e) {
            // expected
        } catch (Exception e) {
            fail("Should throw FileNotFoundException, got: " + e.getClass().getName());
        }
    }

    @Test
    void testLauncherWithArguments() throws Exception {
        launcher.configure(getConfigPath("valid-launch.conf"));

        String[] args = {"arg1", "arg2", "arg3"};
        launcher.launch(args);

        assertEquals(0, launcher.getExitCode());
    }

    @Test
    void testLauncherWithEmptyArguments() throws Exception {
        launcher.configure(getConfigPath("valid-launch.conf"));

        String[] args = {};
        launcher.launch(args);

        assertEquals(0, launcher.getExitCode());
    }

    @Test
    void testLauncherWithNullArguments() throws Exception {
        launcher.configure(getConfigPath("valid-launch.conf"));

        launcher.launch(null);

        assertEquals(0, launcher.getExitCode());
    }

    @Test
    void testGetMainClassWithoutConfiguration() {
        try {
            launcher.getMainClass();
            fail("Should throw IllegalStateException or return null");
        } catch (Exception e) {
            succeed();
        }
    }

    @Test
    void testMultipleConfigurations() throws Exception {
        launcher.configure(getConfigPath("valid-launch.conf"));

        String mainClass1 = launcher.getMainClassName();
        String realm1 = launcher.getMainRealmName();

        launcher.configure(getConfigPath("valid-enh-launch.conf"));

        String mainClass2 = launcher.getMainClassName();
        String realm2 = launcher.getMainRealmName();

        assertNotNull(mainClass1);
        assertNotNull(mainClass2);
        assertNotNull(realm1);
        assertNotNull(realm2);
    }

    @Test
    void testLaunchWithNonExistentRealmInArgs() throws Exception {
        launcher.configure(getConfigPath("valid-launch.conf"));

        String[] args = {"--world=test", "--realm=nonexistent"};

        try {
            launcher.launch(args);
            succeed();
        } catch (Exception e) {
            fail("Should handle non-existent realm gracefully");
        }
    }

    private FileInputStream getConfigPath(String name) throws Exception {
        String basedir = TestUtil.getBasedir();

        return new FileInputStream(new File(new File(basedir, "src/test/test-data"), name));
    }

    private void succeed() {}
}
