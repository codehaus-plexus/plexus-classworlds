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

import java.io.File;
import java.net.URL;

public class DefaultClassRealmTest
    extends TestCase
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
