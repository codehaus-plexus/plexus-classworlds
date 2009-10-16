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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

import org.codehaus.classworlds.ClassRealmAdapter;
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
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        loadClass( mainRealm, "org.codehaus.plexus.Component0" );
    }

    public void testLoadClassFromChildRealmWhereClassIsLocatedInParentRealm()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        loadClass( childRealm, "org.codehaus.plexus.Component0" );
    }

    public void testLoadClassFromChildRealmWhereClassIsLocatedInGrantParentRealm()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        ClassRealm grandchildRealm = childRealm.createChildRealm( "grandchild" );

        loadClass( grandchildRealm, "org.codehaus.plexus.Component0" );
    }

    public void testLoadClassFromChildRealmWhereClassIsLocatedInBothChildRealmAndParentRealm()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "parent", null );

        mainRealm.addURL( getJarUrl( "component5-1.0.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        childRealm.addURL( getJarUrl( "component5-2.0.jar" ) );

        Class cls = loadClass( childRealm, "test.Component5" );

        assertSame( childRealm, cls.getClassLoader() );
        assertEquals( 1, cls.getMethods().length );
        assertEquals( "printNew", cls.getMethods()[0].getName() );
    }

    public void testLoadNonExistentClass()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

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

    public void testParentImport()
        throws Exception
    {
        ClassWorld world = new ClassWorld();

        ClassRealm parent = world.newRealm( "parent" );

        ClassRealm child = world.newRealm( "child" );

        parent.addURL( getJarUrl( "component0-1.0.jar" ) );

        child.setParentRealm( parent );

        Class type = loadClass( child, "org.codehaus.plexus.Component0" );

        child.importFromParent( "non-existing" );

        assertSame( null, loadClassOrNull( child, "org.codehaus.plexus.Component0" ) );

        child.importFromParent( "org.codehaus.plexus" );

        assertSame( type, loadClass( child, "org.codehaus.plexus.Component0" ) );
    }

    public void testLoadClassFromBaseClassLoaderBeforeSelf()
        throws Exception
    {
        ClassWorld world = new ClassWorld();

        ClassRealm base = world.newRealm( "base" );

        base.addURL( getJarUrl( "a.jar" ) );

        ClassRealm child = world.newRealm( "child", base );

        child.addURL( getJarUrl( "a.jar" ) );

        Class baseClass = loadClass( base, "a.A" );
        Class childClass = loadClass( child, "a.A" );

        assertSame( base, baseClass.getClassLoader() );
        assertSame( base, childClass.getClassLoader() );
        assertSame( baseClass, childClass );
    }

    // ----------------------------------------------------------------------
    // Resource testing
    // ----------------------------------------------------------------------

    public void testResource()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

        mainRealm.addURL( getJarUrl( "component0-1.0.jar" ) );

        getResource( mainRealm, "META-INF/plexus/components.xml" );
    }

    public void testMalformedResource()
        throws Exception
    {
        URL jarUrl = getJarUrl( "component0-1.0.jar" );

        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

        mainRealm.addURL( jarUrl );

        ClassLoader officialClassLoader = new URLClassLoader( new URL[] { jarUrl } );

        String resource = "META-INF/plexus/components.xml";

        assertNotNull( mainRealm.getResource( resource ) );
        assertNotNull( officialClassLoader.getResource( resource ) );

        /*
         * NOTE: Resource names with a leading slash are invalid when passed to a class loader and must not be found!
         * One can use a leading slash in Class.getResource() but not in ClassLoader.getResource().
         */

        assertSame( null, mainRealm.getResource( "/" + resource ) );
        assertSame( null, officialClassLoader.getResource( "/" + resource ) );

        /*
         * For backward-compat, legacy class realms have to support leading slashes.
         */

        org.codehaus.classworlds.ClassRealm legacyRealm = ClassRealmAdapter.getInstance( mainRealm );
        assertNotNull( legacyRealm.getResource( "/" + resource ) );
        assertNotNull( legacyRealm.getResourceAsStream( "/" + resource ) );
        assertTrue( legacyRealm.findResources( "/" + resource ).hasMoreElements() );
    }

    public void testFindResourceOnlyScansSelf()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

        mainRealm.addURL( getJarUrl( "a.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        childRealm.addURL( getJarUrl( "b.jar" ) );

        assertNotNull( childRealm.getResource( "a.properties" ) );
        assertNotNull( childRealm.getResource( "b.properties" ) );

        assertNull( childRealm.findResource( "a.properties" ) );

        assertNotNull( childRealm.findResource( "b.properties" ) );
    }

    public void testFindResourcesOnlyScansSelf()
        throws Exception
    {
        ClassRealm mainRealm = new ClassRealm( new ClassWorld(), "main", null );

        mainRealm.addURL( getJarUrl( "a.jar" ) );

        ClassRealm childRealm = mainRealm.createChildRealm( "child" );

        childRealm.addURL( getJarUrl( "b.jar" ) );

        assertTrue( childRealm.getResources( "a.properties" ).hasMoreElements() );
        assertTrue( childRealm.getResources( "b.properties" ).hasMoreElements() );

        assertFalse( childRealm.findResources( "a.properties" ).hasMoreElements() );

        assertTrue( childRealm.findResources( "b.properties" ).hasMoreElements() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private Class loadClassOrNull( ClassRealm realm, String name )
    {
        try
        {
            return loadClass( realm, name );
        }
        catch ( ClassNotFoundException e )
        {
            return null;
        }
    }

    private Class loadClass( ClassRealm realm, String name )
        throws ClassNotFoundException
    {
        Class cls = realm.loadClass( name );

        /*
         * NOTE: Load the class both directly from the realm and indirectly from an (ordinary) child class loader which
         * uses the specified class realm for parent delegation. The child class loader itself has no additional class
         * path entries but relies entirely on the provided class realm. Hence, the created child class loader should in
         * theory be able to load exactly the same classes/resources as the underlying class realm. In practice, it will
         * test that class realms properly integrate into the standard Java class loader hierarchy.
         */
        ClassLoader childLoader = new URLClassLoader( new URL[0], realm );
        assertEquals( cls, childLoader.loadClass( name ) );

        return cls;
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
