package org.codehaus.classworlds;

import java.net.URLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * @author Hani Suleiman (hani@formicary.net)
 *         <p/>
 *         Date: Oct 20, 2003
 *         Time: 12:46:01 AM
 */
public class BytesURLConnection extends URLConnection
{
    protected byte[] content;

    protected int offset;

    protected int length;

    public BytesURLConnection( URL url, byte[] content )
    {
        super( url );
        this.content = content;
    }

    public void connect()
    {
    }

    public InputStream getInputStream()
    {
        return new ByteArrayInputStream( content );
    }
}