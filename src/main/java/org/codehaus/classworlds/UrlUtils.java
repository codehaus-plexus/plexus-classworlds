package org.codehaus.classworlds;

import java.util.Set;
import java.util.HashSet;
import java.net.URLClassLoader;

/*
 * Copyright 2001-2006 The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

    public static Set getURLs( URLClassLoader loader )
    {
        Set ret = new HashSet();

        for ( int i = 0; i < loader.getURLs().length; i++ )
        {
            ret.add( loader.getURLs()[i] );
        }

        return ret;
    }
}
