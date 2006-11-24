package org.codehaus.classworlds;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.realm.NoSuchRealmException
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class NoSuchRealmException
    extends org.codehaus.plexus.classworlds.realm.NoSuchRealmException
{
    public NoSuchRealmException( org.codehaus.classworlds.ClassWorld world,
                                 String id )
    {
        super( world, id );
    }
}
