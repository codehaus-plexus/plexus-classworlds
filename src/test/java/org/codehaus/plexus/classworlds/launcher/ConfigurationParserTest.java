package org.codehaus.plexus.classworlds.launcher;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ConfigurationParserTest extends AbstractClassWorldsTestCase {

    ConfigurationParser configurator = new ConfigurationParser(null, System.getProperties());

    @Test
    void testFilter_Unterminated() {
        try {
            this.configurator.filter("${cheese");
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("Unterminated"));
        }
    }

    @Test
    void testFilter_Solitary() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("${classworlds.test.prop}");

        assertEquals("test prop value", result);
    }

    @Test
    void testFilter_AtStart() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("${classworlds.test.prop}cheese");

        assertEquals("test prop valuecheese", result);
    }

    @Test
    void testFilter_AtEnd() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("cheese${classworlds.test.prop}");

        assertEquals("cheesetest prop value", result);
    }

    @Test
    void testFilter_Multiple() throws Exception {
        System.setProperty("classworlds.test.prop.one", "test prop value one");

        System.setProperty("classworlds.test.prop.two", "test prop value two");

        String result =
                this.configurator.filter("I like ${classworlds.test.prop.one} and ${classworlds.test.prop.two} a lot");

        assertEquals("I like test prop value one and test prop value two a lot", result);
    }

    @Test
    void testFilter_NonExistent() {
        try {
            this.configurator.filter("${gollygeewillikers}");
            fail("throw ConfigurationException");
        } catch (ConfigurationException e) {
            // expected and correct
            assertTrue(e.getMessage().startsWith("No such property"));
        }
    }

    @Test
    void testFilter_InMiddle() throws Exception {
        System.setProperty("classworlds.test.prop", "test prop value");

        String result = this.configurator.filter("cheese${classworlds.test.prop}toast");

        assertEquals("cheesetest prop valuetoast", result);
    }
}
