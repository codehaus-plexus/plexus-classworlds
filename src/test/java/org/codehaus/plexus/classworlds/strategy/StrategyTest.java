package org.codehaus.plexus.classworlds.strategy;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.junit.Before;
import org.junit.Test;

// jars within jars
// hierarchy vs graph

public class StrategyTest
    extends AbstractClassWorldsTestCase
{
    private ClassWorld world;
    
    private ClassRealm realm;

    private Strategy strategy;

    @Before
    public void setUp()
        throws Exception
    {
        this.world = new ClassWorld();

        this.realm = this.world.newRealm( "realm" );

        this.strategy = this.realm.getStrategy();
        
        realm.addURL( getJarUrl( "component0-1.0.jar" ) );
    }

    @Test
    public void testLoadingOfApplicationClass()
        throws Exception
    {
        Class<?> c = strategy.loadClass( "org.codehaus.plexus.Component0" );

        assertNotNull( c );
    }

    @Test
    public void testLoadingOfApplicationClassThenDoingItAgain()
        throws Exception
    {
        Class<?> c = strategy.loadClass( "org.codehaus.plexus.Component0" );

        assertNotNull( c );

        c = strategy.loadClass( "org.codehaus.plexus.Component0" );

        assertNotNull( c );
    }


    @Test
    public void testLoadingOfSystemClass()
        throws Exception
    {
        Class<?> c = strategy.getRealm().loadClass( "java.lang.Object" );

        assertNotNull( c );
    }

    @Test
    public void testLoadingOfNonExistentClass()
        throws Exception
    {
        try
        {
            strategy.loadClass( "org.codehaus.plexus.NonExistentComponent" );

            fail( "Should have thrown a ClassNotFoundException!" );
        }
        catch ( ClassNotFoundException e )
        {
            // do nothing
        }
    }

    @Test
    public void testGetApplicationResource()
        throws Exception
    {
        URL resource = strategy.getResource( "META-INF/plexus/components.xml" );

        assertNotNull( resource );

        String content = getContent( resource.openStream() );

        assertTrue( content.startsWith( "<component-set>" ) );
    }

    @Test
    public void testGetSystemResource()
        throws Exception
    {
        assumeTrue( "Due to strong encapsulation you cannot get the java/lang/Object.class as resource since Java 9",
                    getJavaVersion() < 9.0 );
        
        URL resource = strategy.getRealm().getResource( "java/lang/Object.class" );

        assertNotNull( resource );
    }

    @Test
    public void testFindResources()
        throws Exception
    {
        realm.addURL( getJarUrl( "component1-1.0.jar" ) );

        Enumeration<URL> e = strategy.getResources( "META-INF/plexus/components.xml" );

        assertNotNull( e );

        int resourceCount = 0;

        for ( Enumeration<URL> resources = e; resources.hasMoreElements(); )
        {
            resources.nextElement();

            resourceCount++;
        }

        assertEquals( 2, resourceCount );
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
    
    private double getJavaVersion()
    {
        return Double.parseDouble( System.getProperty( "java.specification.version" ) );
    }
}
