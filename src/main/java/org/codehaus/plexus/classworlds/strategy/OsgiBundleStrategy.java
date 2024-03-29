package org.codehaus.plexus.classworlds.strategy;

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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

public class OsgiBundleStrategy extends AbstractStrategy {

    // java.* from parent
    // imported packages [Import-Package header with explicit constraints on the exporter]
    // requires bundle [Required-Bundle]
    // self [Bundle-Classpath header]
    // attached fragments
    //
    // We need to trya and be OSGi r4 compliant in the loading of all the bundles so that we can try to
    // load eclipse without requiring equinox. Or any other OSGi container for that matter.
    public OsgiBundleStrategy(ClassRealm realm) {
        super(realm);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = realm.loadClassFromImport(name);

        if (clazz == null) {
            clazz = realm.loadClassFromSelf(name);

            if (clazz == null) {
                clazz = realm.loadClassFromParent(name);

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
            resource = realm.loadResourceFromSelf(name);

            if (resource == null) {
                resource = realm.loadResourceFromParent(name);
            }
        }

        return resource;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> imports = realm.loadResourcesFromImport(name);
        Enumeration<URL> self = realm.loadResourcesFromSelf(name);
        Enumeration<URL> parent = realm.loadResourcesFromParent(name);

        return combineResources(imports, self, parent);
    }
}
