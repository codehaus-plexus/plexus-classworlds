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

import java.util.Collection;
import java.util.Vector;

/**
 * An adapter for ClassWorlds
 *
 * @author Andrew Williams
 */
@Deprecated
public class ClassWorldAdapter
    extends ClassWorld
{

    public static ClassWorldAdapter getInstance( org.codehaus.plexus.classworlds.ClassWorld newWorld )
    {
        return new ClassWorldAdapter( newWorld );
    }

    private org.codehaus.plexus.classworlds.ClassWorld world;

    private ClassWorldAdapter( org.codehaus.plexus.classworlds.ClassWorld newWorld )
    {
        super( false );
        this.world = newWorld;
    }

    public ClassRealm newRealm( String id )
        throws DuplicateRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( world.newRealm( id ) );
        }
        catch ( org.codehaus.plexus.classworlds.realm.DuplicateRealmException e )
        {
            throw new DuplicateRealmException( this, e.getId() );
        }
    }

    public ClassRealm newRealm( String id,
                                ClassLoader classLoader )
        throws DuplicateRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( world.newRealm( id, classLoader ) );
        }
        catch ( org.codehaus.plexus.classworlds.realm.DuplicateRealmException e )
        {
            throw new DuplicateRealmException( this, e.getId() );
        }
    }

    public void disposeRealm( String id )
        throws NoSuchRealmException
    {
        try
        {
            world.disposeRealm( id );
        }
        catch ( org.codehaus.plexus.classworlds.realm.NoSuchRealmException e )
        {
            throw new NoSuchRealmException( this, e.getId() );
        }
    }

    public ClassRealm getRealm( String id )
        throws NoSuchRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( world.getRealm( id ) );
        }
        catch ( org.codehaus.plexus.classworlds.realm.NoSuchRealmException e )
        {
            throw new NoSuchRealmException( this, e.getId() );
        }            
    }

    public Collection getRealms()
    {
        Collection<org.codehaus.plexus.classworlds.realm.ClassRealm> realms = world.getRealms();
        Vector<ClassRealmAdapter> ret = new Vector<>();
        for ( org.codehaus.plexus.classworlds.realm.ClassRealm classRealm : realms )
        {
            ret.add( ClassRealmAdapter.getInstance( classRealm ) );
        }

        return ret;
    }
}
