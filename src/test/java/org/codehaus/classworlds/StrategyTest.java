package org.codehaus.classworlds;

import junit.framework.TestCase;
import org.codehaus.classworlds.strategy.Strategy;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

// jars within jars
// hierarchy vs graph

public class StrategyTest
        extends TestCase
{
    private ClassWorld world;
    
    private ClassRealm realm;

    private Strategy strategy;

    public void setUp()
        throws Exception
    {
        this.world = new ClassWorld();

        this.realm = this.world.newRealm( "realm" );

        this.strategy = this.realm.getStrategy();
        
        strategy.addURL( getJarUrl( "component0-1.0.jar" ) );
    }

    public void testLoadingOfApplicationClass()
        throws Exception
    {
        Class c = strategy.loadClass( realm, "org.codehaus.plexus.Component0" );

        assertNotNull( c );
    }

    public void testLoadingOfApplicationClassThenDoingItAgain()
        throws Exception
    {
        Class c;

        c = strategy.loadClass( realm, "org.codehaus.plexus.Component0" );

        assertNotNull( c );

        c = strategy.loadClass( realm, "org.codehaus.plexus.Component0" );

        assertNotNull( c );
    }


    public void testLoadingOfSystemClass()
        throws Exception
    {
        Class c = strategy.loadClass( realm, "java.lang.Object" );

        assertNotNull( c );
    }

    public void testLoadingOfNonExistentClass()
        throws Exception
    {
        try
        {
            strategy.loadClass( realm, "org.codehaus.plexus.NonExistentComponent" );

            fail( "Should have thrown a ClassNotFoundException!" );
        }
        catch ( ClassNotFoundException e )
        {
            // do nothing
        }
    }

    public void testGetApplicationResource()
        throws Exception
    {
        URL resource = strategy.getResource( realm, "META-INF/plexus/components.xml" );

        assertNotNull( resource );

        String content = getContent( resource.openStream() );

        assertTrue( content.startsWith( "<component-set>" ) );
    }

    public void testGetSystemResource()
        throws Exception
    {
        URL resource = strategy.getResource( realm, "java/lang/Object.class" );

        assertNotNull( resource );
    }


    public void testGetResources()
        throws Exception
    {
        strategy.addURL( getJarUrl( "component1-1.0.jar" ) );

        Enumeration e = strategy.getResources( realm, "META-INF/plexus/components.xml" );

        assertNotNull( e );

        int resourceCount = 0;

        for ( Enumeration resources = e; resources.hasMoreElements(); )
        {
            resources.nextElement();

            resourceCount++;
        }

        assertEquals( 2, resourceCount );
    }


    public void testGetResourceAsStream()
        throws Exception
    {
        InputStream is = strategy.getResourceAsStream( realm, "META-INF/plexus/components.xml" );

        assertNotNull( is );

        String content = getContent( is );

        assertTrue( content.startsWith( "<component-set>" ) );
    }


    protected URL getJarUrl( String jarName )
        throws Exception
    {
        File jarFile = new File( System.getProperty( "basedir" ), "src/test-jars/" + jarName );

        return jarFile.toURL();
    }

    protected String getContent( InputStream in )
        throws Exception
    {
        byte[] buffer = new byte[1024];

        int read = 0;

        StringBuffer content = new StringBuffer();

        while ( ( read = in.read( buffer, 0, 1024 ) ) >= 0 )
        {
            content.append( new String( buffer, 0, read ) );
        }

        return content.toString();
    }
}
