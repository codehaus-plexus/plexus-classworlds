package org.codehaus.classworlds;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.realm.ClassRealm
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public abstract class ClassRealm
    extends org.codehaus.plexus.classworlds.realm.ClassRealm
{
    public ClassRealm( ClassWorld world, String id )
    {
        this( world, id, null );
    }

    public ClassRealm( ClassWorld world, String id, ClassLoader foreignClassLoader )
    {
        super( world, id, foreignClassLoader );
    }

    public String getId()
    {
        return super.getId();
    }

    public ClassWorld getWorld()
    {
        return ClassWorldAdapter.getInstance( super.getWorld() );
    }

    public void importFrom( String realmId, String pkgName )
        throws NoSuchRealmException
    {
        try
        {
            super.importFrom( realmId, pkgName );
        }
        catch ( org.codehaus.plexus.classworlds.realm.NoSuchRealmException e )
        {
            throw new NoSuchRealmException( getWorld(), e.getId() );
        }
    }

    public void addConstituent( URL constituent )
    {
        super.addURL( constituent );
    }

    public ClassRealm locateSourceRealm( String className )
    {
        return ClassRealmAdapter.getInstance( super.locateSourceRealm( className ) );
    }

    public void setParent( ClassRealm classRealm )
    {
        super.setParentRealm( classRealm );
    }

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( super.createChildRealm( id ) );
        }
        catch ( org.codehaus.plexus.classworlds.realm.DuplicateRealmException e )
        {
            throw new DuplicateRealmException( getWorld(), e.getId() );
        }
    }

    public ClassLoader getClassLoader()
    {
        return this;
    }

// CANNOT DO THIS ANY MORE
//    public abstract ClassRealm getParent();

    public URL[] getConstituents()
    {
        return super.getURLs();
    }

    // ----------------------------------------------------------------------
    // Classloading
    // ----------------------------------------------------------------------

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        return super.loadClass( name );
    }

    // ----------------------------------------------------------------------
    // Resource handling
    // ----------------------------------------------------------------------

    public URL getResource( String name )
    {
        return super.getResource( name );
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        return super.findResources( name );
    }

    public InputStream getResourceAsStream( String name )
    {
        return super.getResourceAsStream( name );
    }

    public void display()
    {
        super.display();
    }
}
