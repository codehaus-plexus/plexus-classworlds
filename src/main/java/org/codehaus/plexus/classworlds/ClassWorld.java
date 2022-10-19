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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.FilteredClassRealm;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

/**
 * A collection of <code>ClassRealm</code>s, indexed by id.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class ClassWorld
{
    private Map<String, ClassRealm> realms;

    private final List<ClassWorldListener> listeners = new ArrayList<>();

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
        this.realms = new LinkedHashMap<>();
    }

    public ClassRealm newRealm( String id )
        throws DuplicateRealmException
    {
        return newRealm( id, getClass().getClassLoader() );
    }

    public ClassRealm newRealm( String id, ClassLoader classLoader )
        throws DuplicateRealmException
    {
        return newRealm( id, classLoader, Collections.emptySet() );
    }

    /**
     * Shortcut for {@link #newRealm(String, ClassLoader, Set)} with the class loader of the current class.
     * @param id
     * @param allowedResourceNamePrefixes
     * @return the created class realm
     * @throws DuplicateRealmException
     * @since 2.7.0
     * @see FilteredClassRealm
     */
    public synchronized ClassRealm newRealm( String id, Set<String> allowedResourceNamePrefixes )
         throws DuplicateRealmException
    {
        return newRealm( id, getClass().getClassLoader(), allowedResourceNamePrefixes );
    }

    /**
     * Adds a class realm with filtering.
     * Only resources/classes starting with one of the given prefixes are exposed.
     * @param id
     * @param classLoader
     * @param allowedResourceNamePrefixes the prefixes of resource names which should be exposed. Separator '/' is used here (even for classes).
     * @return the created class realm
     * @throws DuplicateRealmException
     * @since 2.7.0
     * @see FilteredClassRealm
     */
    public synchronized ClassRealm newRealm( String id, ClassLoader classLoader, Set<String> allowedResourceNamePrefixes )
        throws DuplicateRealmException
    {
        if ( realms.containsKey( id ) )
        {
            throw new DuplicateRealmException( this, id );
        }

        ClassRealm realm;

        if ( allowedResourceNamePrefixes.isEmpty() )
        {
            realm = new ClassRealm( this, id, classLoader );
        }
        else
        {
            realm = new FilteredClassRealm( allowedResourceNamePrefixes, this, id, classLoader );
        }
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
        ClassRealm realm = realms.remove( id );

        if ( realm != null )
        {
            try
            {
                realm.close();
            }
            catch ( IOException ignore )
            {
            }
            for ( ClassWorldListener listener : listeners )
            {
                listener.realmDisposed( realm );
            }
        }
    }

    public synchronized ClassRealm getRealm( String id )
        throws NoSuchRealmException
    {
        if ( realms.containsKey( id ) )
        {
            return realms.get( id );
        }

        throw new NoSuchRealmException( this, id );
    }

    public synchronized Collection<ClassRealm> getRealms()
    {
        return Collections.unmodifiableList( new ArrayList<>( realms.values() ) );
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
