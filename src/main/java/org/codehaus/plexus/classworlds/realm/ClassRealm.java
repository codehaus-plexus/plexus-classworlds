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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.strategy.SelfFirstStrategy;
import org.codehaus.plexus.classworlds.strategy.Strategy;


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

    private SortedSet imports;

    private Strategy strategy;

    private ClassLoader parentClassLoader;

    public ClassRealm( ClassWorld world, String id, ClassLoader parentClassLoader )
    {
        super( new URL[0], null );

        this.world = world;

        this.id = id;

        imports = new TreeSet();

        strategy = new SelfFirstStrategy( this );

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

    public void importFrom( String realmId,
                            String packageName )
        throws NoSuchRealmException
    {
        imports.add( new Entry( getWorld().getRealm( realmId ), packageName ) );
    }

    public ClassRealm locateSourceRealm( String classname )
    {
        ClassRealm sourceRealm = getImportRealm( classname );
        return ( sourceRealm != null ) ? sourceRealm : this;
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
        return super.loadClass( name, false );
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

    private static void showUrls( ClassRealm classRealm )
    {
        URL[] urls = classRealm.getURLs();

        for ( int i = 0; i < urls.length; i++ )
        {
            System.out.println( "urls[" + i + "] = " + urls[i] );
        }

        System.out.println( "Number of imports: " + classRealm.imports.size() );

        for ( Iterator i = classRealm.imports.iterator(); i.hasNext(); )
        {
            System.out.println( "import: " + i.next() );
        }
    }

    public String toString()
    {
         return "ClassRealm[" + getId() + ", parent: " + getParentRealm() + "]";
    }
    
    //---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies
    //---------------------------------------------------------------------------------------------

    public ClassRealm getImportRealm( String name )
    {
        for ( Iterator iterator = imports.iterator(); iterator.hasNext(); )
        {
            Entry entry = (Entry) iterator.next();

            if ( entry.matches( name ) )
            {
                return entry.getRealm();
            }
        }

        return null;
    }
    
    public Class loadClassFromImport( String name )
    {
        ClassRealm importRealm = getImportRealm( name );
        Class clazz = null;
        if ( importRealm != null )
        {
            try
            {
                clazz = importRealm.loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
                return null;
            }
        }
        return clazz;
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

        if ( parent != null )
        {
            try
            {
                return parent.loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
                // eat it
            }
        }

        return null;
    }

    //---------------------------------------------------------------------------------------------
    // Resources
    //---------------------------------------------------------------------------------------------

    public URL loadResourceFromImport( String name )
    {
        ClassRealm importRealm = getImportRealm( name );

        if ( importRealm != null )
        {
            return importRealm.getResource( name );
        }

        return null;
    }

    public URL loadResourceFromSelf( String name )
    {
        URL url = super.findResource( name );

        return url;
    }

    public URL loadResourceFromParent( String name )
    {
        ClassLoader parent = getParentClassLoader();

        if ( parent != null )
        {
            return parent.getResource( name );
        }
        else
        {
            return null;
        }
    }

    // Resources

    public Enumeration loadResourcesFromImport( String name )
    {
        ClassRealm importRealm = getImportRealm( name );

        if ( importRealm != null )
        {
            try
            {
                return importRealm.getResources( name );
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

        if ( parent != null )
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
