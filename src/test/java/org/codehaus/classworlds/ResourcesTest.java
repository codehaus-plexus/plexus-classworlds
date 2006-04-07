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

public class ResourcesTest
    extends TestCase
{
    private Launcher launcher;

    private Configurator configurator;

    public ResourcesTest( String name )
    {
        super( name );
    }

    public void setUp()
    {
        this.launcher = new Launcher();

        this.configurator = new Configurator( this.launcher );
    }

    public void tearDown()
    {
        this.launcher = null;
        this.configurator = null;
    }

    public void testConfigure_Inheritance() throws Exception
    {
        this.configurator.configure( getConfigPath( "classworlds.conf" ) );

        ClassWorld classWorld = launcher.getWorld();

        ClassRealm rootMavenPlugin = classWorld.getRealm( "root" );

        assertNotNull( rootMavenPlugin.getResource( "org/codehaus/werkflow/personality/common/core-idioms.xml" ) );

        assertNotNull( rootMavenPlugin.getResource( "/org/codehaus/werkflow/personality/common/core-idioms.xml" ) );

        assertNotNull( "Double dot path element is not working!",
                       rootMavenPlugin.getResource( "org/codehaus/werkflow/personality/basic/../common/core-idioms.xml" ) );
    }

    private FileInputStream getConfigPath( String name )
        throws Exception
    {
        return new FileInputStream( new File( new File( System.getProperty( "basedir" ), "src/test/resources/test-data/resources" ), name ) );
    }
}
