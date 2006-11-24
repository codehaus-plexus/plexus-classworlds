package org.codehaus.classworlds.realm;

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

import org.codehaus.classworlds.strategy.Strategy;
import org.codehaus.classworlds.strategy.StrategyFactory;
import org.codehaus.classworlds.ClassWorld;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * Implementation of <code>ClassRealm</code>.  The realm is the class loading gateway.
 * The search is proceded as follows:
 * <ol>
 * <li>Search the parent class loader (passed via the constructor) if there
 * is one.</li>
 * <li>Search the imports.</li>
 * <li>Search this realm's constituents.</li>
 * <li>Search the parent realm.</li>
 * </ol>
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @author Jason van Zyl
 * @version $Id$
 */
public class ClassRealm
    extends URLClassLoader
{
    private ClassWorld world;

    private String id;

    private TreeSet imports;

    private Strategy strategy;

    private ClassRealm parent;

    public ClassRealm( ClassWorld world,
                       String id )
    {
        this( world, id, null );
    }

    public ClassRealm( ClassWorld world,
                       String id,
                       ClassLoader foreignClassLoader )
    {
        super( new URL[]{}, foreignClassLoader );

        this.world = world;

        this.id = id;

        imports = new TreeSet();

        strategy = StrategyFactory.getStrategy( this, foreignClassLoader );
    }

    public String getId()
    {
        return this.id;
    }

    public ClassWorld getWorld()
    {
        return this.world;
    }

    public void importFrom( String realmId,
                            String packageName )
        throws NoSuchRealmException
    {
        imports.add( new Entry( getWorld().getRealm( realmId ), packageName ) );
        imports.add( new Entry( getWorld().getRealm( realmId ), packageName.replace( '.', '/' ) ) );
    }
        
    public ClassRealm locateSourceRealm( String classname )
    {
        for ( Iterator iterator = imports.iterator(); iterator.hasNext(); )
        {
            Entry entry = (Entry) iterator.next();

            if ( entry.matches( classname ) )
            {
                return entry.getRealm();
            }
        }

        return this;
    }

    public Strategy getStrategy()
    {
        return strategy;
    }

    public void setParentRealm( ClassRealm realm )
    {
        this.parent = realm;
    }

    public ClassRealm getParentRealm()
    {
        return parent;
    }

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {
        ClassRealm childRealm = getWorld().newRealm( id );

        childRealm.setParentRealm( this );

        return childRealm;
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

    // ----------------------------------------------------------------------------
    // These are the methods that the Strategy must use to get direct access
    // the contents of the ClassRealm.
    // ----------------------------------------------------------------------------

    public Class loadRealmClass( String name )
        throws ClassNotFoundException
    {
        return super.loadClass( name );
    }

    public URL getRealmResource( String name )
    {
        return super.getResource( name );
    }

    public InputStream getRealmResourceAsStream( String name )
    {
        return super.getResourceAsStream( name );
    }

    public Enumeration findRealmResources( String name )
        throws IOException
    {
        return super.findResources( name );
    }

    // ----------------------------------------------------------------------
    // We delegate to the Strategy here so that we can change the behavior
    // of any existing ClassRealm.
    // ----------------------------------------------------------------------

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        return strategy.loadClass( name );
    }

    public URL getResource( String name )
    {
        return strategy.getResource( name );
    }

    public InputStream getResourceAsStream( String name )
    {
        return strategy.getResourceAsStream( name );
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        return strategy.findResources( name );
    }

    // ----------------------------------------------------------------------------
    // Display methods
    // ----------------------------------------------------------------------------
        
    public void display()
    {
        ClassRealm cr = this;

        System.out.println( "-----------------------------------------------------" );

        showUrls( cr );

        while ( cr.getParent() != null )
        {
            System.out.println( "\n" );

            cr = cr.getParentRealm();

            showUrls( cr );
        }

        System.out.println( "-----------------------------------------------------" );
    }

    private void showUrls( ClassRealm classRealm )
    {
        System.out.println( "this realm = " + classRealm.getId() );

        URL[] urls = classRealm.getURLs();

        for ( int i = 0; i < urls.length; i++ )
        {
            System.out.println( "urls[" + i + "] = " + urls[i] );
        }

        System.out.println( "Number of imports: " + imports.size() );

        for ( Iterator i = imports.iterator(); i.hasNext(); )
        {
            System.out.println( "import: " + i.next() );
        }
    }
}
