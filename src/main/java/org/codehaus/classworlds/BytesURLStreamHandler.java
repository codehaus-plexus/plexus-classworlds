package org.codehaus.classworlds;

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;

/**
 * @author Hani Suleiman (hani@formicary.net)
 *         Date: Oct 20, 2003
 *         Time: 12:45:18 AM
 */
public class BytesURLStreamHandler extends URLStreamHandler
{
    byte[] content;

    int offset;

    int length;

    public BytesURLStreamHandler( byte[] content )
    {
        this.content = content;
    }

    public URLConnection openConnection( URL url )
    {
        return new BytesURLConnection( url, content );
    }
}