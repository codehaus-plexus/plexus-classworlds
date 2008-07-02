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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.strategy.DefaultStrategy;
import org.codehaus.plexus.classworlds.strategy.ForeignStrategy;
import org.codehaus.plexus.classworlds.strategy.Strategy;
import org.codehaus.plexus.classworlds.strategy.UrlUtils;

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
    private ClassRealm parentRealm;

    public ClassRealm( ClassWorld world, String id )
    {
        this( world, id, ClassLoader.getSystemClassLoader(), null );
    }

    public ClassRealm( ClassWorld world, String id, Strategy strategy )
    {
        this( world, id, ClassLoader.getSystemClassLoader(), strategy );
    }

    public ClassRealm( ClassWorld world, String id, ClassLoader parent )
    {
        this( world, id, parent, null );
    }

    public ClassRealm( ClassWorld world, String id, ClassLoader parent, Strategy strategy )
    {
        super( new URL[] {}, parent );

        this.world = world;
        this.id = id;
        exports = new TreeSet();
        imports = new TreeSet();

        if ( strategy == null )
        {
            this.strategy = new DefaultStrategy( this );
        }
        else
        {
            this.strategy = strategy;
        }

        if ( parent != null && parent instanceof ClassRealm )
        {
            this.parentRealm = (ClassRealm) parent;
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
        this.parentRealm = realm;
    }

    public ClassRealm getParentRealm()
    {
        return parentRealm;
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
        super.addURL( url );
    }

    // ----------------------------------------------------------------------------
    // These are the methods that the Strategy must use to get direct access
    // the contents of the ClassRealm.
    // ----------------------------------------------------------------------------

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

        Class clazz = null;
        
        if ( importRealm != null )
        {
            clazz = importRealm.loadClassFromSelf( name );
            
            if ( clazz == null )
            {
                clazz = importRealm.loadClassFromParent( name );
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
                clazz = findClass( name );
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
        if ( getParent() != null )
        {
            try
            {
                return getParent().loadClass( name );
            }
            catch ( ClassNotFoundException e )
            {
                return null;
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
            return importRealm.findResource( name );
        }

        return null;
    }

    public URL loadResourceFromSelf( String name )
    {
        URL url = findResource( name );

        return url;
    }

    public URL loadResourceFromParent( String name )
    {
        if ( getParent() != null )
        {
            return getParent().getResource( name );
        }

        return null;
    }    

    // Resources
    
    public Enumeration loadResourcesFromImport( String name )
    {
        ClassRealm importRealm = getImportRealm( name );

        if ( importRealm != null )
        {
            try
            {
                return importRealm.findResources( name );
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
        if ( getParent() != null )
        {
            try
            {
                return getParent().getResources( name );
            }
            catch ( IOException e )
            {
                return null;
            }
        }

        return null;
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
            System.out.println( "classloader parent: " + cr.getParent() );
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
    
    public String getClassLocation( final Class clazz )
    {
        if ( clazz == null )
        {
            return null;
        }

        URL result = null;
        String classAsResource = clazz.getName().replace( '.', '/' ).concat( ".class" );

        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        // java.lang.Class contract does not specify if 'pd' can ever be null;
        // it is not the case for Sun's implementations, but guard against null
        // just in case:
        if ( protectionDomain != null )
        {
            CodeSource codeSource = protectionDomain.getCodeSource();
            // 'cs' can be null depending on the classloader behavior:
            if ( codeSource != null )
            {
                result = codeSource.getLocation();
            }

            if ( result != null )
            {
                // Convert a code source location into a full class file location
                // for some common cases:
                if ( "file".equals( result.getProtocol() ) )
                {
                    try
                    {
                        if ( result.toExternalForm().endsWith( ".jar" ) || result.toExternalForm().endsWith( ".zip" ) )
                        {
                            result = new URL( "jar:".concat( result.toExternalForm() ).concat( "!/" ).concat( classAsResource ) );
                        }
                        else if ( new File( result.getFile() ).isDirectory() )
                        {
                            result = new URL( result, classAsResource );
                        }
                    }
                    catch ( MalformedURLException ignore )
                    {
                    }
                }
            }
        }

        if ( result == null )
        {
            // Try to find 'cls' definition as a resource; this is not
            // documented to be legal, but Sun's implementations seem to allow this:
            ClassLoader classLoader = clazz.getClassLoader();

            result = classLoader != null ? classLoader.getResource( classAsResource ) : ClassLoader.getSystemResource( classAsResource );
        }

        return result.toExternalForm();
    }    
}
