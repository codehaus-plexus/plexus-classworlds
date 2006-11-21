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

public class ClassWorldTest
    extends AbstractClassWorldsTestCase
{
    private ClassWorld world;

    public ClassWorldTest( String name )
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

    public void testEmpty()
    {
        assertTrue( this.world.getRealms().isEmpty() );
    }

    public void testNewRealm()
        throws Exception
    {
        ClassRealm realm = this.world.newRealm( "foo" );

        assertNotNull( realm );
    }

    public void testGetRealm()
        throws Exception
    {
        ClassRealm realm = this.world.newRealm( "foo" );

        assertSame( realm, this.world.getRealm( "foo" ) );
    }

    public void testNewRealm_Duplicate()
        throws Exception
    {
        try
        {
            this.world.newRealm( "foo" );
            this.world.newRealm( "foo" );

            fail( "throw DuplicateRealmException" );
        }
        catch ( DuplicateRealmException e )
        {
            // expected and correct

            assertSame( this.world, e.getWorld() );

            assertEquals( "foo", e.getId() );
        }
    }

    public void testGetRealm_NoSuch()
        throws Exception
    {
        try
        {
            this.world.getRealm( "foo" );
            fail( "throw NoSuchRealmException" );
        }
        catch ( NoSuchRealmException e )
        {
            // expected and correct

            assertSame( this.world, e.getWorld() );

            assertEquals( "foo", e.getId() );
        }
    }

    public void testGetRealms()
        throws Exception
    {
        assertTrue( this.world.getRealms().isEmpty() );

        ClassRealm foo = this.world.newRealm( "foo" );

        assertEquals( 1, this.world.getRealms().size() );

        assertTrue( this.world.getRealms().contains( foo ) );

        ClassRealm bar = this.world.newRealm( "bar" );

        assertEquals( 2, this.world.getRealms().size() );

        assertTrue( this.world.getRealms().contains( bar ) );
    }
}
