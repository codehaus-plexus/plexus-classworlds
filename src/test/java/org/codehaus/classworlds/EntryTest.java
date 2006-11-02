package org.codehaus.classworlds;

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
