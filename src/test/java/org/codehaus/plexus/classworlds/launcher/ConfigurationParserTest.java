package org.codehaus.plexus.classworlds.launcher;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.launcher.ConfigurationException;
import org.codehaus.plexus.classworlds.launcher.ConfigurationParser;

public class ConfigurationParserTest
    extends AbstractClassWorldsTestCase
{
    
    ConfigurationParser configurator = new ConfigurationParser( null, System.getProperties() );

    public ConfigurationParserTest( String string )
    {
        super( string );
    }

    public void testFilter_Unterminated()
        throws Exception
    {
        try
        {
            this.configurator.filter( "${cheese" );
            fail( "throw ConfigurationException" );
        }
        catch ( ConfigurationException e )
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "Unterminated" ) );
        }
    }

    public void testFilter_Solitary()
        throws Exception
    {
        System.setProperty( "classworlds.test.prop", "test prop value" );

        String result = this.configurator.filter( "${classworlds.test.prop}" );

        assertEquals( "test prop value", result );
    }

    public void testFilter_AtStart()
        throws Exception
    {
        System.setProperty( "classworlds.test.prop", "test prop value" );

        String result = this.configurator.filter( "${classworlds.test.prop}cheese" );

        assertEquals( "test prop valuecheese", result );
    }

    public void testFilter_AtEnd()
        throws Exception
    {
        System.setProperty( "classworlds.test.prop", "test prop value" );

        String result = this.configurator.filter( "cheese${classworlds.test.prop}" );

        assertEquals( "cheesetest prop value", result );
    }

    public void testFilter_Multiple()
        throws Exception
    {
        System.setProperty( "classworlds.test.prop.one", "test prop value one" );

        System.setProperty( "classworlds.test.prop.two", "test prop value two" );

        String result =
            this.configurator.filter( "I like ${classworlds.test.prop.one} and ${classworlds.test.prop.two} a lot" );

        assertEquals( "I like test prop value one and test prop value two a lot", result );
    }

    public void testFilter_NonExistent()
        throws Exception
    {
        try
        {
            this.configurator.filter( "${gollygeewillikers}" );
            fail( "throw ConfigurationException" );
        }
        catch ( ConfigurationException e )
        {
            // expected and correct
            assertTrue( e.getMessage().startsWith( "No such property" ) );
        }
    }

    public void testFilter_InMiddle()
        throws Exception
    {
        System.setProperty( "classworlds.test.prop", "test prop value" );

        String result = this.configurator.filter( "cheese${classworlds.test.prop}toast" );

        assertEquals( "cheesetest prop valuetoast", result );
    }

}
