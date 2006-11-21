package org.codehaus.classworlds.strategy;

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

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.UrlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: Andrew Williams
 * @since: Nov 19, 2006
 * @version: $Id$
 */
public class DefaultStrategy
    extends URLClassLoader
    implements Strategy
{
    public DefaultStrategy()
    {
        super( new URL[0] );
    }

    public Class loadClass( ClassRealm realm, String name )
        throws ClassNotFoundException
    {
        if ( name.startsWith( "org.codehaus.classworlds." ) )
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
                    return sourceRealm.loadClass( name );
                }
                catch ( ClassNotFoundException cnfe )
                {
                    // Do nothing as we will load directly
                }
            }
            return super.loadClass( name );
        }
        catch ( ClassNotFoundException e )
        {
            if ( realm.getParent() != null )
            {
                return realm.getParent().loadClass( name );
            }

            throw e;
        }
    }

    public URL getResource( ClassRealm realm, String name )
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
            resource = super.getResource( name );
        }

        if ( resource == null && realm.getParent() != null )
        {
            resource = realm.getParent().getResource( name );
        }

        return resource;
    }

    public InputStream getResourceAsStream( ClassRealm realm, String name )
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

    public Enumeration getResources( ClassRealm realm, String name )
        throws IOException
    {
        return findResources( realm, name );
    }

    public Enumeration findResources( ClassRealm realm, String name )
        throws IOException
    {
        name = UrlUtils.normalizeUrlPath( name );

        Vector resources = new Vector();

        // Load imports
        ClassRealm sourceRealm = realm.locateSourceRealm( name );

        if ( sourceRealm != realm )
        {
            // Attempt to load directly first, then go to the imported packages.
            for ( Enumeration res = sourceRealm.findResources( name ); res.hasMoreElements(); )
            {
                resources.addElement( res.nextElement() );
            }
        }

        // Load from our classloader
        for ( Enumeration direct = super.findResources( name ); direct.hasMoreElements(); )
        {
            resources.addElement( direct.nextElement() );
        }

        // Find resources from the parent realm.
        if ( realm.getParent() != null )
        {
            for ( Enumeration parent = realm.getParent().findResources( name ); parent.hasMoreElements(); )
            {
                resources.addElement( parent.nextElement() );
            }
        }

        return resources.elements();
    }

    public void addURL( URL url )
    {
        String urlStr = url.toExternalForm();

        if ( urlStr.startsWith( "jar:" ) && urlStr.endsWith( "!/" ) )
        {
            urlStr = urlStr.substring( 4, urlStr.length() - 2 );

            try
            {
                url = new URL( urlStr );
            }
            catch ( MalformedURLException e )
            {
                e.printStackTrace();
            }
        }

        super.addURL( url );
    }
}
