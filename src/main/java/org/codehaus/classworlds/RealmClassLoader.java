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
import java.io.IOException;
import java.util.Enumeration;

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

    /** Load a class directly from this classloader without
     *  defering through any other <code>ClassRealm</code>.
     *
     *  @param name The name of the class to load.
     *
     *  @return The loaded class.
     *
     *  @throws ClassNotFoundException If the class could not be found.
     */
    Class loadClassDirect(String name) throws ClassNotFoundException
    {
        return super.loadClass( name, false );
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    //     java.lang.ClassLoader
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Load a class.
     *
     * @param name    The name of the class to load.
     * @param resolve If <code>true</code> then resolve the class.
     * @return The loaded class.
     * @throws ClassNotFoundException If the class cannot be found.
     */
    protected Class loadClass( String name,
                               boolean resolve )
        throws ClassNotFoundException
    {
        return getRealm().loadClass( name );
    }

    /**
     * Retrieve the <code>URL</code>s used by this <code>ClassLoader</code>.
     *
     * @return The urls.
     */
    public URL[] getURLs()
    {
        return super.getURLs();
    }

    /**
     * Find a resource within this ClassLoader only (don't delegate to the parent).
     *
     * @return The resource.
     */
    public URL findResource( String name )
    {
        return super.findResource( name );
    }

    public URL getResource( String name )
    {
        return getRealm().getResource( name );
    }

    /** Get a resource from this ClassLoader, and don't search the realm.
     *  Otherwise we'd recurse indefinitely.
     *
     *  @return The resource.
     */
    public URL getResourceDirect(String name)
    {
        return super.getResource( name );
    }

    public Enumeration findResources(String name) throws IOException {
        return getRealm().findResources( name );
    }

    /** Find resources from this ClassLoader, and don't search the realm.
     *  Otherwise we'd recurse indefinitely.
     *
     *  @return The resource.
     */
    public Enumeration findResourcesDirect(String name)
        throws IOException
    {
        return super.findResources( name );
    }
}
