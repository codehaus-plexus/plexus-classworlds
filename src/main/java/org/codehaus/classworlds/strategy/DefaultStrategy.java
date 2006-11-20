package org.codehaus.classworlds.strategy;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.UrlUtils;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: <a href="mailto:andy@handyande.co.uk">Andrew Williams</a>
 * @since: Nov 19, 2006
 * @version: $Id$
 */
public class DefaultStrategy
    extends URLClassLoader
    implements Strategy
{
    private ClassRealm realm;

    public DefaultStrategy()
    {
        super( new URL[0] );
    }

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        if ( name.startsWith( "org.codehaus.classworlds." ) )
        {
            return getRealm().getWorld().loadClass( name );
        }

        try
        {
            ClassRealm sourceRealm = getRealm().locateSourceRealm( name );

            if ( sourceRealm != getRealm() )
            {
                try
                {
                    return sourceRealm.loadClass( name );
                }
                catch ( ClassNotFoundException cnfe )
                {
                    // Do nothing as we will load directly
                }
            }
            return super.loadClass( name );
        }
        catch ( ClassNotFoundException e )
        {
            if ( getRealm().getParent() != null )
            {
                return getRealm().getParent().loadClass( name );
            }

            throw e;
        }
    }

    public URL getResource( String name )
    {
        URL resource = null;
        name = UrlUtils.normalizeUrlPath( name );

        ClassRealm sourceRealm = getRealm().locateSourceRealm( name );

        if ( sourceRealm != getRealm() )
        {
            resource = sourceRealm.getResource( name );
        }
        if ( resource == null )
        {
            resource = super.getResource( name );
        }

        if ( resource == null && getRealm().getParent() != null )
        {
            resource = getRealm().getParent().getResource( name );
        }

        return resource;
    }

    public InputStream getResourceAsStream( String name )
    {
        URL url = getResource( name );

        InputStream is = null;

        if ( url != null )
        {
            try
            {
                is = url.openStream();
            }
            catch ( IOException e )
            {
                // do nothing
            }
        }

        return is;
    }

    public Enumeration findResources( String name )
        throws IOException
    {
        name = UrlUtils.normalizeUrlPath( name );

        Vector resources = new Vector();

        // Load imports
        ClassRealm sourceRealm = getRealm().locateSourceRealm( name );

        if ( sourceRealm != getRealm() )
        {
            // Attempt to load directly first, then go to the imported packages.
            for ( Enumeration res = sourceRealm.findResources( name ); res.hasMoreElements(); )
            {
                resources.addElement( res.nextElement() );
            }
        }

        // Load from our classloader
        for ( Enumeration direct = super.findResources( name ); direct.hasMoreElements(); )
        {
            resources.addElement( direct.nextElement() );
        }

        // Find resources from the parent realm.
        if ( getRealm().getParent() != null )
        {
            for ( Enumeration parent = getRealm().getParent().findResources( name ); parent.hasMoreElements(); )
            {
                resources.addElement( parent.nextElement() );
            }
        }

        return resources.elements();
    }

    public void addURL( URL url )
    {
        String urlStr = url.toExternalForm();

        if ( urlStr.startsWith( "jar:" ) && urlStr.endsWith( "!/" ) )
        {
            urlStr = urlStr.substring( 4, urlStr.length() - 2 );

            try
            {
                url = new URL( urlStr );
            }
            catch ( MalformedURLException e )
            {
                e.printStackTrace();
            }
        }

        super.addURL( url );
    }

    public void setRealm( ClassRealm realm )
    {
        this.realm = realm;
    }

    public ClassRealm getRealm()
    {
        return realm;
    }
}
