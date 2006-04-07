package org.codehaus.classworlds;

/*
 $Id$

 Copyright 2002 (C) The Werken Company. All Rights Reserved.
 
 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.
 
 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.
 
 3. The name "classworlds" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Werken Company.  For written permission,
    please contact bob@werken.com.
 
 4. Products derived from this Software may not be called "classworlds"
    nor may "classworlds" appear in their names without prior written
    permission of The Werken Company. "classworlds" is a registered
    trademark of The Werken Company.
 
 5. Due credit should be given to The Werken Company.
    (http://classworlds.werken.com/).
 
 THIS SOFTWARE IS PROVIDED BY THE WERKEN COMPANY AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE WERKEN COMPANY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.
 
 */

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collection;

public class ConfiguratorTest extends TestCase
{
    private Launcher launcher;
    private Configurator configurator;

    public ConfiguratorTest(String name)
    {
        super( name );
    }

    public void setUp()
    {
        this.launcher = new Launcher();
        this.configurator = new Configurator( this.launcher );
    }

    public void tearDown()
    {
        this.launcher = null;
        this.configurator = null;
        System.getProperties().remove( "set.using.existent" );
        System.getProperties().remove( "set.using.default" );
        System.getProperties().remove( "set.using.nonexistent" );
        System.getProperties().remove( "set.using.nonexistent.default" );
	System.getProperties().remove( "set.using.missing" );
	System.getProperties().remove( "set.using.filtered.default" );
    }

    public void testConfigure_Nonexistent() throws Exception
    {
        try
        {
            this.configurator.configure( getConfigPath( "notfound.conf" ) );
            fail( "throw FileNotFoundException" );
        }
        catch (FileNotFoundException e)
        {
            // expected and correct
        }
    }

    public void testConfigure_DuplicateMain() throws Exception
    {
        try
        {
            this.configurator.configure( getConfigPath( "dupe-main.conf" ) );
            fail( "throw ConfigurationException" );
        }
        catch (ConfigurationException e)
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "Duplicate main" ) );
        }
    }

    public void testConfigure_DuplicateRealm() throws Exception
    {
        try
        {
            this.configurator.configure( getConfigPath( "dupe-realm.conf" ) );
            fail( "throw DuplicateRealmException" );
        }
        catch (DuplicateRealmException e)
        {
            // expected and correct
            assertEquals( "dupe.realm",
                          e.getId() );
        }
    }

    public void testConfigure_EarlyImport() throws Exception
    {
        try
        {
            this.configurator.configure( getConfigPath( "early-import.conf" ) );
            fail( "throw ConfigurationException" );
        }
        catch (ConfigurationException e)
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "Unhandled import" ) );
        }
    }

    public void testConfigure_RealmSyntax() throws Exception
    {
        try
        {
            this.configurator.configure( getConfigPath( "realm-syntax.conf" ) );
            fail( "throw ConfigurationException" );
        }
        catch (ConfigurationException e)
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "Invalid realm" ) );
        }
    }

    public void testConfigure_Valid() throws Exception
    {
        this.configurator.configure( getConfigPath( "valid.conf" ) );

        assertEquals( "org.apache.maven.app.App",
                      this.launcher.getMainClassName() );
        
        assertEquals( "maven",
                      this.launcher.getMainRealmName() );

        ClassWorld world = this.launcher.getWorld();

        Collection realms = world.getRealms();

        assertEquals( 4,
                      realms.size() );

        assertNotNull( world.getRealm( "ant" ) );
        assertNotNull( world.getRealm( "maven" ) );
        assertNotNull( world.getRealm( "xml" ) );

        ClassRealm antRealm = world.getRealm( "ant" );
        ClassRealm mavenRealm = world.getRealm( "maven" );
        ClassRealm xmlRealm = world.getRealm( "xml" );
        ClassRealm globRealm = world.getRealm( "glob" );

        assertSame( antRealm,
                    antRealm.locateSourceRealm( "org.apache.tools.Ant" ) );

        assertSame( xmlRealm,
                    antRealm.locateSourceRealm( "org.xml.sax.SAXException" ) );

        assertSame( mavenRealm,
                    mavenRealm.locateSourceRealm( "org.apache.maven.app.App" ) );

        assertSame( xmlRealm,
                    mavenRealm.locateSourceRealm( "org.xml.sax.SAXException" ) );
                    
        // Test the glob support
        RealmClassLoader cl = (RealmClassLoader) globRealm.getClassLoader();
        URL[] urls = cl.getURLs();
        
        assertArrayContains(urls, new File(System.getProperty("basedir") + "/target/test-classes/test-data/nested.jar").toURL());
        assertArrayContains(urls, new File(System.getProperty("basedir") + "/target/test-classes/test-data/a.jar").toURL());
        assertArrayContains(urls, new File(System.getProperty("basedir") + "/target/test-classes/test-data/b.jar").toURL());
        assertArrayContains(urls, new File(System.getProperty("basedir") + "/target/test-classes/test-data/c.jar").toURL());
    }

    public void testConfigure_Optionally_NonExistent() throws Exception
    {
        this.configurator.configure( getConfigPath( "optionally-nonexistent.conf" ) );

        assertEquals( "org.apache.maven.app.App",
                      this.launcher.getMainClassName() );
        
        assertEquals( "opt",
                      this.launcher.getMainRealmName() );

        ClassWorld world = this.launcher.getWorld();

        Collection realms = world.getRealms();

        assertEquals( 1,
                      realms.size() );

        assertNotNull( world.getRealm( "opt" ) );

        ClassRealm optRealm = world.getRealm( "opt" );

        RealmClassLoader cl = (RealmClassLoader) optRealm.getClassLoader();

        URL[] urls = cl.getURLs();

        assertEquals( "no urls",
                      0,
                      urls.length );
    }

    public void testConfigure_Optionally_Existent() throws Exception
    {
        this.configurator.configure( getConfigPath( "optionally-existent.conf" ) );

        assertEquals( "org.apache.maven.app.App",
                      this.launcher.getMainClassName() );
        
        assertEquals( "opt",
                      this.launcher.getMainRealmName() );

        ClassWorld world = this.launcher.getWorld();

        Collection realms = world.getRealms();

        assertEquals( 1,
                      realms.size() );

        assertNotNull( world.getRealm( "opt" ) );

        ClassRealm optRealm = world.getRealm( "opt" );

        RealmClassLoader cl = (RealmClassLoader) optRealm.getClassLoader();

        URL[] urls = cl.getURLs();

        assertEquals( "one url",
                      1,
                      urls.length );

        assertSame( optRealm,
                    optRealm.locateSourceRealm( "org.xml.sax.SAXException" ) );
    }

    public void testConfigure_Unhandled() throws Exception
    {
        try
        {
            this.configurator.configure( getConfigPath( "unhandled.conf" ) );
            fail( "throw ConfigurationException" );
        }
        catch (ConfigurationException e)
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "Unhandled configuration" ) );
        }
    }

    public void testFilter_Unterminated() throws Exception
    {
        try
        {
            this.configurator.filter( "${cheese" );
            fail( "throw ConfigurationException" );
        }
        catch (ConfigurationException e)
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "Unterminated" ) );
        }
    }

    public void testFilter_Solitary() throws Exception
    {
        System.setProperty( "classworlds.test.prop",
                            "test prop value" );

        String result = this.configurator.filter( "${classworlds.test.prop}" );

        assertEquals( "test prop value",
                      result );
    }

    public void testFilter_AtStart() throws Exception
    {
        System.setProperty( "classworlds.test.prop",
                            "test prop value" );

        String result = this.configurator.filter( "${classworlds.test.prop}cheese" );

        assertEquals( "test prop valuecheese",
                      result );
    }

    public void testFilter_AtEnd() throws Exception
    {
        System.setProperty( "classworlds.test.prop",
                            "test prop value" );

        String result = this.configurator.filter( "cheese${classworlds.test.prop}" );

        assertEquals( "cheesetest prop value",
                      result );
    }

    public void testFilter_Multiple() throws Exception
    {
        System.setProperty( "classworlds.test.prop.one",
                            "test prop value one" );

        System.setProperty( "classworlds.test.prop.two",
                            "test prop value two" );

        String result = this.configurator.filter( "I like ${classworlds.test.prop.one} and ${classworlds.test.prop.two} a lot" );

        assertEquals( "I like test prop value one and test prop value two a lot",
                      result );
    }

    public void testFilter_NonExistent() throws Exception
    {
        try
        {
            this.configurator.filter( "${gollygeewillikers}" );
            fail( "throw ConfigurationException" );
        }
        catch (ConfigurationException e)
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "No such property" ) );
        }
    }

    public void testFilter_InMiddle() throws Exception
    {
        System.setProperty( "classworlds.test.prop",
                            "test prop value" );

        String result = this.configurator.filter( "cheese${classworlds.test.prop}toast" );

        assertEquals( "cheesetest prop valuetoast",
                      result );
    }

    public void testSet_Using_Existent() throws Exception
    {
        assertNull( System.getProperty( "set.using.existent" ) );
        
        this.configurator.configure( getConfigPath( "set-using-existent.conf" ) );

        assertEquals( "testSet_Using_Existent", System.getProperty( "set.using.existent" ) );
    }

    public void testSet_Using_NonExistent() throws Exception
    {
        assertNull( System.getProperty( "set.using.nonexistent" ) );
        
        this.configurator.configure( getConfigPath( "set-using-nonexistent.conf" ) );

        assertNull( System.getProperty( "set.using.nonexistent" ) );
    }

    public void testSet_Using_NonExistent_Default() throws Exception
    {
        assertNull( System.getProperty( "set.using.nonexistent.default" ) );
        
        this.configurator.configure( getConfigPath( "set-using-nonexistent.conf" ) );

        assertEquals( "testSet_Using_NonExistent_Default", System.getProperty( "set.using.nonexistent.default" ) );
    }

    public void testSet_Using_NonExistent_Override() throws Exception
    {
        assertNull( System.getProperty( "set.using.default" ) );
        System.setProperty( "set.using.default", "testSet_Using_NonExistent_Override" );
        
        this.configurator.configure( getConfigPath( "set-using-nonexistent.conf" ) );

        assertEquals( "testSet_Using_NonExistent_Override", System.getProperty( "set.using.default" ) );
    }

    public void testSet_Using_Existent_Override() throws Exception
    {
        assertNull( System.getProperty( "set.using.existent" ) );
        System.setProperty( "set.using.existent", "testSet_Using_Existent_Override" );
        
        this.configurator.configure( getConfigPath( "set-using-existent.conf" ) );

        assertEquals( "testSet_Using_Existent_Override", System.getProperty( "set.using.existent" ) );
    }

    public void testSet_Using_Existent_Default() throws Exception
    {
        assertNull( System.getProperty( "set.using.default" ) );
        
        this.configurator.configure( getConfigPath( "set-using-existent.conf" ) );

        assertEquals( "testSet_Using_Existent_Default", System.getProperty( "set.using.default" ) );
    }

    public void testSet_Using_Missing_Default() throws Exception
    {
        assertNull( System.getProperty( "set.using.missing" ) );
        
        this.configurator.configure( getConfigPath( "set-using-missing.conf" ) );

        assertEquals( "testSet_Using_Missing_Default", System.getProperty( "set.using.missing" ) );
    }

    public void testSet_Using_Missing_Override() throws Exception
    {
        assertNull( System.getProperty( "set.using.missing" ) );
        System.setProperty( "set.using.missing", "testSet_Using_Missing_Override" );
        
        this.configurator.configure( getConfigPath( "set-using-missing.conf" ) );

        assertEquals( "testSet_Using_Missing_Override", System.getProperty( "set.using.missing" ) );
    }

    public void testSet_Using_Filtered_Default() throws Exception
    {
        assertNull( System.getProperty( "set.using.filtered.default" ) );
        
        this.configurator.configure( getConfigPath( "set-using-missing.conf" ) );

        assertEquals( System.getProperty( "user.home" ) + "/m2", System.getProperty( "set.using.filtered.default" ) );
    }

    private FileInputStream getConfigPath(String name)
        throws Exception
    {
        return new FileInputStream( new File( new File( System.getProperty( "basedir" ), "target/test-classes/test-data" ), name ) ) ;
    }

    private void assertArrayContains(URL[] array, URL url) throws Exception {
        for (int i = 0; i < array.length; ++i)
            if (url.equals(array[i]))
                return;
        fail("URL (" + url + ") not found in array of URLs");
    }
}
