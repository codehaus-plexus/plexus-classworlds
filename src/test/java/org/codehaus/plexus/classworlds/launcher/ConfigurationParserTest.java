package org.codehaus.plexus.classworlds.launcher;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ConfigurationParserTest extends AbstractClassWorldsTestCase {

    final ConfigurationParser configurator = new ConfigurationParser(null, System.getProperties());

    @Test
    void filterUnterminated() {
        try {
            this.configurator.filter("${cheese");
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("Unterminated"));
        }
    }

    @Test
    void filterSolitary() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("${classworlds.test.prop}");

        assertEquals("test prop value", result);
    }

    @Test
    void filterAtStart() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("${classworlds.test.prop}cheese");

        assertEquals("test prop valuecheese", result);
    }

    @Test
    void filterAtEnd() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("cheese${classworlds.test.prop}");

        assertEquals("cheesetest prop value", result);
    }

    @Test
    void filterMultiple() throws Exception {
        System.setProperty("classworlds.test.prop.one", "test prop value one");

        System.setProperty("classworlds.test.prop.two", "test prop value two");

        String result =
                this.configurator.filter("I like ${classworlds.test.prop.one} and ${classworlds.test.prop.two} a lot");

        assertEquals("I like test prop value one and test prop value two a lot", result);
    }

    @Test
    void filterNonExistent() {
        try {
            this.configurator.filter("${gollygeewillikers}");
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("No such property"));
        }
    }

    @Test
    void filterInMiddle() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("cheese${classworlds.test.prop}toast");

        assertEquals("cheesetest prop valuetoast", result);
    }

    @Test
    void loadGlobMatchesBothPrefixAndSuffix(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("maven-core.jar"));
        Files.createFile(tempDir.resolve("maven-model.jar"));
        Files.createFile(tempDir.resolve("sisu-plexus.jar"));
        Files.createFile(tempDir.resolve("guice.jar"));

        List<File> loaded = new ArrayList<>();
        ConfigurationHandler handler = new NoopConfigurationHandler() {
            @Override
            public void addLoadFile(File file) {
                loaded.add(file);
            }
        };
        ConfigurationParser parser = new ConfigurationParser(handler, System.getProperties());

        parser.loadGlob(new File(tempDir.toFile(), "maven-*.jar").toString(), false);

        assertEquals(
                2,
                loaded.size(),
                "glob 'maven-*.jar' should match only files starting with 'maven-' AND ending with '.jar'");
        List<String> names = loaded.stream().map(File::getName).sorted().collect(Collectors.toList());
        assertEquals(Arrays.asList("maven-core.jar", "maven-model.jar"), names);
    }

    private abstract static class NoopConfigurationHandler implements ConfigurationHandler {
        @Override
        public void setAppMain(String mainClassName, String mainRealmName) {}

        @Override
        public void addRealm(String realmName) {}

        @Override
        public void addImportFrom(String realmName, String importSpec) {}

        @Override
        public void addLoadFile(File file) {}

        @Override
        public void addLoadURL(java.net.URL url) {}
    }
}
