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

/**
 * @author <a href="bwalding@jakarta.org">Ben Walding</a>
 * @version $Id$
 */
public class EntryTest extends TestCase
{

    /**
     * Constructor for EntryTest.
     *
     * @param name
     */
    public EntryTest( String name )
    {
        super( name );
    }

    public void testCompareTo() throws Exception
    {
        ClassWorld cw = new ClassWorld();
        DefaultClassRealm r = (DefaultClassRealm) cw.newRealm( "test1" );

        Entry entry1 = new Entry( r, "org.test" );
        Entry entry2 = new Entry( r, "org.test.impl" );

        assertTrue( "org.test > org.test.impl", entry1.compareTo( entry2 ) > 0 );
    }

    /**
     * Tests the equality is realm independant
     *
     * @throws Exception
     */
    public void testEquals() throws Exception
    {
        ClassWorld cw = new ClassWorld();
        DefaultClassRealm r1 = (DefaultClassRealm) cw.newRealm( "test1" );
        DefaultClassRealm r2 = (DefaultClassRealm) cw.newRealm( "test2" );

        Entry entry1 = new Entry( r1, "org.test" );
        Entry entry2 = new Entry( r2, "org.test" );

        assertTrue( "entry1 == entry2", entry1.equals( entry2 ) );
        assertTrue( "entry1.hashCode() == entry2.hashCode()", entry1.hashCode() == entry2.hashCode() );
    }


}
