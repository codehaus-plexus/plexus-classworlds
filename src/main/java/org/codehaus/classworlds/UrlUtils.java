package org.codehaus.classworlds;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class UrlUtils
{
    public static String normalizeUrlPath( String name )
    {
        if ( name.startsWith( "/" ) )
        {
            name = name.substring( 1 );
        }

        // Looking for org/codehaus/werkflow/personality/basic/../common/core-idioms.xml
        //                                               |    i  |
        //                                               +-------+ remove
        //
        int i = name.indexOf( "/.." );

        // Can't be at the beginning because we have no root to refer to so
        // we start at 1.
        if ( i > 0 )
        {
            int j = name.lastIndexOf( "/", i - 1 );

            name = name.substring( 0, j ) + name.substring( i + 3 );
        }

        return name;
    }
}
