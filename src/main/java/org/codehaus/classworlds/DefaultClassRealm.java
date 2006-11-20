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


import org.codehaus.classworlds.strategy.Strategy;
import org.codehaus.classworlds.strategy.StrategyFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
 * @todo allow inheritance to be turn on/off at runtime.
 * @todo allow direction of search
 */
public class DefaultClassRealm
    implements ClassRealm
{
    private ClassWorld world;

    private String id;

    private TreeSet imports;

    private ClassLoader foreignClassLoader;

    private Strategy strategy;

    private ClassRealm parent;

    public DefaultClassRealm( ClassWorld world,
                              String id )
    {
        this( world, id, null );
    }

    public DefaultClassRealm( ClassWorld world,
                              String id,
                              ClassLoader foreignClassLoader )
    {
        this.world = world;

        this.id = id;

        imports = new TreeSet();

        if ( foreignClassLoader != null )
        {
            this.foreignClassLoader = foreignClassLoader;
        }

        strategy = StrategyFactory.getStrategy( this );
    }

    public URL[] getURLs()
    {
        return strategy.getURLs();
    }

    public ClassRealm getParent()
    {
        return parent;
    }

    public void setParent( ClassRealm parent )
    {
        this.parent = parent;
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

    public void addURL( URL url)
    {
        strategy.addURL(url);
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

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {
        ClassRealm childRealm = getWorld().newRealm( id );

        childRealm.setParent( this );

        return childRealm;
    }

    public ClassLoader getForeignClassLoader() {
      return foreignClassLoader;
    }

    public void setForeignClassLoader(ClassLoader foreignClassLoader) {
      this.foreignClassLoader = foreignClassLoader;
    }

    public void display()
    {
        ClassRealm cr = this;

        System.out.println( "-----------------------------------------------------" );


        showUrls( cr );

        while ( cr.getParent() != null )
        {
            System.out.println( "\n" );

            cr = cr.getParent();

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

    // ----------------------------------------------------------------------
    // ClassLoader API
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
}
