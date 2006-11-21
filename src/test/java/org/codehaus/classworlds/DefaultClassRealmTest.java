package org.codehaus.classworlds;

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

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

public class DefaultClassRealmTest
    extends AbstractClassWorldsTestCase
{
    public DefaultClassRealmTest( String name )
    {
        super( name );
    }

    // ----------------------------------------------------------------------
    // Class testing
    // ----------------------------------------------------------------------

    public void testLoadClassFromRealm()
        throws Exception
    {
        DefaultClassRealm mainRealm = new DefaultClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        mainRealm.loadClass( "org.codehaus.plexus.Component0" );
    }

    public void testLoadClassFromChildRealmWhereClassIsLocatedInParentRealm()
        throws Exception
    {
        DefaultClassRealm mainRealm = new DefaultClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        childRealm.loadClass( "org.codehaus.plexus.Component0" );
    }

    public void testLoadClassFromChildRealmWhereClassIsLocatedInGrantParentRealm()
        throws Exception
    {
        DefaultClassRealm mainRealm = new DefaultClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        ClassRealm grandchildRealm = childRealm.createChildRealm( "grandchild" );

        grandchildRealm.loadClass( "org.codehaus.plexus.Component0" );
    }

    public void testLoadNonExistentClass()
        throws Exception
    {
        DefaultClassRealm mainRealm = new DefaultClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        try
        {
            mainRealm.loadClass( "org.foo.bar.NonExistentClass" );

            fail( "A ClassNotFoundException should have been thrown!" );
        }
        catch ( ClassNotFoundException e )
        {
        }
    }

    public void testImport()
        throws Exception
    {
        ClassWorld world = new ClassWorld();

        ClassRealm r0 = world.newRealm( "r0" );

        ClassRealm r1 = world.newRealm( "r1" );

        r0.addURL( getJarUrl( "component0-1.0.jar" ) );

        r1.importFrom( "r0", "org.codehaus.plexus" );

        r1.loadClass( "org.codehaus.plexus.Component0" );
    }

    // ----------------------------------------------------------------------
    // Resource testing
    // ----------------------------------------------------------------------

    public void testResource()
        throws Exception
    {
        DefaultClassRealm mainRealm = new DefaultClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        URL resource = mainRealm.getResource( "META-INF/plexus/components.xml" );

        assertNotNull( resource );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected URL getJarUrl( String jarName )
        throws Exception
    {
        File jarFile = new File( System.getProperty( "basedir" ), "src/test-jars/" + jarName );

        return jarFile.toURL();
    }
}
