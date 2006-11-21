package org.codehaus.classworlds.strategy;

import org.codehaus.classworlds.ClassRealm;

import java.net.URL;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;

/**
 * A strategy is a class for defining how classes and resources are located
 * in classworlds.
 *
 * @uthor: <a href="mailto:andy@handyande.co.uk">Andrew Williams</a>
 * @since: Nov 19, 2006
 * @version: $Id$
 */
public interface Strategy
{
    Class loadClass( ClassRealm classRealm, String name )
        throws ClassNotFoundException;

    URL getResource( ClassRealm classRealm, String name );

    // not sure we need both find/getResources
    Enumeration getResources( ClassRealm classRealm, String string )
        throws IOException;

    InputStream getResourceAsStream( ClassRealm classRealm, String name );

    Enumeration findResources( ClassRealm classRealm, String name )
        throws IOException;

    void addURL( URL url );

    URL[] getURLs();
}
