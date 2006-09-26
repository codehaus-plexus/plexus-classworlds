package org.codehaus.classworlds;

/*
 $Id$

 Copyright 2002 (C) The Werken Company. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "classworlds" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Werken Company.  For written permission,
    please contact bob@werken.com.

 4. Products derived from this Software may not be called "classworlds"
    nor may "classworlds" appear in their names without prior written
    permission of The Werken Company. "classworlds" is a registered
    trademark of The Werken Company.

 5. Due credit should be given to The Werken Company.
    (http://classworlds.werken.com/).

 THIS SOFTWARE IS PROVIDED BY THE WERKEN COMPANY AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE WERKEN COMPANY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;


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

    private RealmClassLoader classLoader;

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

        classLoader = new RealmClassLoader( this );
    }

    public URL[] getConstituents()
    {
        return classLoader.getURLs();
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

    public void addConstituent( URL constituent )
    {
        classLoader.addConstituent( constituent );
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

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {
        ClassRealm childRealm = getWorld().newRealm( id );

        childRealm.setParent( this );

        return childRealm;
    }

    // ----------------------------------------------------------------------
    // ClassLoader API
    // ----------------------------------------------------------------------

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        if ( name.startsWith( "org.codehaus.classworlds." ) )
        {
            return getWorld().loadClass( name );
        }

        try
        {
            if ( foreignClassLoader != null )
            {
                try
                {
                    return foreignClassLoader.loadClass( name );
                }
                catch ( ClassNotFoundException e )
                {
                    // Do nothing as we will now look in the realm.
                }
            }

            ClassRealm sourceRealm = locateSourceRealm( name );

            if ( sourceRealm == this )
            {
                return classLoader.loadClassDirect( name );
            }
            else
            {
                try
                {
                    return sourceRealm.loadClass( name );
                }
                catch ( ClassNotFoundException cnfe )
                {
                    // If we can't find it in an import, try loading directly.
                    return classLoader.loadClassDirect( name );
                }
            }
        }
        catch ( ClassNotFoundException e )
        {
            if ( getParent() != null )
            {
                return getParent().loadClass( name );
            }

            throw e;
        }
    }

    public URL getResource( String name )
    {
        URL resource = null;
        name = UrlUtils.normalizeUrlPath( name );

        if ( foreignClassLoader != null )
        {
            resource = foreignClassLoader.getResource( name );

            if ( resource != null )
            {
                return resource;
            }
        }

        ClassRealm sourceRealm = locateSourceRealm( name );

        if ( sourceRealm == this )
        {
            resource = classLoader.getResourceDirect( name );
        }
        else
        {
            resource = sourceRealm.getResource( name );

            if ( resource == null )
            {
                resource = classLoader.getResourceDirect( name );
            }
        }

        if ( resource == null && getParent() != null )
        {
            resource = getParent().getResource( name );
        }

        return resource;
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

    public Enumeration findResources( String name )
        throws IOException
    {
        name = UrlUtils.normalizeUrlPath( name );

        Vector resources = new Vector();

        // Find resources from the parent class loader
        if ( foreignClassLoader != null )
        {
            for ( Enumeration res = foreignClassLoader.getResources( name ); res.hasMoreElements(); )
            {
                resources.addElement( res.nextElement() );
            }
        }

        // Load imports
        ClassRealm sourceRealm = locateSourceRealm( name );

        if ( sourceRealm != this )
        {
            // Attempt to load directly first, then go to the imported packages.
            for ( Enumeration res = sourceRealm.findResources( name ); res.hasMoreElements(); )
            {
                resources.addElement( res.nextElement() );
            }
        }

        // Load from our classloader
        for ( Enumeration direct = classLoader.findResourcesDirect( name ); direct.hasMoreElements(); )
        {
            resources.addElement( direct.nextElement() );
        }

        // Find resources from the parent realm.
        if ( parent != null )
        {
            for ( Enumeration parent = getParent().findResources( name ); parent.hasMoreElements(); )
            {
                resources.addElement( parent.nextElement() );
            }
        }

        return resources.elements();
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

        URL[] urls = classRealm.getConstituents();

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
