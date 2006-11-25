package org.codehaus.classworlds;

import java.util.HashMap;
import java.util.Collection;

/**
 * A reverse adapter for ClassWorlds
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class ClassWorldReverseAdapter
    extends org.codehaus.plexus.classworlds.ClassWorld
{
    private static HashMap instances = new HashMap();

    public static ClassWorldReverseAdapter getInstance( ClassWorld oldWorld )
    {
        if ( instances.containsKey( oldWorld ) )
            return (ClassWorldReverseAdapter) instances.get( oldWorld );

        ClassWorldReverseAdapter adapter = new ClassWorldReverseAdapter( oldWorld );
        instances.put( oldWorld, adapter );

        return adapter;
    }

    private ClassWorld world;

    private ClassWorldReverseAdapter( ClassWorld newWorld )
    {
        super();
        this.world = newWorld;
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm newRealm( String id )
        throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException
    {
        try
        {
            return ClassRealmReverseAdapter.getInstance( world.newRealm( id ) );
        }
        catch ( DuplicateRealmException e )
        {
            throw new org.codehaus.plexus.classworlds.realm.DuplicateRealmException( this, e.getId() );
        }
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm newRealm( String id,
                                ClassLoader classLoader )
        throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException
    {
        try
        {
            return ClassRealmReverseAdapter.getInstance( world.newRealm( id,
                                                                  classLoader ) );
        }
        catch ( DuplicateRealmException e )
        {
            throw new org.codehaus.plexus.classworlds.realm.DuplicateRealmException( this, e.getId() );
        }
    }

    public void disposeRealm( String id )
        throws org.codehaus.plexus.classworlds.realm.NoSuchRealmException
    {
        try
        {
            world.disposeRealm( id );
        }
        catch ( NoSuchRealmException e )
        {
            throw new org.codehaus.plexus.classworlds.realm.NoSuchRealmException( this, e.getId() );
        }
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm getRealm( String id )
        throws org.codehaus.plexus.classworlds.realm.NoSuchRealmException
    {
        try
        {
            return ClassRealmReverseAdapter.getInstance( world.getRealm( id ) );
        }
        catch ( NoSuchRealmException e )
        {
            throw new org.codehaus.plexus.classworlds.realm.NoSuchRealmException( this, e.getId() );
        }
    }

    public Collection getRealms()
    {
        return world.getRealms();
    }
}
