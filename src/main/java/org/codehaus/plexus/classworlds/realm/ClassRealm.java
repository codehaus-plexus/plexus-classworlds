package org.codehaus.plexus.classworlds.realm;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.strategy.Strategy;
import org.codehaus.plexus.classworlds.strategy.StrategyFactory;

/**
 * The class loading gateway. Each class realm has access to the bootstrap class loader, imports form other other class
 * loaders, an optional parent class loader and of course its own class path. When queried for a class/resource, a class
 * realm will always query the bootstrap class loader first before it delegates to a pluggable strategy. The strategy in
 * turn controls the order in which imported class loaders, the parent class loader and the realm itself are searched.
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

    private SortedSet foreignImports;

    private SortedSet parentImports;

    private Strategy strategy;

    private ClassLoader parentClassLoader;

    public ClassRealm( ClassWorld world, String id, ClassLoader parentClassLoader )
    {
        super( new URL[0], null );

        this.world = world;

        this.id = id;

        foreignImports = new TreeSet();

        strategy = StrategyFactory.getStrategy( this );

        this.parentClassLoader = parentClassLoader;
    }

    public String getId()
    {
        return this.id;
    }

    public ClassWorld getWorld()
    {
        return this.world;
    }

    public void importFromParent( String packageName )
    {
        if ( parentImports == null )
        {
            parentImports = new TreeSet();
        }

        parentImports.add( new Entry( null, packageName ) );
    }

    boolean isImportedFromParent( String name )
    {
        if ( parentImports != null && !parentImports.isEmpty() )
        {
            for ( Iterator iterator = parentImports.iterator(); iterator.hasNext(); )
            {
                Entry entry = (Entry) iterator.next();

                if ( entry.matches( name ) )
                {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    public void importFrom( String realmId, String packageName )
        throws NoSuchRealmException
    {
        importFrom( getWorld().getRealm( realmId ), packageName );
    }

    public void importFrom( ClassLoader classLoader, String packageName )
    {
        foreignImports.add( new Entry( classLoader, packageName ) );
    }

    public ClassLoader getImportClassLoader( String name )
    {
        for ( Iterator iterator = foreignImports.iterator(); iterator.hasNext(); )
        {
            Entry entry = (Entry) iterator.next();

            if ( entry.matches( name ) )
            {
                return entry.getClassLoader();
            }
        }

        return null;
    }

    public Strategy getStrategy()
    {
        return strategy;
    }

    public void setParentClassLoader( ClassLoader parentClassLoader )
    {
        this.parentClassLoader = parentClassLoader;
    }

    public ClassLoader getParentClassLoader()
    {
        return parentClassLoader;
    }

    public void setParentRealm( ClassRealm realm )
    {
        this.parentClassLoader = realm;
    }

    public ClassRealm getParentRealm()
    {
        return ( parentClassLoader instanceof ClassRealm ) ? (ClassRealm) parentClassLoader : null;
    }

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {        
        ClassRealm childRealm = getWorld().newRealm( id, this );

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

    // ----------------------------------------------------------------------
    // We delegate to the Strategy here so that we can change the behavior
    // of any existing ClassRealm.
    // ----------------------------------------------------------------------

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        return loadClass( name, false );
    }

    protected Class loadClass( String name, boolean resolve )
        throws ClassNotFoundException
    {
        try
        {
            // first, try loading bootstrap classes
            return super.loadClass( name, resolve );
        }
        catch ( ClassNotFoundException e )
        {
            // next, try loading via imports, self and parent as controlled by strategy 
            return strategy.loadClass( name );
        }
    }

    protected Class findClass( String name )
        throws ClassNotFoundException
    {
        /*
         * NOTE: This gets only called from ClassLoader.loadClass(Class, boolean) while we try to check for bootstrap
         * stuff. Don't scan our class path yet, loadClassFromSelf() will do this later when called by the strategy.
         */
        throw new ClassNotFoundException( name );
    }

    public URL findResource( String name )
    {
        /*
         * NOTE: If this gets called from ClassLoader.getResource(String), delegate to the strategy. If this got called
         * directly by other code, only scan our class path as usual for an URLClassLoader.
         */
        StackTraceElement caller = new Exception().getStackTrace()[1];

        if ( "java.lang.ClassLoader".equals( caller.getClassName() ) )
        {
            return strategy.getResource( name );
        }
        else
        {
            return super.findResource( name );
        }
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        /*
         * NOTE: If this gets called from ClassLoader.getResources(String), delegate to the strategy. If this got called
         * directly by other code, only scan our class path as usual for an URLClassLoader.
         */
        StackTraceElement caller = new Exception().getStackTrace()[1];

        if ( "java.lang.ClassLoader".equals( caller.getClassName() ) )
        {
            return strategy.getResources( name );
        }
        else
        {
            return super.findResources( name );
        }
    }

    // ----------------------------------------------------------------------------
    // Display methods
    // ----------------------------------------------------------------------------

    public void display()
    {
        System.out.println( "-----------------------------------------------------" );

        for ( ClassRealm cr = this; cr != null; cr = cr.getParentRealm() )
        {
            System.out.println( "this realm =    " + cr.getId() );
            System.out.println( "this strategy = " + cr.getStrategy().getClass().getName() );

            showUrls( cr );

            System.out.println();
        }

        System.out.println( "-----------------------------------------------------" );
    }

    private static void showUrls( ClassRealm classRealm )
    {
        URL[] urls = classRealm.getURLs();

        for ( int i = 0; i < urls.length; i++ )
        {
            System.out.println( "urls[" + i + "] = " + urls[i] );
        }

        System.out.println( "Number of foreign imports: " + classRealm.foreignImports.size() );

        for ( Iterator i = classRealm.foreignImports.iterator(); i.hasNext(); )
        {
            System.out.println( "import: " + i.next() );
        }

        if ( classRealm.parentImports != null )
        {
            System.out.println( "Number of parent imports: " + classRealm.parentImports.size() );

            for ( Iterator i = classRealm.parentImports.iterator(); i.hasNext(); )
            {
                System.out.println( "import: " + i.next() );
            }
        }
    }

    public String toString()
    {
         return "ClassRealm[" + getId() + ", parent: " + getParentRealm() + "]";
    }
    
    //---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies to load a class
    //---------------------------------------------------------------------------------------------

    public Class loadClassFromImport( String name )
    {
        ClassLoader importClassLoader = getImportClassLoader( name );

        if ( importClassLoader != null )
        {
            try
            {
                return importClassLoader.loadClass( name );
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
            clazz = findLoadedClass( name );

            if ( clazz == null )
            {
                clazz = super.findClass( name );
            }
        }
        catch ( ClassNotFoundException e )
        {
            return null;
        }

        resolveClass( clazz );

        return clazz;
    }

    public Class loadClassFromParent( String name )
    {
        ClassLoader parent = getParentClassLoader();

        if ( parent != null && isImportedFromParent( name ) )
        {
            try
            {
                return parent.loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
                return null;
            }
        }

        return null;
    }

    //---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies to get a resource
    //---------------------------------------------------------------------------------------------

    public URL loadResourceFromImport( String name )
    {
        ClassLoader importClassLoader = getImportClassLoader( name );

        if ( importClassLoader != null )
        {
            return importClassLoader.getResource( name );
        }

        return null;
    }

    public URL loadResourceFromSelf( String name )
    {
        return super.findResource( name );
    }

    public URL loadResourceFromParent( String name )
    {
        ClassLoader parent = getParentClassLoader();

        if ( parent != null && isImportedFromParent( name ) )
        {
            return parent.getResource( name );
        }
        else
        {
            return null;
        }
    }

    //---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies to get resources
    //---------------------------------------------------------------------------------------------

    public Enumeration loadResourcesFromImport( String name )
    {
        ClassLoader importClassLoader = getImportClassLoader( name );

        if ( importClassLoader != null )
        {
            try
            {
                return importClassLoader.getResources( name );
            }
            catch ( IOException e )
            {
                return null;
            }
        }

        return null;
    }

    public Enumeration loadResourcesFromSelf( String name )
    {
        try
        {
            return super.findResources( name );
        }
        catch ( IOException e )
        {
            return null;
        }
    }

    public Enumeration loadResourcesFromParent( String name )
    {
        ClassLoader parent = getParentClassLoader();

        if ( parent != null && isImportedFromParent( name ) )
        {
            try
            {
                return parent.getResources( name );
            }
            catch ( IOException e )
            {
                // eat it
            }
        }

        return null;
    }

}
