package org.codehaus.classworlds;

import java.util.HashMap;
import java.util.Enumeration;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * A reverse adapter for ClassRealms
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class ClassRealmReverseAdapter
    extends org.codehaus.plexus.classworlds.realm.ClassRealm
{
    private static HashMap instances = new HashMap();

    public static ClassRealmReverseAdapter getInstance( ClassRealm oldRealm )
    {
        if ( instances.containsKey( oldRealm ) )
            return (ClassRealmReverseAdapter) instances.get( oldRealm );

        ClassRealmReverseAdapter adapter = new ClassRealmReverseAdapter( oldRealm );
        instances.put( oldRealm, adapter );

        return adapter;
    }

    private ClassRealm realm;

    private ClassRealmReverseAdapter( ClassRealm oldRealm )
    {
        super( ClassWorldReverseAdapter.getInstance( oldRealm.getWorld() ),
               oldRealm.getId(), oldRealm.getClassLoader() );
        this.realm = oldRealm;
    }

    public String getId()
    {
        return realm.getId();
    }

    public org.codehaus.plexus.classworlds.ClassWorld getWorld()
    {
        return ClassWorldReverseAdapter.getInstance( realm.getWorld() );
    }

    public void importFrom( String realmId,
                            String pkgName )
        throws org.codehaus.plexus.classworlds.realm.NoSuchRealmException
    {
        try
        {
            realm.importFrom( realmId, pkgName );
        }
        catch ( NoSuchRealmException e )
        {
            throw new org.codehaus.plexus.classworlds.realm.NoSuchRealmException( getWorld(), e.getId() );
        }
    }

    public void addURL( URL constituent )
    {
        realm.addConstituent( constituent );
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm locateSourceRealm( String className )
    {
        return getInstance( realm.locateSourceRealm(
            className ) );
    }

    public void setParentRealm( org.codehaus.plexus.classworlds.realm.ClassRealm classRealm )
    {
        realm.setParent( ClassRealmAdapter.getInstance( classRealm ) );
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm createChildRealm( String id )
        throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException
    {
        try
        {
            return getInstance( realm.createChildRealm( id ) );
        }
        catch ( DuplicateRealmException e )
        {
            throw new org.codehaus.plexus.classworlds.realm.DuplicateRealmException( getWorld(), e.getId() );
        }
    }

    public ClassLoader getClassLoader()
    {
        return realm.getClassLoader();
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm getParentRealm()
    {
        return getInstance( realm.getParent() );
    }

    public URL[] getURLs()
    {
        return realm.getConstituents();
    }

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        return realm.loadClass( name );
    }

    public URL getResource( String name )
    {
        return realm.getResource( name );
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        return realm.findResources( name );
    }

    public InputStream getResourceAsStream( String name )
    {
        return realm.getResourceAsStream( name );
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
}
