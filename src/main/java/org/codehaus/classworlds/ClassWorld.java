package org.codehaus.classworlds;

/*
 * Copyright 2001-2006 Codehaus Foundation.
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

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A collection of <code>ClassRealm</code>s, indexed by id.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @version $Id$
 */
public class ClassWorld
{
    private Map realms;

    public ClassWorld( String realmId,
                       ClassLoader classLoader )
    {
        this();

        try
        {
            newRealm( realmId, classLoader );
        }
        catch ( DuplicateRealmException e )
        {
            // Will never happen as we are just creating the world.
        }
    }

    public ClassWorld()
    {
        this.realms = new LinkedHashMap();
    }

    public ClassRealm newRealm( String id )
        throws DuplicateRealmException
    {
        return newRealm( id, null );
    }

    public ClassRealm newRealm( String id,
                                ClassLoader classLoader )
        throws DuplicateRealmException
    {
        if ( realms.containsKey( id ) )
        {
            throw new DuplicateRealmException( this, id );
        }

        ClassRealm realm;

        if ( classLoader != null )
        {
            realm = new DefaultClassRealm( this, id, classLoader );

            realms.put( id, realm );
        }
        else
        {
            realm = new DefaultClassRealm( this, id );
        }

        realms.put( id, realm );

        return realm;
    }

    public void disposeRealm( String id )
        throws NoSuchRealmException
    {
        realms.remove( id );
    }

    public ClassRealm getRealm( String id )
        throws NoSuchRealmException
    {
        if ( realms.containsKey( id ) )
        {
            return (ClassRealm) realms.get( id );
        }

        throw new NoSuchRealmException( this, id );
    }

    public Collection getRealms()
    {
        return realms.values();
    }
}
