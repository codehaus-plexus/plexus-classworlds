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
import java.net.URL;
import java.util.Collection;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.TestUtil;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ConfiguratorTest extends AbstractClassWorldsTestCase {
    private Launcher launcher;
    private Configurator configurator;

    @BeforeEach
    public void setUp() {
        this.launcher = new Launcher();
        this.configurator = new Configurator(this.launcher);
    }

    @AfterEach
    public void tearDown() {
        this.launcher = null;
        this.configurator = null;
        System.getProperties().remove("set.using.existent");
        System.getProperties().remove("set.using.default");
        System.getProperties().remove("set.using.nonexistent");
        System.getProperties().remove("set.using.nonexistent.default");
        System.getProperties().remove("set.using.missing");
        System.getProperties().remove("set.using.filtered.default");
    }

    @Test
    void testConfigure_Nonexistent() throws Exception {
        try {
            this.configurator.configure(getConfigPath("notfound.conf"));
            fail("throw FileNotFoundException");
        } catch (FileNotFoundException e) {
            // expected and correct
        }
    }

    @Test
    void testConfigure_DuplicateMain() throws Exception {
        try {
            this.configurator.configure(getConfigPath("dupe-main.conf"));
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("Duplicate main"));
        }
    }

    @Test
    void testConfigure_DuplicateRealm() throws Exception {
        try {
            this.configurator.configure(getConfigPath("dupe-realm.conf"));
            fail("throw DuplicateRealmException");
        } catch (DuplicateRealmException e) {
            // expected and correct
            assertEquals("dupe.realm", e.getId());
        }
    }

    @Test
    void testConfigure_EarlyImport() throws Exception {
        try {
            this.configurator.configure(getConfigPath("early-import.conf"));
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("Unhandled import"));
        }
    }

    @Test
    void testConfigure_RealmSyntax() throws Exception {
        try {
            this.configurator.configure(getConfigPath("realm-syntax.conf"));
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("Invalid realm"));
        }
    }

    @Test
    void testConfigure_Valid() throws Exception {
        this.configurator.configure(getConfigPath("valid.conf"));

        assertEquals("org.apache.maven.app.App", this.launcher.getMainClassName());

        assertEquals("maven", this.launcher.getMainRealmName());

        ClassWorld world = this.launcher.getWorld();

        Collection<ClassRealm> realms = world.getRealms();

        assertEquals(4, realms.size());

        assertNotNull(world.getRealm("ant"));
        assertNotNull(world.getRealm("maven"));
        assertNotNull(world.getRealm("xml"));

        ClassRealm antRealm = world.getRealm("ant");
        ClassRealm mavenRealm = world.getRealm("maven");
        ClassRealm xmlRealm = world.getRealm("xml");
        ClassRealm globRealm = world.getRealm("glob");

        assertSame(null, antRealm.getImportClassLoader("org.apache.tools.Ant"));

        // Ant has dependency to xerces:xercesImpl (test)
        assertSame(null, antRealm.getImportClassLoader("org.xml.sax.SAXException"));

        assertSame(xmlRealm, antRealm.getImportClassLoader("jakarta.xml.bind.JAXBException"));

        assertSame(null, mavenRealm.getImportClassLoader("org.apache.maven.app.App"));

        assertSame(xmlRealm, mavenRealm.getImportClassLoader("jakarta.xml.bind.JAXBException"));

        URL[] urls = globRealm.getURLs();

        String basedir = TestUtil.getBasedir();
        assertArrayContains(
                urls, new File(basedir, "src/test/test-data/nested.jar").toURI().toURL());
        assertArrayContains(
                urls, new File(basedir, "src/test/test-data/a.jar").toURI().toURL());
        assertArrayContains(
                urls, new File(basedir, "src/test/test-data/b.jar").toURI().toURL());
        assertArrayContains(
                urls, new File(basedir, "src/test/test-data/c.jar").toURI().toURL());
    }

    @Test
    void testConfigure_Optionally_NonExistent() throws Exception {
        this.configurator.configure(getConfigPath("optionally-nonexistent.conf"));

        assertEquals("org.apache.maven.app.App", this.launcher.getMainClassName());

        assertEquals("opt", this.launcher.getMainRealmName());

        ClassWorld world = this.launcher.getWorld();

        Collection<ClassRealm> realms = world.getRealms();

        assertEquals(1, realms.size());

        assertNotNull(world.getRealm("opt"));

        ClassRealm optRealm = world.getRealm("opt");

        URL[] urls = optRealm.getURLs();

        assertEquals(0, urls.length, "no urls");
    }

    @Test
    void testConfigure_Optionally_Existent() throws Exception {
        this.configurator.configure(getConfigPath("optionally-existent.conf"));

        assertEquals("org.apache.maven.app.App", this.launcher.getMainClassName());

        assertEquals("opt", this.launcher.getMainRealmName());

        ClassWorld world = this.launcher.getWorld();

        Collection<ClassRealm> realms = world.getRealms();

        assertEquals(1, realms.size());

        assertNotNull(world.getRealm("opt"));

        ClassRealm optRealm = world.getRealm("opt");

        URL[] urls = optRealm.getURLs();

        assertEquals(1, urls.length, "one url");

        assertSame(null, optRealm.getImportClassLoader("jakarta.xml.bind.JAXBException"));
    }

    @Test
    void testConfigure_Unhandled() throws Exception {
        try {
            this.configurator.configure(getConfigPath("unhandled.conf"));
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("Unhandled configuration"));
        }
    }

    @Test
    void testSet_Using_Existent() throws Exception {
        assertNull(System.getProperty("set.using.existent"));

        this.configurator.configure(getConfigPath("set-using-existent.conf"));

        assertEquals("testSet_Using_Existent", System.getProperty("set.using.existent"));
    }

    @Test
    void testSet_Using_NonExistent() throws Exception {
        assertNull(System.getProperty("set.using.nonexistent"));

        this.configurator.configure(getConfigPath("set-using-nonexistent.conf"));

        assertNull(System.getProperty("set.using.nonexistent"));
    }

    @Test
    void testSet_Using_NonExistent_Default() throws Exception {
        assertNull(System.getProperty("set.using.nonexistent.default"));

        this.configurator.configure(getConfigPath("set-using-nonexistent.conf"));

        assertEquals("testSet_Using_NonExistent_Default", System.getProperty("set.using.nonexistent.default"));
    }

    @Test
    void testSet_Using_NonExistent_Override() throws Exception {
        assertNull(System.getProperty("set.using.default"));
        System.setProperty("set.using.default", "testSet_Using_NonExistent_Override");

        this.configurator.configure(getConfigPath("set-using-nonexistent.conf"));

        assertEquals("testSet_Using_NonExistent_Override", System.getProperty("set.using.default"));
    }

    @Test
    void testSet_Using_Existent_Override() throws Exception {
        assertNull(System.getProperty("set.using.existent"));
        System.setProperty("set.using.existent", "testSet_Using_Existent_Override");

        this.configurator.configure(getConfigPath("set-using-existent.conf"));

        assertEquals("testSet_Using_Existent_Override", System.getProperty("set.using.existent"));
    }

    @Test
    void testSet_Using_Existent_Default() throws Exception {
        assertNull(System.getProperty("set.using.default"));

        this.configurator.configure(getConfigPath("set-using-existent.conf"));

        assertEquals("testSet_Using_Existent_Default", System.getProperty("set.using.default"));
    }

    @Test
    void testSet_Using_Missing_Default() throws Exception {
        assertNull(System.getProperty("set.using.missing"));

        this.configurator.configure(getConfigPath("set-using-missing.conf"));

        assertEquals("testSet_Using_Missing_Default", System.getProperty("set.using.missing"));
    }

    @Test
    void testSet_Using_Missing_Override() throws Exception {
        assertNull(System.getProperty("set.using.missing"));
        System.setProperty("set.using.missing", "testSet_Using_Missing_Override");

        this.configurator.configure(getConfigPath("set-using-missing.conf"));

        assertEquals("testSet_Using_Missing_Override", System.getProperty("set.using.missing"));
    }

    @Test
    void testSet_Using_Filtered_Default() throws Exception {
        assertNull(System.getProperty("set.using.filtered.default"));

        this.configurator.configure(getConfigPath("set-using-missing.conf"));

        assertEquals(System.getProperty("user.home") + "/m2", System.getProperty("set.using.filtered.default"));
    }

    private FileInputStream getConfigPath(String name) throws Exception {
        return new FileInputStream(new File(new File(TestUtil.getBasedir(), "src/test/test-data"), name));
    }

    private void assertArrayContains(URL[] array, URL url) {
        for (URL value : array) {
            if (url.equals(value)) {
                return;
            }
        }
        fail("URL (" + url + ") not found in array of URLs");
    }
}
