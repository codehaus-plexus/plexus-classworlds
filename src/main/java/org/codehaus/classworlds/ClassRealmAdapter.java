package org.codehaus.classworlds;

import java.net.URL;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;

/**
 * An adapter for ClassRealms
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class ClassRealmAdapter
    implements ClassRealm
{

    public static ClassRealmAdapter getInstance( org.codehaus.plexus.classworlds.realm.ClassRealm newRealm )
    {
        ClassRealmAdapter adapter = new ClassRealmAdapter( newRealm );

        return adapter;
    }

    private org.codehaus.plexus.classworlds.realm.ClassRealm realm;

    private ClassRealmAdapter( org.codehaus.plexus.classworlds.realm.ClassRealm newRealm )
    {
        this.realm = newRealm;
    }

    public String getId()
    {
        return realm.getId();
    }

    public ClassWorld getWorld()
    {
        return ClassWorldAdapter.getInstance( realm.getWorld() );
    }

    public void importFrom( String realmId,
                            String pkgName )
        throws NoSuchRealmException
    {
        try
        {
            realm.importFrom( realmId, pkgName );
        }
        catch ( org.codehaus.plexus.classworlds.realm.NoSuchRealmException e )
        {
            throw new NoSuchRealmException( getWorld(), e.getId() );
        }
    }

    public void addConstituent( URL constituent )
    {
        realm.addURL( constituent );
    }

    public ClassRealm locateSourceRealm( String className )
    {
        ClassLoader importLoader = realm.getImportClassLoader( className );

        if ( importLoader instanceof org.codehaus.plexus.classworlds.realm.ClassRealm )
        {
            return ClassRealmAdapter.getInstance( (org.codehaus.plexus.classworlds.realm.ClassRealm) importLoader );
        }
        else
        {
            return null;
        }
    }

    public void setParent( ClassRealm classRealm )
    {
        if ( classRealm != null )
        {
            realm.setParentRealm( ClassRealmReverseAdapter.getInstance( classRealm ) );
        }
    }

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( realm.createChildRealm( id ) );
        }
        catch ( org.codehaus.plexus.classworlds.realm.DuplicateRealmException e )
        {
            throw new DuplicateRealmException( getWorld(), e.getId() );
        }
    }

    public ClassLoader getClassLoader()
    {
        return realm;
    }

    public ClassRealm getParent()
    {
        return ClassRealmAdapter.getInstance( realm.getParentRealm() );
    }

    public ClassRealm getParentRealm()
    {
        return ClassRealmAdapter.getInstance( realm.getParentRealm() );
    }

    public URL[] getConstituents()
    {
        return realm.getURLs();
    }

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        return realm.loadClass( name );
    }

    public URL getResource( String name )
    {
        return realm.getResource( trimLeadingSlash( name ) );
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        return realm.findResources( trimLeadingSlash( name ) );
    }

    public InputStream getResourceAsStream( String name )
    {
        return realm.getResourceAsStream( trimLeadingSlash( name ) );
    }

    public void display()
    {
        realm.display();
    }

    public boolean equals(Object o)
    {
        if ( !( o instanceof ClassRealm ) )
            return false;

        return getId().equals( ( (ClassRealm) o ).getId() );
    }

    /**
     * Provides backward-compat with the old classworlds which accepted resource names with leading slashes.
     */
    private String trimLeadingSlash( String resource )
    {
        if ( resource != null && resource.startsWith( "/" ) )
        {
            return resource.substring( 1 );
        }
        else
        {
            return resource;
        }
    }

}
