package org.codehaus.plexus.classworlds.launcher;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LauncherTest
    extends AbstractClassWorldsTestCase
{
    private Launcher launcher;

    @BeforeEach
    public void setUp()
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.codehaus.classworlds.protocol" );

        this.launcher = new Launcher();

        this.launcher.setSystemClassLoader( Thread.currentThread().getContextClassLoader() );
    }

    @AfterEach
    public void tearDown()
    {
        this.launcher = null;
    }

    @Test
    void testConfigure_Valid()
        throws Exception
    {
        launcher.configure( getConfigPath( "valid-launch.conf" ) );

        Class<?> mainClass = launcher.getMainClass();

        assertNotNull( mainClass );

        assertEquals( "a.A", mainClass.getName() );

        assertEquals( "app", launcher.getMainRealm().getId() );
    }

    @Test
    void testLaunch_ValidStandard()
        throws Exception
    {
        launcher.configure( getConfigPath( "valid-launch.conf" ) );

        launcher.launch( new String[]{} );
    }

    @Test
    void testLaunch_ValidStandardExitCode()
        throws Exception
    {
        launcher.configure( getConfigPath( "valid-launch-exitCode.conf" ) );

        launcher.launch( new String[]{} );

        assertEquals( 15, launcher.getExitCode() , "check exit code");
    }

    @Test
    void testLaunch_ValidEnhanced()
        throws Exception
    {
        launcher.configure( getConfigPath( "valid-enh-launch.conf" ) );

        launcher.launch( new String[]{} );
    }

    @Test
    void testLaunch_ValidEnhancedExitCode()
        throws Exception
    {
        launcher.configure( getConfigPath( "valid-enh-launch-exitCode.conf" ) );

        launcher.launch( new String[]{} );

        assertEquals( 45, launcher.getExitCode() , "check exit code");
    }

    @Test
    void testLaunch_NoSuchMethod()
        throws Exception
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

    @Test
    void testLaunch_ClassNotFound()
        throws Exception
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
        String basedir = TestUtil.getBasedir();

        return new FileInputStream( new File( new File( basedir, "src/test/test-data" ), name ) );
    }
}
