package org.codehaus.classworlds;

import junit.framework.TestCase;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

// jars within jars
// hierarchy vs graph

public class RealmClassLoaderTest
    extends TestCase
{
    private ClassWorld world;
    
    private ClassRealm realm;

    private RealmClassLoader classLoader;

    public void setUp()
        throws Exception
    {
        this.world = new ClassWorld();

        this.realm = this.world.newRealm( "realm" );

        this.classLoader = (RealmClassLoader) this.realm.getClassLoader();
        
        classLoader.addURL( getJarUrl( "component0-1.0.jar" ) );
    }

    public void testLoadingOfApplicationClass()
        throws Exception
    {
        Class c = classLoader.loadClass( "org.codehaus.plexus.Component0" );

        assertNotNull( c );
    }

    public void testLoadingOfApplicationClassThenDoingItAgain()
        throws Exception
    {
        Class c;

        c = classLoader.loadClass( "org.codehaus.plexus.Component0" );

        assertNotNull( c );

        c = classLoader.loadClass( "org.codehaus.plexus.Component0" );

        assertNotNull( c );
    }


    public void testLoadingOfSystemClass()
        throws Exception
    {
        Class c = classLoader.loadClass( "java.lang.Object" );

        assertNotNull( c );
    }

    public void testLoadingOfNonExistentClass()
        throws Exception
    {
        try
        {
            classLoader.loadClass( "org.codehaus.plexus.NonExistentComponent" );

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
        URL resource = classLoader.getResource( "META-INF/plexus/components.xml" );

        assertNotNull( resource );

        String content = getContent( resource.openStream() );

        assertTrue( content.startsWith( "<component-set>" ) );
    }

    public void testGetSystemResource()
        throws Exception
    {
        URL resource = classLoader.getResource( "java/lang/Object.class" );

        assertNotNull( resource );
    }


    public void testGetResources()
        throws Exception
    {
        classLoader.addURL( getJarUrl( "component1-1.0.jar" ) );

        Enumeration e = classLoader.getResources( "META-INF/plexus/components.xml" );

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
        InputStream is = classLoader.getResourceAsStream( "META-INF/plexus/components.xml" );

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
