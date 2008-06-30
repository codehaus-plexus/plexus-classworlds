package org.codehaus.plexus.classworlds.realm;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.codehaus.plexus.classworlds.strategy.DefaultStrategy;
import org.codehaus.plexus.classworlds.strategy.ForeignStrategy;
import org.codehaus.plexus.classworlds.strategy.Strategy;
import org.codehaus.plexus.classworlds.strategy.UrlUtils;
import org.codehaus.plexus.classworlds.ClassWorld;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author Jason van Zyl
 * @version $Id$
 */
public class ClassRealm
    extends URLClassLoader
{
    private ClassWorld world;
    private String id;

    /** Packages this ClassRealm is willing to make visible to outside callers. */
    private TreeSet exports;

    /** Pacakges this ClassRealm wants to import from other realms. */
    private TreeSet imports;

    private Strategy strategy;
    private ClassRealm parent;

    public ClassRealm( ClassWorld world, String id )
    {
        this( world, id, null );
    }

    public ClassRealm( ClassWorld world, String id, ClassLoader foreignClassLoader )
    {
        super( new URL[] {}, foreignClassLoader );

        this.world = world;

        this.id = id;

        exports = new TreeSet();

        imports = new TreeSet();

        strategy = getStrategy( this, foreignClassLoader );

        if ( foreignClassLoader != null && foreignClassLoader instanceof ClassRealm )
        {
            this.parent = (ClassRealm) foreignClassLoader;
        }
    }

    public String getId()
    {
        return this.id;
    }

    public ClassWorld getWorld()
    {
        return this.world;
    }

    public void importFrom( String realmId, String packageName )
        throws NoSuchRealmException
    {
        imports.add( new Entry( getWorld().getRealm( realmId ), packageName ) );
        imports.add( new Entry( getWorld().getRealm( realmId ), packageName.replace( '.', '/' ) ) );
    }

    public ClassRealm getImportRealm( String classname )
    {
        for ( Iterator iterator = imports.iterator(); iterator.hasNext(); )
        {
            Entry entry = (Entry) iterator.next();

            if ( entry.matches( classname ) )
            {
                return entry.getRealm();
            }
        }

        return null;
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
        ClassRealm childRealm = getWorld().newRealm( id, this );

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

    //---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies
    //---------------------------------------------------------------------------------------------
    
    public Class loadClassFromImport( String name )
    {
        ClassRealm importRealm = getImportRealm( name );
        
        if ( importRealm != null )
        {
            try
            {
                return importRealm.loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
                return null;
            }
        }
        
        return null;
    }

    public Class loadClassFromSelf( String name )
    {
        Class clazz;
        
        try
        {
            clazz = findClass( name );
        }
        catch ( ClassNotFoundException e )
        {
            return null;
        }

        if ( true )
        {
            resolveClass( clazz );
        }

        return clazz;
    }

    public Class loadClassFromParent( String name )
    {
        return getParentRealm().loadClassFromSelf( name );
    }

    // ----------------------------------------------------------------------
    // We delegate to the Strategy here so that we can change the behavior
    // of any existing ClassRealm.
    // ----------------------------------------------------------------------

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        if ( name.startsWith( "org.codehaus.plexus.classworlds." ) )
        {
            return getWorld().getClass().getClassLoader().loadClass( name );
        }

        return strategy.loadClass( name );
    }

    public URL getResource( String name )
    {
        return strategy.getResource( UrlUtils.normalizeUrlPath( name ) );
    }

    public InputStream getResourceAsStream( String name )
    {
        return strategy.getResourceAsStream( name );
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        return strategy.findResources( UrlUtils.normalizeUrlPath( name ) );
    }

    // ----------------------------------------------------------------------------
    // Display methods
    // ----------------------------------------------------------------------------

    public void display()
    {
        ClassRealm cr = this;

        System.out.println( "-----------------------------------------------------" );

        while ( cr != null )
        {
            System.out.println( "this realm =    " + cr.getId() );
            System.out.println( "this strategy = " + this.getStrategy().getClass().getName() );

            showUrls( cr );

            System.out.println( "\n" );

            cr = cr.getParentRealm();
        }

        System.out.println( "-----------------------------------------------------" );
    }

    private void showUrls( ClassRealm classRealm )
    {
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

    public boolean equals( Object o )
    {
        if ( !( o instanceof ClassRealm ) )
        {
            return false;
        }

        return getId().equals( ( (ClassRealm) o ).getId() );
    }

    public String toString()
    {
        return "ClassRealm[" + getId() + ", parent: " + getParentRealm() + "]";
    }
    
    // These need to be simplified or the strategy being passed in knowingly by the user
    
    public Strategy getStrategy( ClassRealm realm )
    {
        return getStrategy( realm, "default", null );
    }

    public Strategy getStrategy( ClassRealm realm, ClassLoader foreign )
    {
        return getStrategy( realm, "default", foreign );
    }

    public Strategy getStrategy( ClassRealm realm, String hint )
    {
        return getStrategy( realm, hint, null );
    }

    public static Strategy getStrategy( ClassRealm realm, String hint, ClassLoader foreign )
    {
        if ( foreign != null )
        {
            return new ForeignStrategy( realm, foreign );
        }

        // Here we shall check hint to load non-default strategies

        Strategy ret = new DefaultStrategy( realm );

        return ret;
    }    
}
