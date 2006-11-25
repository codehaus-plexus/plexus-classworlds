package org.codehaus.classworlds;

import java.util.Collection;
import java.util.Vector;
import java.util.Iterator;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.ClassWorld
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class ClassWorld
    extends org.codehaus.plexus.classworlds.ClassWorld
{
    public ClassWorld( String realmId,
                       ClassLoader classLoader )
    {
        super( realmId, classLoader );
    }

    public ClassWorld()
    {
        super();
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm newRealm( String id )
        throws DuplicateRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( super.newRealm( id ) );
        }
        catch ( org.codehaus.plexus.classworlds.realm.DuplicateRealmException e )
        {
            throw new DuplicateRealmException( this, e.getId() );
        }
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm newRealm( String id,
                                ClassLoader classLoader )
        throws DuplicateRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( super.newRealm( id, classLoader ) );
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
            super.disposeRealm( id );
        }
        catch ( org.codehaus.plexus.classworlds.realm.NoSuchRealmException e )
        {
            throw new NoSuchRealmException( this, e.getId() );
        }
    }

    public org.codehaus.plexus.classworlds.realm.ClassRealm getRealm( String id )
        throws NoSuchRealmException
    {
        try
        {
            return ClassRealmAdapter.getInstance( super.getRealm( id ) );
        }
        catch ( org.codehaus.plexus.classworlds.realm.NoSuchRealmException e )
        {
            throw new NoSuchRealmException( this, e.getId() );
        }
    }

    public Collection getRealms()
    {
        Collection parent = super.getRealms();
        Vector ret = new Vector();

        Iterator realms = parent.iterator();
        while ( realms.hasNext() )
        {
            org.codehaus.plexus.classworlds.realm.ClassRealm classRealm =
                (org.codehaus.plexus.classworlds.realm.ClassRealm) realms.next();

            ret.add( ClassRealmAdapter.getInstance( classRealm ) );
        }

        return ret;
    }
}
