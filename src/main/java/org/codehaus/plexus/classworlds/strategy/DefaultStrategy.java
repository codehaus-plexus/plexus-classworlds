package org.codehaus.plexus.classworlds.strategy;

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

import org.codehaus.plexus.classworlds.UrlUtils;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Load classes directly from the Realm first before attempting to get from the parent.
 *
 * @uthor: Andrew Williams
 * @since: Nov 19, 2006
 * @version: $Id$
 */
public class DefaultStrategy
    extends AbstractStrategy
{
    public DefaultStrategy( ClassRealm realm )
    {
        super( realm );
    }

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        if ( name.startsWith( "org.codehaus.plexus.classworlds." ) )
        {
            return realm.getWorld().getClass().getClassLoader().loadClass( name );
        }

        try
        {
            ClassRealm sourceRealm = realm.locateSourceRealm( name );

            if ( sourceRealm != realm )
            {
                try
                {
                    return sourceRealm.loadRealmClass( name );
                }
                catch ( ClassNotFoundException cnfe )
                {
                    // Do nothing as we will load directly
                }
            }

            return realm.loadRealmClass( name );
        }
        catch ( ClassNotFoundException e )
        {
            if ( realm.getParentRealm() != null )
            {
                return realm.getParentRealm().loadClass( name );
            }

            throw e;
        }
    }

    public URL getResource( String name )
    {
        URL resource = null;

        name = UrlUtils.normalizeUrlPath( name );

        ClassRealm sourceRealm = realm.locateSourceRealm( name );

        if ( sourceRealm != realm )
        {
            resource = sourceRealm.getResource( name );
        }
        if ( resource == null )
        {
            resource = realm.getRealmResource( name );
        }

        if ( resource == null && realm.getParent() != null )
        {
            resource = realm.getParentRealm().getRealmResource( name );
        }

        return resource;
    }

    public InputStream getResourceAsStream( String name )
    {
        URL url = getResource( name );

        InputStream is = null;

        if ( url != null )
        {
            try
            {
                is = url.openStream();
            }
            catch ( IOException e )
            {
                // do nothing
            }
        }

        return is;
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        name = UrlUtils.normalizeUrlPath( name );

        Vector resources = new Vector();

        // Load imports
        ClassRealm sourceRealm = realm.locateSourceRealm( name );

        if ( sourceRealm != realm )
        {
            // Attempt to load directly first, then go to the imported packages.
            for ( Enumeration res = sourceRealm.findRealmResources( name ); res.hasMoreElements(); )
            {
                resources.addElement( res.nextElement() );
            }
        }

        // Load from our realm
        for ( Enumeration direct = realm.findRealmResources( name ); direct.hasMoreElements(); )
        {
            resources.addElement( direct.nextElement() );
        }

        // Find resources from the parent realm.
        if ( realm.getParentRealm() != null )
        {
            for ( Enumeration parent = realm.getParentRealm().findRealmResources( name ); parent.hasMoreElements(); )
            {
                resources.addElement( parent.nextElement() );
            }
        }

        return resources.elements();
    }
}
