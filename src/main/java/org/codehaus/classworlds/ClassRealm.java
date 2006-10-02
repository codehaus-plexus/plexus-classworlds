package org.codehaus.classworlds;

/*
 * Copyright 2001-2006 The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Autonomous sub-portion of a <code>ClassWorld</code>.
 * <p/>
 * <p/>
 * This class most closed maps to the <code>ClassLoader</code>
 * role from Java and in facts can provide a <code>ClassLoader</code>
 * view of itself using {@link #getClassLoader}.
 * </p>
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @author Jason van Zyl
 * @version $Id$
 */
public interface ClassRealm
{
    String getId();

    ClassWorld getWorld();

    void importFrom( String realmId,
                     String pkgName )
        throws NoSuchRealmException;

    void addConstituent( URL constituent );

    ClassRealm locateSourceRealm( String className );

    void setParent( ClassRealm classRealm );

    ClassRealm createChildRealm( String id )
        throws DuplicateRealmException;

    ClassLoader getClassLoader();

    ClassRealm getParent();

    URL[] getConstituents();

    // ----------------------------------------------------------------------
    // Classloading
    // ----------------------------------------------------------------------

    Class loadClass( String name )
        throws ClassNotFoundException;

    // ----------------------------------------------------------------------
    // Resource handling
    // ----------------------------------------------------------------------

    URL getResource( String name );

    Enumeration findResources( String name )
        throws IOException;

    InputStream getResourceAsStream( String name );

    void display();
}
