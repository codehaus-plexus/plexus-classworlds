package org.codehaus.classworlds;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.realm.DuplicateRealmException
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class DuplicateRealmException
    extends org.codehaus.plexus.classworlds.realm.DuplicateRealmException
{
    public DuplicateRealmException( org.codehaus.classworlds.ClassWorld world,
                                    String id )
    {
        super( world, id );
    }
}
