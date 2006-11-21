package org.codehaus.classworlds.realm;

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

import java.net.MalformedURLException;

import java.net.URL;

import org.codehaus.classworlds.realm.DefaultClassRealm;
import org.codehaus.classworlds.realm.ClassRealm;
import org.codehaus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.TestUtil;

public class ClassRealmImplTest
    extends AbstractClassWorldsTestCase
{
    private ClassWorld world;

    public ClassRealmImplTest( String name )
    {
        super( name );
    }

    public void setUp()
    {
        this.world = new ClassWorld();
    }

    public void tearDown()
    {
        this.world = null;
    }

    public void testNewRealm()
        throws Exception
    {
        ClassRealm realm = this.world.newRealm( "foo" );

        assertNotNull( realm );

        assertSame( this.world, realm.getWorld() );

        assertEquals( "foo", realm.getId() );
    }

    public void testLocateSourceRealm_NoImports()
        throws Exception
    {
        DefaultClassRealm realm = new DefaultClassRealm( this.world, "foo" );

        assertSame( realm, realm.locateSourceRealm( "com.werken.Stuff" ) );
    }

    public void testLocateSourceRealm_SimpleImport()
        throws Exception
    {
        DefaultClassRealm mainRealm = (DefaultClassRealm) this.world.newRealm( "main" );

        ClassRealm werkflowRealm = this.world.newRealm( "werkflow" );

        mainRealm.importFrom( "werkflow", "com.werken.werkflow" );

        assertSame( werkflowRealm, mainRealm.locateSourceRealm( "com.werken.werkflow.WerkflowEngine" ) );

        assertSame( werkflowRealm, mainRealm.locateSourceRealm( "com.werken.werkflow.process.ProcessManager" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "com.werken.blissed.Process" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "java.lang.Object" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "NoviceProgrammerClass" ) );
    }

    public void testLocateSourceRealm_MultipleImport()
        throws Exception
    {
        DefaultClassRealm mainRealm = (DefaultClassRealm) this.world.newRealm( "main" );

        ClassRealm werkflowRealm = this.world.newRealm( "werkflow" );

        ClassRealm blissedRealm = this.world.newRealm( "blissed" );

        mainRealm.importFrom( "werkflow", "com.werken.werkflow" );

        mainRealm.importFrom( "blissed", "com.werken.blissed" );

        assertSame( werkflowRealm, mainRealm.locateSourceRealm( "com.werken.werkflow.WerkflowEngine" ) );

        assertSame( werkflowRealm, mainRealm.locateSourceRealm( "com.werken.werkflow.process.ProcessManager" ) );

        assertSame( blissedRealm, mainRealm.locateSourceRealm( "com.werken.blissed.Process" ) );

        assertSame( blissedRealm, mainRealm.locateSourceRealm( "com.werken.blissed.guard.BooleanGuard" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "java.lang.Object" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "NoviceProgrammerClass" ) );
    }

    public void testLocateSourceRealm_Hierachy()
        throws Exception
    {
        DefaultClassRealm mainRealm = (DefaultClassRealm) this.world.newRealm( "main" );

        ClassRealm fooRealm = this.world.newRealm( "foo" );

        ClassRealm fooBarRealm = this.world.newRealm( "fooBar" );

        ClassRealm fooBarBazRealm = this.world.newRealm( "fooBarBaz" );

        mainRealm.importFrom( "foo", "foo" );

        mainRealm.importFrom( "fooBar", "foo.bar" );

        mainRealm.importFrom( "fooBarBaz", "foo.bar.baz" );

        assertSame( fooRealm, mainRealm.locateSourceRealm( "foo.Goober" ) );

        assertSame( fooRealm, mainRealm.locateSourceRealm( "foo.cheese.Goober" ) );

        assertSame( fooBarRealm, mainRealm.locateSourceRealm( "foo.bar.Goober" ) );

        assertSame( fooBarRealm, mainRealm.locateSourceRealm( "foo.bar.cheese.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.locateSourceRealm( "foo.bar.baz.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.locateSourceRealm( "foo.bar.baz.cheese.Goober" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "java.lang.Object" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "NoviceProgrammerClass" ) );
    }

    public void testLocateSourceRealm_Hierachy_Reverse()
        throws Exception
    {
        ClassRealm fooBarBazRealm = this.world.newRealm( "fooBarBaz" );

        ClassRealm fooBarRealm = this.world.newRealm( "fooBar" );

        ClassRealm fooRealm = this.world.newRealm( "foo" );

        DefaultClassRealm mainRealm = (DefaultClassRealm) this.world.newRealm( "main" );

        mainRealm.importFrom( "fooBarBaz", "foo.bar.baz" );

        mainRealm.importFrom( "fooBar", "foo.bar" );

        mainRealm.importFrom( "foo", "foo" );

        assertSame( fooRealm, mainRealm.locateSourceRealm( "foo.Goober" ) );

        assertSame( fooRealm, mainRealm.locateSourceRealm( "foo.cheese.Goober" ) );

        assertSame( fooBarRealm, mainRealm.locateSourceRealm( "foo.bar.Goober" ) );

        assertSame( fooBarRealm, mainRealm.locateSourceRealm( "foo.bar.cheese.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.locateSourceRealm( "foo.bar.baz.Goober" ) );

        assertSame( fooBarBazRealm, mainRealm.locateSourceRealm( "foo.bar.baz.cheese.Goober" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "java.lang.Object" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "NoviceProgrammerClass" ) );
    }

    public void testLoadClass_SystemClass()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        Class cls = mainRealm.loadClass( "java.lang.Object" );

        assertNotNull( cls );
    }

    public void testLoadClass_NonSystemClass()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        try
        {
            Class c = mainRealm.loadClass( "com.werken.projectz.UberThing" );

            System.out.println( "c = " + c );

            fail( "A ClassNotFoundException should be thrown!" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }
    }

    public void testLoadClass_ClassWorldsClass()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        Class cls = mainRealm.loadClass( "org.codehaus.classworlds.ClassWorld" );

        assertNotNull( cls );

        assertSame( ClassWorld.class, cls );
    }

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

        Class classA = mainRealm.loadClass( "a.A" );

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

        Class classA = realmA.loadClass( "a.A" );

        assertNotNull( classA );

        assertEquals( realmA.getStrategy(), classA.getClassLoader() );

        Class classMain = mainRealm.loadClass( "a.A" );

        assertNotNull( classMain );

        assertEquals( realmA.getStrategy(), classMain.getClassLoader() );

        assertSame( classA, classMain );
    }

    public void testLoadClass_Package()
        throws Exception
    {
        ClassRealm realmA = this.world.newRealm( "realmA" );
        realmA.addURL( getJarUrl( "a.jar" ) );

        Class clazz = realmA.loadClass( "a.A" );
        assertNotNull( clazz );
        assertEquals( "a.A", clazz.getName() );

        Package p = clazz.getPackage();
        assertNotNull( p );
        assertEquals( "p.getName()", "a", p.getName() );
    }


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

        Class classA_A = realmA.loadClass( "a.A" );
        Class classB_B = realmB.loadClass( "b.B" );
        Class classC_C = realmC.loadClass( "c.C" );

        assertNotNull( classA_A );
        assertNotNull( classB_B );
        assertNotNull( classC_C );

        assertEquals( realmA.getStrategy(), classA_A.getClassLoader() );

        assertEquals( realmB.getStrategy(), classB_B.getClassLoader() );

        assertEquals( realmC.getStrategy(), classC_C.getClassLoader() );

        // load from C

        Class classA_C = realmC.loadClass( "a.A" );

        assertNotNull( classA_C );

        assertSame( classA_A, classA_C );

        assertEquals( realmA.getStrategy(), classA_C.getClassLoader() );

        Class classB_C = realmC.loadClass( "b.B" );

        assertNotNull( classB_C );

        assertSame( classB_B, classB_C );

        assertEquals( realmB.getStrategy(), classB_C.getClassLoader() );

        // load from A

        Class classC_A = realmA.loadClass( "c.C" );

        assertNotNull( classC_A );

        assertSame( classC_C, classC_A );

        assertEquals( realmC.getStrategy(), classC_A.getClassLoader() );

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

    protected URL getJarUrl( String jarName )
        throws MalformedURLException
    {
        return TestUtil.getTestResourceUrl( jarName );
    }

    public void testLoadClass_ClassWorldsClassRepeatedly()
        throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        for ( int i = 0; i < 100; i++ )
        {
            Class cls = mainRealm.loadClass( "org.codehaus.classworlds.ClassWorld" );

            assertNotNull( cls );

            assertSame( ClassWorld.class, cls );
        }
    }
}
