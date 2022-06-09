package org.codehaus.classworlds;

/*
 * Copyright 2001-2010 Codehaus Foundation.
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

import java.util.HashMap;
import java.util.Collection;
import java.util.Vector;

/**
 * A reverse adapter for ClassWorlds
 *
 * @author Andrew Williams
 */
@Deprecated
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
        Collection realms = world.getRealms();
        Vector ret = new Vector();

        for ( Object o : realms )
        {
            ClassRealm realm = (ClassRealm) o;
            ret.add( ClassRealmReverseAdapter.getInstance( realm ) );
        }

        return ret;
    }
}
