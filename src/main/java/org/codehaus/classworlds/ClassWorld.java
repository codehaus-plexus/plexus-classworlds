package org.codehaus.classworlds;

import java.util.Collection;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.ClassWorld
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class ClassWorld
{
    private ClassWorldAdapter adapter;

    public ClassWorld( String realmId,
                       ClassLoader classLoader )
    {
        adapter = ClassWorldAdapter.getInstance(
            new org.codehaus.plexus.classworlds.ClassWorld( realmId, classLoader ) );
    }

    public ClassWorld()
    {
        adapter = ClassWorldAdapter.getInstance(
            new org.codehaus.plexus.classworlds.ClassWorld( ) );
    }

    public ClassWorld( boolean ignore )
    {
        /* fake */
    }

    public ClassRealm newRealm( String id )
        throws DuplicateRealmException
    {
        return adapter.newRealm( id );
    }

    public ClassRealm newRealm( String id,
                                ClassLoader classLoader )
        throws DuplicateRealmException
    {
        return adapter.newRealm( id, classLoader );
    }

    public void disposeRealm( String id )
        throws NoSuchRealmException
    {
        adapter.disposeRealm( id );
    }

    public ClassRealm getRealm( String id )
        throws NoSuchRealmException
    {
        return adapter.getRealm( id );
    }

    public Collection getRealms()
    {
        return adapter.getRealms();
    }
}
