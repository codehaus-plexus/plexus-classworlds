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
import java.io.FileInputStream;

public class LauncherTest
    extends TestCase
{
    private Launcher launcher;

    public LauncherTest( String name )
    {
        super( name );
    }

    public void setUp()
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.codehaus.classworlds.protocol" );

        this.launcher = new Launcher();
    }

    public void tearDown()
    {
        this.launcher = null;
    }

    public void testConfigure_Valid() throws Exception
    {
        launcher.configure( getConfigPath( "valid-launch.conf" ) );

        Class mainClass = launcher.getMainClass();

        assertNotNull( mainClass );

        assertEquals( "a.A", mainClass.getName() );

        assertEquals( "app", launcher.getMainRealm().getId() );
    }

    public void testLaunch_ValidStandard() throws Exception
    {
        launcher.configure( getConfigPath( "valid-launch.conf" ) );

        launcher.launch( new String[]{} );
    }

    public void testLaunch_ValidStandardExitCode() throws Exception
    {
        launcher.configure( getConfigPath( "valid-launch-exitCode.conf" ) );

        launcher.launch( new String[]{} );

        assertEquals( "check exit code", 15, launcher.getExitCode() );
    }

    public void testLaunch_ValidEnhanced() throws Exception
    {
        launcher.configure( getConfigPath( "valid-enh-launch.conf" ) );

        launcher.launch( new String[]{} );
    }

    public void testLaunch_ValidEnhancedExitCode() throws Exception
    {
        launcher.configure( getConfigPath( "valid-enh-launch-exitCode.conf" ) );

        launcher.launch( new String[]{} );

        assertEquals( "check exit code", 45, launcher.getExitCode() );
    }

    public void testLaunch_NoSuchMethod() throws Exception
    {
        launcher.configure( getConfigPath( "launch-nomethod.conf" ) );

        try
        {
            launcher.launch( new String[]{} );
            fail( "should have thrown NoSuchMethodException" );
        }
        catch ( NoSuchMethodException e )
        {
            // expected and correct
        }
    }

    public void testLaunch_ClassNotFound() throws Exception
    {
        launcher.configure( getConfigPath( "launch-noclass.conf" ) );

        try
        {
            launcher.launch( new String[]{} );
            fail( "throw ClassNotFoundException" );
        }
        catch ( ClassNotFoundException e )
        {
            // expected and correct
        }
    }

    private FileInputStream getConfigPath( String name )
        throws Exception
    {
        return new FileInputStream( new File( new File( System.getProperty( "basedir" ), "src/test/test-data" ), name ) );
    }
}
