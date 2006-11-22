package org.codehaus.classworlds.strategy;

/*
 * Copyright 2001-2006 Codehaus Foundation.
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

import org.codehaus.classworlds.realm.ClassRealm;

import java.net.URL;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;

/**
 * A strategy is a class for defining how classes and resources are located
 * in classworlds.
 *
 * @uthor: Andrew Williams
 * @since: Nov 19, 2006
 * @version: $Id$
 */
public interface Strategy
{
    Class loadClass( ClassRealm classRealm, String name )
        throws ClassNotFoundException;

    URL getResource( ClassRealm classRealm, String name );

    InputStream getResourceAsStream( ClassRealm classRealm, String name );

    Enumeration findResources( ClassRealm classRealm, String name )
        throws IOException;

    void addURL( URL url );

    URL[] getURLs();
}
