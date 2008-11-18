package org.codehaus.plexus.classworlds.realm;

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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;

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
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        loadClass( mainRealm, "org.codehaus.plexus.Component0" );
    }

    public void testLoadClassFromChildRealmWhereClassIsLocatedInParentRealm()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        loadClass( childRealm, "org.codehaus.plexus.Component0" );
    }

    public void testLoadClassFromChildRealmWhereClassIsLocatedInGrantParentRealm()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        ClassRealm grandchildRealm = childRealm.createChildRealm( "grandchild" );

        loadClass( grandchildRealm, "org.codehaus.plexus.Component0" );
    }

    public void testLoadNonExistentClass()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        try
        {
            mainRealm.loadClass( "org.foo.bar.NonExistentClass" );

            fail( "A ClassNotFoundException should have been thrown!" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected
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

        loadClass( r1, "org.codehaus.plexus.Component0" );
    }

    // ----------------------------------------------------------------------
    // Resource testing
    // ----------------------------------------------------------------------

    public void testResource()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main" );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        getResource( mainRealm, "META-INF/plexus/components.xml" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected URL getJarUrl( String jarName )
        throws Exception
    {
        File jarFile = new File( System.getProperty( "basedir" ), "src/test-jars/" + jarName );

        return jarFile.toURI().toURL();
    }

    private void loadClass( ClassRealm realm, String name )
        throws Exception
    {
        /*
         * NOTE: Load the class both directly from the realm and indirectly from an (ordinary) child class loader which
         * uses the specified class realm for parent delegation. The child class loader itself has no additional class
         * path entries but relies entirely on the provided class realm. Hence, the created child class loader should in
         * theory be able to load exactly the same classes/resources as the underlying class realm. In practice, it will
         * test that class realms properly integrate into the standard Java class loader hierarchy.
         */
        ClassLoader childLoader = new URLClassLoader( new URL[0], realm );
        assertEquals( realm.loadClass( name ), childLoader.loadClass( name ) );
    }

    private void getResource( ClassRealm realm, String name )
        throws Exception
    {
        ClassLoader childLoader = new URLClassLoader( new URL[0], realm );
        assertNotNull( realm.getResource( name ) );
        assertEquals( realm.getResource( name ), childLoader.getResource( name ) );
        assertEquals( Collections.list( realm.getResources( name ) ),
                      Collections.list( childLoader.getResources( name ) ) );
    }

}
