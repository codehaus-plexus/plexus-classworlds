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

import java.util.Collection;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.ClassWorld
 * provided for legacy code
 *
 * @author Andrew Williams
 */
@Deprecated
public class ClassWorld {
    private ClassWorldAdapter adapter;

    public ClassWorld(String realmId, ClassLoader classLoader) {
        adapter = ClassWorldAdapter.getInstance(new org.codehaus.plexus.classworlds.ClassWorld(realmId, classLoader));
    }

    public ClassWorld() {
        adapter = ClassWorldAdapter.getInstance(new org.codehaus.plexus.classworlds.ClassWorld());
    }

    public ClassWorld(boolean ignore) {
        /* fake */
    }

    public ClassRealm newRealm(String id) throws DuplicateRealmException {
        return adapter.newRealm(id);
    }

    public ClassRealm newRealm(String id, ClassLoader classLoader) throws DuplicateRealmException {
        return adapter.newRealm(id, classLoader);
    }

    public void disposeRealm(String id) throws NoSuchRealmException {
        adapter.disposeRealm(id);
    }

    public ClassRealm getRealm(String id) throws NoSuchRealmException {
        return adapter.getRealm(id);
    }

    public Collection getRealms() {
        return adapter.getRealms();
    }
}
