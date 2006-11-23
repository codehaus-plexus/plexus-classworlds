package org.codehaus.classworlds.strategy;

import org.codehaus.classworlds.realm.ClassRealm;
import org.codehaus.classworlds.UrlUtils;

import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: Andrew Williams
 * @since: Nov 22, 2006
 * @version: $Id$
 */
public class ForeignStrategy
    extends DefaultStrategy
{
    private ClassLoader foreign;

    public ForeignStrategy( ClassRealm realm,
                            ClassLoader foreign )
    {
        super( realm );

        this.foreign = foreign;
    }

    public Class loadClass( ClassRealm realm, String name )
        throws ClassNotFoundException
    {
        try
        {
            return foreign.loadClass( name );
        }
        catch ( ClassNotFoundException e )
        {
            return super.loadClass( realm, name );
        }
    }

    public URL getResource( ClassRealm realm, String name )
    {
        URL resource = null;

        resource = foreign.getResource( name );

        if ( resource == null )
        {
            resource = super.getResource( realm, name );
        }

        return resource;
    }

    public Enumeration findResources( ClassRealm realm, String name )
        throws IOException
    {
        name = UrlUtils.normalizeUrlPath( name );

        Vector resources = new Vector();

        // Load from DefaultStrategy
        for ( Enumeration direct = super.findResources( realm, name ); direct.hasMoreElements(); )
        {
            resources.addElement( direct.nextElement() );
        }

        // Load from foreign classloader
        for ( Enumeration direct = foreign.getResources( name ); direct.hasMoreElements(); )
        {
            resources.addElement( direct.nextElement() );
        }

        return resources.elements();
    }
}
