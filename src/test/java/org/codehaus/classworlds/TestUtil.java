/*
 * Created on Jul 31, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.codehaus.classworlds;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="bwalding@jakarta.org">Ben Walding</a>
 * @version $Id$
 */
public class TestUtil
{
    public static URL getTestResourceUrl( String resourceName )
        throws MalformedURLException
    {
        File baseDir = new File( System.getProperty( "basedir" ) );

        File testDir = new File( baseDir, "target/test-classes/test-data" );

        File resourceFile = new File( testDir, resourceName );

        return resourceFile.toURL();
    }

}
