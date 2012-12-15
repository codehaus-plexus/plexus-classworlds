package org.codehaus.plexus.classworlds;

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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

/**
 * A collection of <code>ClassRealm</code>s, indexed by id.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class ClassWorld
{
    private Map<String, ClassRealm> realms;

    private final List<ClassWorldListener> listeners = new ArrayList<ClassWorldListener>();

    public ClassWorld( String realmId, ClassLoader classLoader )
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
        this.realms = new LinkedHashMap<String, ClassRealm>();
    }

    public ClassRealm newRealm( String id )
        throws DuplicateRealmException
    {
        return newRealm( id, getClass().getClassLoader() );
    }

    public synchronized ClassRealm newRealm( String id, ClassLoader classLoader )
        throws DuplicateRealmException
    {
        if ( realms.containsKey( id ) )
        {
            throw new DuplicateRealmException( this, id );
        }

        ClassRealm realm;

        realm = new ClassRealm( this, id, classLoader );

        realms.put( id, realm );

        for ( ClassWorldListener listener : listeners )
        {
            listener.realmCreated( realm );
        }

        return realm;
    }

    public synchronized void disposeRealm( String id )
        throws NoSuchRealmException
    {
        ClassRealm realm = (ClassRealm) realms.remove( id );

        if ( realm != null )
        {
            closeIfJava7( realm );
            for ( ClassWorldListener listener : listeners )
            {
                listener.realmDisposed( realm );
            }
        }
    }

    private void closeIfJava7( ClassRealm realm )
    {
        try
        {
            //noinspection ConstantConditions
            if ( realm instanceof Closeable )
            {
                //noinspection RedundantCast
                ( (Closeable) realm ).close();
            }
        }
        catch ( IOException ignore )
        {
        }
    }

    public synchronized ClassRealm getRealm( String id )
        throws NoSuchRealmException
    {
        if ( realms.containsKey( id ) )
        {
            return (ClassRealm) realms.get( id );
        }

        throw new NoSuchRealmException( this, id );
    }

    public synchronized Collection<ClassRealm> getRealms()
    {
        return Collections.unmodifiableList( new ArrayList<ClassRealm>( realms.values() ) );
    }

    // from exports branch
    public synchronized ClassRealm getClassRealm( String id )
    {
        if ( realms.containsKey( id ) )
        {
            return realms.get( id );
        }

        return null;
    }

    public synchronized void addListener( ClassWorldListener listener )
    {
        // TODO ideally, use object identity, not equals
        if ( !listeners.contains( listener ) )
        {
            listeners.add( listener );
        }
    }

    public synchronized void removeListener( ClassWorldListener listener )
    {
        listeners.remove( listener );
    }
}
