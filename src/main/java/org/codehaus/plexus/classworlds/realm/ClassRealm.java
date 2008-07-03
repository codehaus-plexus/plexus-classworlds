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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.strategy.DefaultStrategy;
import org.codehaus.plexus.classworlds.strategy.Strategy;

import com.sun.tools.jdi.LinkedHashMap;

/**
 * @author Jason van Zyl
 * @version $Id$
 */
public class ClassRealm
    extends URLClassLoader
{
    /** The ClassWorld we live in. */
    private ClassWorld world;

    /** The unique id of our ClassRealm */
    private String id;

    /** Packages this ClassRealm is willing to make visible to outside callers. */
    private Set exports;

    /** Pacakges this ClassRealm wants to import from other realms. */
    private Set imports;
    
    /** Map of packages to import from given realms */
    private Map importRealmMappings;
    
    /** The strategy we are using to load classes and resources. */
    private Strategy strategy;

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
        this.exports = new TreeSet();
        this.imports = new TreeSet();
        this.importRealmMappings = new LinkedHashMap();

        if ( strategy == null )
        {
            this.strategy = new DefaultStrategy( this );
        }
        else
        {
            this.strategy = strategy;
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
        Entry e = new Entry( packageName );
        imports.add( e );
        importRealmMappings.put( e.pkgName, realmId );
        
        Entry e1 = new Entry( packageName.replace( '.', '/' ) );
        imports.add( e1 );
        importRealmMappings.put( e1.pkgName, realmId );            
    }

    public ClassRealm getImportRealm( String classname )
    {
        for ( Iterator iterator = imports.iterator(); iterator.hasNext(); )
        {
            Entry entry = (Entry) iterator.next();

            if ( entry.matches( classname ) )
            {
                return world.getClassRealm( (String) importRealmMappings.get( entry.pkgName ) );
            }
        }

        return null;
    }

    public ClassRealm locateSourceRealm( String classname )
    {
        ClassRealm importRealm = getImportRealm( classname );
        
        if ( importRealm != null )
        {
            return importRealm;
        }
        
        return this;        
    }

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {
        return getWorld().newRealm( id, this );
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
        return strategy.getResource( normalizeUrlPath( name ) );
    }

    public InputStream getResourceAsStream( String name )
    {
        return strategy.getResourceAsStream( name );
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        return strategy.findResources( normalizeUrlPath( name ) );
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
            System.out.println( "this strategy = " + strategy.getClass().getName() );

            showUrls( cr );

            System.out.println( "\n" );
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
        return "ClassRealm[" + getId() + ", parent: " + getParent() + "]";
    }

    /**
     * Find the exact URL that a particular class was loaded from. This can be used to debug
     * problems where you need to know exactly what JAR, or location in the file system that a class
     * came from.
     * 
     * @param clazz
     * @return The location of class that was loaded in URL external form.
     */
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

    // Util methods

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

    public static class Entry
        implements Comparable
    {
        private final String pkgName;

        Entry( String pkgName )
        {
            this.pkgName = pkgName;
        }

        String getPackageName()
        {
            return this.pkgName;
        }

        /**
         * Determine if the classname matches the package described by this entry.
         * 
         * @param classname The class name to test.
         * @return <code>true</code> if this entry matches the classname, otherwise
         *         <code>false</code>.
         */
        boolean matches( String classname )
        {
            return classname.startsWith( getPackageName() );
        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
        //     java.lang.Comparable
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

        /**
         * Compare this entry to another for relative ordering. <p/> <p/> The natural ordering of
         * Entry objects is reverse-alphabetical based upon package name. </p>
         * 
         * @param thatObj The object to compare.
         * @return -1 if this object sorts before that object, 0 if they are equal, or 1 if this
         *         object sorts after that object.
         */
        public int compareTo( Object thatObj )
        {
            Entry that = (Entry) thatObj;

            // We are reverse sorting this list, so that
            // we get longer matches first:
            //
            //     com.werken.foo.bar
            //     com.werken.foo
            //     com.werken

            return ( getPackageName().compareTo( that.getPackageName() ) ) * -1;
        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
        //     java.lang.Object
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

        /**
         * Test this entry for equality to another. <p/> <p/> Consistent with {@link #compareTo},
         * this method tests for equality purely on the package name. </p>
         * 
         * @param thatObj The object to compare
         * @return <code>true</code> if the two objects are semantically equivalent, otherwise
         *         <code>false</code>.
         */
        public boolean equals( Object thatObj )
        {
            Entry that = (Entry) thatObj;

            return getPackageName().equals( that.getPackageName() );
        }

        /**
         * <p/> Consistent with {@link #equals}, this method creates a hashCode based on the
         * packagename. </p>
         */
        public int hashCode()
        {
            return getPackageName().hashCode();
        }
    }
}
