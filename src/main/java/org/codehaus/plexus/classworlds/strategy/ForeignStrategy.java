package org.codehaus.plexus.classworlds.strategy;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Jason van Zyl
 * @author: Andrew Williams
 */
public class ForeignStrategy
    extends DefaultStrategy
{
    private ClassLoader foreign;

    public ForeignStrategy( ClassRealm realm, ClassLoader foreign )
    {
        super( realm );

        this.foreign = foreign;
    }

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        try
        {
            return foreign.loadClass( name );
        }
        catch ( ClassNotFoundException e )
        {
            return super.loadClass( name );
        }
    }

    public URL getResource( String name )
    {
        URL resource;

        resource = foreign.getResource( name );

        if ( resource == null )
        {
            resource = super.getResource( name );
        }

        return resource;
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        Vector resources = new Vector();

        // Load from DefaultStrategy
        for ( Enumeration direct = super.findResources( name ); direct.hasMoreElements(); )
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
