package org.codehaus.plexus.classworlds.strategy;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Jason van Zyl
 * @version: $Id$
 */
public class DefaultStrategy
    extends AbstractStrategy
{
    public DefaultStrategy( ClassRealm realm )
    {
        super( realm );
    }

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        Class clazz = realm.loadClassFromImport( name );

        if ( clazz == null )
        {
            clazz = realm.loadClassFromSelf( name );
        }

        if ( clazz == null )
        {
            clazz = realm.loadClassFromParent( name );
        }

        if ( clazz == null )
        {
            throw new ClassNotFoundException( name );
        }

        return clazz;
    }

    public URL getResource( String name )
    {
        URL resource = realm.loadResourceFromImport( name );

        if ( resource == null )
        {
            resource = realm.loadResourceFromSelf( name );
        }

        if ( resource == null )
        {
            resource = realm.loadResourceFromParent( name );
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
        Vector resources = new Vector();

        loadResourcesFromRealm( resources, realm.loadResourcesFromImport( name ) );
        loadResourcesFromRealm( resources, realm.loadResourcesFromSelf( name ) );
        loadResourcesFromRealm( resources, realm.loadResourcesFromParent( name ) );
                
        return resources.elements();
    }

    private void loadResourcesFromRealm( Vector v, Enumeration e )
    {
        if ( e != null )
        {
            for ( Enumeration a = e; a.hasMoreElements(); )
            {
                v.addElement( a.nextElement() );
            }
        }
    }
}
