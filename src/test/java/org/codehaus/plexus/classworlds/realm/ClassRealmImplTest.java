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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClassRealmImplTest
    extends AbstractClassWorldsTestCase
{
    private ClassWorld world;

    @Before
    public void setUp()
    {
        this.world = new ClassWorld();
    }

    @After
    public void tearDown()
    {
        this.world = null;
    }

    @Test
    public void testNewRealm()
        throws Exception
    {
        ClassRealm realm = this.world.newRealm( "foo" );

        assertNotNull( realm );

        assertSame( this.world, realm.getWorld() );

        assertEquals( "foo", realm.getId() );
    }

    @Test
    public void testLocateSourceRealm_NoImports()
    {
        ClassRealm realm = new ClassRealm( this.world, "foo", null );

        assertSame( null, realm.getImportClassLoader( "com.werken.Stuff" ) );
    }

    @Test
    public void testLocateSourceRealm_SimpleImport()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        ClassRealm werkflowRealm = this.world.newRealm( "werkflow" );

        mainRealm.importFrom( "werkflow", "com.werken.werkflow" );

        assertSame( werkflowRealm, mainRealm.getImportClassLoader( "com.werken.werkflow.WerkflowEngine" ) );

        assertSame( werkflowRealm, mainRealm.getImportClassLoader( "com/werken/werkflow/some.properties" ) );

        assertSame( werkflowRealm, mainRealm.getImportClassLoader( "com.werken.werkflow.process.ProcessManager" ) );

        assertSame( null, mainRealm.getImportClassLoader( "com.werken.blissed.Process" ) );

        assertSame( null, mainRealm.getImportClassLoader( "java.lang.Object" ) );

        assertSame( null, mainRealm.getImportClassLoader( "NoviceProgrammerClass" ) );
    }

    @Test
    public void testLocateSourceRealm_MultipleImport()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        ClassRealm werkflowRealm = this.world.newRealm( "werkflow" );

        ClassRealm blissedRealm = this.world.newRealm( "blissed" );

        mainRealm.importFrom( "werkflow", "com.werken.werkflow" );

        mainRealm.importFrom( "blissed", "com.werken.blissed" );

        assertSame( werkflowRealm, mainRealm.getImportClassLoader( "com.werken.werkflow.WerkflowEngine" ) );

        assertSame( werkflowRealm, mainRealm.getImportClassLoader( "com.werken.werkflow.process.ProcessManager" ) );

        assertSame( blissedRealm, mainRealm.getImportClassLoader( "com.werken.blissed.Process" ) );

        assertSame( blissedRealm, mainRealm.getImportClassLoader( "com.werken.blissed.guard.BooleanGuard" ) );

        assertSame( null, mainRealm.getImportClassLoader( "java.lang.Object" ) );

        assertSame( null, mainRealm.getImportClassLoader( "NoviceProgrammerClass" ) );
    }

    @Test
    public void testLocateSourceRealm_Hierachy()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        ClassRealm fooRealm = this.world.newRealm( "foo" );

        ClassRealm fooBarRealm = this.world.newRealm( "fooBar" );

        ClassRealm fooBarBazRealm = this.world.newRealm( "fooBarBaz" );

        mainRealm.importFrom( "foo", "foo" );

        mainRealm.importFrom( "fooBar", "foo.bar" );

        mainRealm.importFrom( "fooBarBaz", "foo.bar.baz" );

        assertSame( fooRealm, mainRealm.getImportClassLoader( "foo.Goober" ) );

        assertSame( fooRealm, mainRealm.getImportClassLoader( "foo.cheese.Goober" ) );

        assertSame( fooBarRealm, mainRealm.getImportClassLoader( "foo.bar.Goober" ) );

        assertSame( fooBarRealm, mainRealm.getImportClassLoader( "foo.bar.cheese.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.getImportClassLoader( "foo.bar.baz.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.getImportClassLoader( "foo.bar.baz.cheese.Goober" ) );

        assertSame( null, mainRealm.getImportClassLoader( "java.lang.Object" ) );

        assertSame( null, mainRealm.getImportClassLoader( "NoviceProgrammerClass" ) );
    }

    @Test
    public void testLocateSourceRealm_Hierachy_Reverse()
        throws Exception
    {
        ClassRealm fooBarBazRealm = this.world.newRealm( "fooBarBaz" );

        ClassRealm fooBarRealm = this.world.newRealm( "fooBar" );

        ClassRealm fooRealm = this.world.newRealm( "foo" );

        ClassRealm mainRealm = this.world.newRealm( "main" );

        mainRealm.importFrom( "fooBarBaz", "foo.bar.baz" );

        mainRealm.importFrom( "fooBar", "foo.bar" );

        mainRealm.importFrom( "foo", "foo" );

        assertSame( fooRealm, mainRealm.getImportClassLoader( "foo.Goober" ) );

        assertSame( fooRealm, mainRealm.getImportClassLoader( "foo.cheese.Goober" ) );

        assertSame( fooBarRealm, mainRealm.getImportClassLoader( "foo.bar.Goober" ) );

        assertSame( fooBarRealm, mainRealm.getImportClassLoader( "foo.bar.cheese.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.getImportClassLoader( "foo.bar.baz.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.getImportClassLoader( "foo.bar.baz.cheese.Goober" ) );

        assertSame( null, mainRealm.getImportClassLoader( "java.lang.Object" ) );

        assertSame( null, mainRealm.getImportClassLoader( "NoviceProgrammerClass" ) );
    }

    @Test
    public void testLoadClass_SystemClass()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        Class<?> cls = mainRealm.loadClass( "java.lang.Object" );

        assertNotNull( cls );
    }

    @Test
    public void testLoadClass_NonSystemClass()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        try
        {
            Class<?> c = mainRealm.loadClass( "com.werken.projectz.UberThing" );

            System.out.println( "c = " + c );

            fail( "A ClassNotFoundException should be thrown!" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }
    }

    @Test
    public void testLoadClass_ClassWorldsClass()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        Class<?> cls = mainRealm.loadClass( "org.codehaus.plexus.classworlds.ClassWorld" );

        assertNotNull( cls );

        assertSame( ClassWorld.class, cls );
    }

    @Test
    public void testLoadClass_Local()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        try
        {
            mainRealm.loadClass( "a.A" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }

        mainRealm.addURL( getJarUrl( "a.jar" ) );

        Class<?> classA = mainRealm.loadClass( "a.A" );

        assertNotNull( classA );

        ClassRealm otherRealm = this.world.newRealm( "other" );

        try
        {
            otherRealm.loadClass( "a.A" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }
    }

    @Test
    public void testLoadClass_Imported()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        ClassRealm realmA = this.world.newRealm( "realmA" );

        try
        {
            realmA.loadClass( "a.A" );

            fail( "realmA.loadClass(a.A) should have thrown a ClassNotFoundException" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }

        realmA.addURL( getJarUrl( "a.jar" ) );

        try
        {
            mainRealm.loadClass( "a.A" );

            fail( "mainRealm.loadClass(a.A) should have thrown a ClassNotFoundException" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }

        mainRealm.importFrom( "realmA", "a" );

        Class<?> classA = realmA.loadClass( "a.A" );

        assertNotNull( classA );

        assertEquals( realmA, classA.getClassLoader() );

        Class<?> classMain = mainRealm.loadClass( "a.A" );

        assertNotNull( classMain );

        assertEquals( realmA, classMain.getClassLoader() );

        assertSame( classA, classMain );
    }

    @Test
    public void testLoadClass_Package()
        throws Exception
    {
        ClassRealm realmA = this.world.newRealm( "realmA" );
        realmA.addURL( getJarUrl( "a.jar" ) );

        Class<?> clazz = realmA.loadClass( "a.A" );
        assertNotNull( clazz );
        assertEquals( "a.A", clazz.getName() );

        Package p = clazz.getPackage();
        assertNotNull( p );
        assertEquals( "p.getName()", "a", p.getName() );
    }


    @Test
    public void testLoadClass_Complex()
        throws Exception
    {
        ClassRealm realmA = this.world.newRealm( "realmA" );
        ClassRealm realmB = this.world.newRealm( "realmB" );
        ClassRealm realmC = this.world.newRealm( "realmC" );

        realmA.addURL( getJarUrl( "a.jar" ) );
        realmB.addURL( getJarUrl( "b.jar" ) );
        realmC.addURL( getJarUrl( "c.jar" ) );

        realmC.importFrom( "realmA", "a" );

        realmC.importFrom( "realmB", "b" );

        realmA.importFrom( "realmC", "c" );

        Class<?> classA_A = realmA.loadClass( "a.A" );
        Class<?> classB_B = realmB.loadClass( "b.B" );
        Class<?> classC_C = realmC.loadClass( "c.C" );

        assertNotNull( classA_A );
        assertNotNull( classB_B );
        assertNotNull( classC_C );

        assertEquals( realmA, classA_A.getClassLoader() );

        assertEquals( realmB, classB_B.getClassLoader() );

        assertEquals( realmC, classC_C.getClassLoader() );

        // load from C

        Class<?> classA_C = realmC.loadClass( "a.A" );

        assertNotNull( classA_C );

        assertSame( classA_A, classA_C );

        assertEquals( realmA, classA_C.getClassLoader() );

        Class<?> classB_C = realmC.loadClass( "b.B" );

        assertNotNull( classB_C );

        assertSame( classB_B, classB_C );

        assertEquals( realmB, classB_C.getClassLoader() );

        // load from A

        Class<?> classC_A = realmA.loadClass( "c.C" );

        assertNotNull( classC_A );

        assertSame( classC_C, classC_A );

        assertEquals( realmC, classC_A.getClassLoader() );

        try
        {
            realmA.loadClass( "b.B" );
            fail( "throw ClassNotFoundException" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }

        // load from B

        try
        {
            realmB.loadClass( "a.A" );
            fail( "throw ClassNotFoundException" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }

        try
        {
            realmB.loadClass( "c.C" );
            fail( "throw ClassNotFoundException" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }
    }

    @Test
    public void testLoadClass_ClassWorldsClassRepeatedly()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        for ( int i = 0; i < 100; i++ )
        {
            Class<?> cls = mainRealm.loadClass( "org.codehaus.plexus.classworlds.ClassWorld" );

            assertNotNull( cls );

            assertSame( ClassWorld.class, cls );
        }
    }

    @Test
    public void testLoadClassWithModuleName_Java9()
    {
        final ExtendedClassRealm mainRealm = new ExtendedClassRealm( world );
        mainRealm.addURL( getJarUrl( "a.jar" ) );
        assertNotNull(mainRealm.simulateLoadClassFromModule( "a.A" ));
    }

    @Test
    public void testGetResources_BaseBeforeSelf()
        throws Exception
    {
        String resource = "common.properties";

        ClassRealm base = this.world.newRealm( "realmA" );
        base.addURL( getJarUrl( "a.jar" ) );

        URL baseUrl = base.getResource( resource );
        assertNotNull( baseUrl );

        ClassRealm sub = this.world.newRealm( "realmB", base );
        sub.addURL( getJarUrl( "b.jar" ) );

        URL subUrl = sub.getResource( resource );
        assertNotNull( subUrl );

        assertEquals( baseUrl, subUrl );

        List<String> urls = new ArrayList<>();
        for ( URL url : Collections.list( sub.getResources( resource ) ) )
        {
            String path = url.toString();
            path = path.substring( path.lastIndexOf( '/', path.lastIndexOf( ".jar!" ) ) );
            urls.add( path );
        }
        assertEquals( Arrays.asList( "/a.jar!/common.properties", "/b.jar!/common.properties" ), urls );
    }

    @Test
    public void testGetResources_SelfBeforeParent()
        throws Exception
    {
        String resource = "common.properties";

        ClassRealm parent = this.world.newRealm( "realmA" );
        parent.addURL( getJarUrl( "a.jar" ) );

        URL parentUrl = parent.getResource( resource );
        assertNotNull( parentUrl );

        ClassRealm child = parent.createChildRealm( "realmB" );
        child.addURL( getJarUrl( "b.jar" ) );

        URL childUrl = child.getResource( resource );
        assertNotNull( childUrl );

        List<URL> urls = Collections.list( child.getResources( resource ) );
        assertNotNull( urls );
        assertEquals( Arrays.asList( childUrl, parentUrl ), urls );
    }

    /**
     * Simulates new {@code java.lang.ClassLoader#findClass(String,String)} introduced with Java 9.
     * It is reversed in terms of inheritance but enables to simulate the same behavior in these tests.
     * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/ClassLoader.html#findClass(java.lang.String,java.lang.String)">ClassLoader#findClass(String,String)</a>
     */
    private static class ExtendedClassRealm extends ClassRealm
    {
        
        public ExtendedClassRealm(final ClassWorld world)
        {
            super( world, "java9", Thread.currentThread().getContextClassLoader() );
        }

        public Class<?> simulateLoadClassFromModule(final String name)
        {
            synchronized (getClassLoadingLock(name))
            {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    c = findClass(null, name);
                }
                return c;
            }
        }
    }
}
