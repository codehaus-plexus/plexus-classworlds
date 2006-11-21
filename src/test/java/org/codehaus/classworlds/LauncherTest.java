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
        String basedir = System.getProperty( "basedir" );

        /* do our best if we are not running from surefire */
        if ( basedir == null || basedir.equals( "" ) );
        {
            basedir = ( new File( "." ) ).getAbsolutePath();
        }
        return new FileInputStream( new File( new File( System.getProperty( "basedir" ), "src/test/resources/test-data" ), name ) );
    }
}
