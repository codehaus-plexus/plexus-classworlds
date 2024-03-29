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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

/**
 * @author Jason van Zyl
 */
public class ParentFirstStrategy extends AbstractStrategy {

    public ParentFirstStrategy(ClassRealm realm) {
        super(realm);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = realm.loadClassFromImport(name);

        if (clazz == null) {
            clazz = realm.loadClassFromParent(name);

            if (clazz == null) {
                clazz = realm.loadClassFromSelf(name);

                if (clazz == null) {
                    throw new ClassNotFoundException(name);
                }
            }
        }

        return clazz;
    }

    public URL getResource(String name) {
        URL resource = realm.loadResourceFromImport(name);

        if (resource == null) {
            resource = realm.loadResourceFromParent(name);

            if (resource == null) {
                resource = realm.loadResourceFromSelf(name);
            }
        }

        return resource;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> imports = realm.loadResourcesFromImport(name);
        Enumeration<URL> parent = realm.loadResourcesFromParent(name);
        Enumeration<URL> self = realm.loadResourcesFromSelf(name);

        return combineResources(imports, parent, self);
    }
}
