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
public interface Strategy {
    Class loadClass( String name ) throws ClassNotFoundException;

    URL getResource( String name );

    Enumeration getResources(java.lang.String string) throws IOException;

    InputStream getResourceAsStream( String name );

    Enumeration findResources( String name ) throws IOException;

    void addURL( URL url );

    URL[] getURLs();

    void setRealm( ClassRealm realm );
}
