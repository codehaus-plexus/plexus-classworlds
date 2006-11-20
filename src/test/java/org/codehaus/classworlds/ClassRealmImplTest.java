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

import java.net.MalformedURLException;

import java.net.URL;

public class ClassRealmImplTest
    extends TestCase
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

    public void testLocateSourceRealm_Hierachy() throws Exception
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

        assertSame( fooBarBazRealm,
                    mainRealm.locateSourceRealm( "foo.bar.baz.cheese.Goober" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "java.lang.Object" ) );

        assertSame( mainRealm, mainRealm.locateSourceRealm( "NoviceProgrammerClass" ) );
    }

    public void testLocateSourceRealm_Hierachy_Reverse() throws Exception
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

    public void testLoadClass_SystemClass() throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        Class cls = mainRealm.loadClass( "java.lang.Object" );

        assertNotNull( cls );
    }

    public void testLoadClass_NonSystemClass() throws Exception
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

    public void testLoadClass_ClassWorldsClass() throws Exception
    {
        ClassRealm mainRealm = this.world.newRealm( "main" );

        Class cls = mainRealm.loadClass( "org.codehaus.classworlds.ClassWorld" );

        assertNotNull( cls );

        assertSame( ClassWorld.class, cls );
    }

    public void testLoadClass_Local() throws Exception
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

    public void testLoadClass_Imported() throws Exception
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

    public void testLoadClass_Package() throws Exception
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


    public void testLoadClass_Complex() throws Exception
    {
        ClassRealm realmA = this.world.newRealm( "realmA" );
        ClassRealm realmB = this.world.newRealm( "realmB" );
        ClassRealm realmC = this.world.newRealm( "realmC" );

        realmA.addURL( getJarUrl( "a.jar" ) );
        realmB.addURL( getJarUrl( "b.jar" ) );
        realmC.addURL( getJarUrl( "c.jar" ) );

        realmC.importFrom( "realmA",
                           "a" );

        realmC.importFrom( "realmB",
                           "b" );

        realmA.importFrom( "realmC",
                           "c" );

        Class classA_A = realmA.loadClass( "a.A" );
        Class classB_B = realmB.loadClass( "b.B" );
        Class classC_C = realmC.loadClass( "c.C" );

        assertNotNull( classA_A );
        assertNotNull( classB_B );
        assertNotNull( classC_C );

        assertEquals( realmA.getStrategy(),
                      classA_A.getClassLoader() );

        assertEquals( realmB.getStrategy(),
                      classB_B.getClassLoader() );

        assertEquals( realmC.getStrategy(),
                      classC_C.getClassLoader() );

        // load from C

        Class classA_C = realmC.loadClass( "a.A" );

        assertNotNull( classA_C );

        assertSame( classA_A,
                    classA_C );

        assertEquals( realmA.getStrategy(),
                      classA_C.getClassLoader() );

        Class classB_C = realmC.loadClass( "b.B" );

        assertNotNull( classB_C );

        assertSame( classB_B,
                    classB_C );

        assertEquals( realmB.getStrategy(),
                      classB_C.getClassLoader() );

        // load from A

        Class classC_A = realmA.loadClass( "c.C" );

        assertNotNull( classC_A );

        assertSame( classC_C,
                    classC_A );

        assertEquals( realmC.getStrategy(),
                      classC_A.getClassLoader() );

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

    protected URL getJarUrl( String jarName ) throws MalformedURLException
    {
        return TestUtil.getTestResourceUrl( jarName );
    }

    public void testLoadClass_ClassWorldsClassRepeatedly() throws Exception
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
