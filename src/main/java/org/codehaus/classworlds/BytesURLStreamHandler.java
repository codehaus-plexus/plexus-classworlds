package org.codehaus.classworlds;

/*
 * Copyright 2001-2010 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;

/**
 * @author Hani Suleiman (hani@formicary.net)
 *         Date: Oct 20, 2003
 *         Time: 12:45:18 AM
 */
@Deprecated
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