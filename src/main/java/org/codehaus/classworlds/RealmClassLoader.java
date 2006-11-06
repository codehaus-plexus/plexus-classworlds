package org.codehaus.classworlds;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Classloader for <code>ClassRealm</code>s.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @version $Id$
 */
public class RealmClassLoader
    extends URLClassLoader
{
    protected DefaultClassRealm realm;

    RealmClassLoader( DefaultClassRealm realm )
    {
        this( realm, null );
    }

    RealmClassLoader( DefaultClassRealm realm,
                      ClassLoader classLoader )
    {
        super( new URL[0], classLoader );
        this.realm = realm;
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /**
     * Retrieve the realm.
     *
     * @return The realm.
     */
    DefaultClassRealm getRealm()
    {
        return this.realm;
    }

    /**
     * Add a constituent to this realm for locating classes.
     * If the url definition ends in .class its a BytesURLStreamHandler
     * so use defineClass insead. addURL is still called for byte[]
     * even though it has no affect and we use defineClass instead,
     * this is for consistentency and to allow access to the class
     * with getURLs()
     *
     * @param constituent URL to contituent jar or directory.
     */
    void addConstituent( URL constituent )
    {
        String urlStr = constituent.toExternalForm();

        if ( urlStr.startsWith( "jar:" ) && urlStr.endsWith( "!/" ) )
        {
            urlStr = urlStr.substring( 4, urlStr.length() - 2 );

            try
            {
                constituent = new URL( urlStr );
            }
            catch ( MalformedURLException e )
            {
                e.printStackTrace();
            }
        }

        addURL( constituent );
    }

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        if ( name.startsWith( "org.codehaus.classworlds." ) )
        {
            return getRealm().getWorld().loadClass( name );
        }

        try
        {
            if ( getRealm().getForeignClassLoader() != null )
            {
                try
                {
                    return getRealm().getForeignClassLoader().loadClass( name );
                }
                catch ( ClassNotFoundException e )
                {
                    // Do nothing as we will now look in the realm.
                }
            }

            ClassRealm sourceRealm = getRealm().locateSourceRealm( name );

            if ( sourceRealm != getRealm() )
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
            if ( getRealm().getParent() != null )
            {
                return getRealm().getParent().loadClass( name );
            }

            throw e;
        }
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

    public URL getResource( String name )
    {
        URL resource = null;
        name = UrlUtils.normalizeUrlPath( name );

        if ( getRealm().getForeignClassLoader() != null )
        {
            resource = getRealm().getForeignClassLoader().getResource( name );

            if ( resource != null )
            {
                return resource;
            }
        }

        ClassRealm sourceRealm = getRealm().locateSourceRealm( name );

        if ( sourceRealm != getRealm() )
        {
            resource = sourceRealm.getResource( name );
        }
        if ( resource == null )
        {
            resource = super.getResource( name );
        }

        if ( resource == null && getRealm().getParent() != null )
        {
            resource = getRealm().getParent().getResource( name );
        }

        return resource;
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        name = UrlUtils.normalizeUrlPath( name );

        Vector resources = new Vector();

        // Find resources from the parent class loader
        if ( getRealm().getForeignClassLoader() != null )
        {
            for ( Enumeration res = getRealm().getForeignClassLoader().getResources( name ); res.hasMoreElements(); )
            {
                resources.addElement( res.nextElement() );
            }
        }

        // Load imports
        ClassRealm sourceRealm = getRealm().locateSourceRealm( name );

        if ( sourceRealm != getRealm() )
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
        if ( getRealm().getParent() != null )
        {
            for ( Enumeration parent = getRealm().getParent().findResources( name ); parent.hasMoreElements(); )
            {
                resources.addElement( parent.nextElement() );
            }
        }

        return resources.elements();
    }
}
