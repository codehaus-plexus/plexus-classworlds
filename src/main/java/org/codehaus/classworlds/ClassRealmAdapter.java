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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Adapter that wraps a modern {@link org.codehaus.plexus.classworlds.realm.ClassRealm} and exposes
 * it as the legacy {@link ClassRealm} interface.
 *
 * <p>This class is referenced directly by the compiled bytecode of
 * {@code org.eclipse.sisu:org.eclipse.sisu.plexus} and must remain on the classpath for
 * Maven 3+ to function. Do not remove or rename it. New code should not use this adapter;
 * use {@link org.codehaus.plexus.classworlds.realm.ClassRealm} directly.</p>
 *
 * @author Andrew Williams
 * @deprecated Legacy adapter retained for Sisu binary compatibility.
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "DeprecatedIsStillUsed", "rawtypes"})
@Deprecated
public final class ClassRealmAdapter implements ClassRealm {

    public static ClassRealmAdapter getInstance(org.codehaus.plexus.classworlds.realm.ClassRealm newRealm) {
        return new ClassRealmAdapter(newRealm);
    }

    private final org.codehaus.plexus.classworlds.realm.ClassRealm realm;

    private ClassRealmAdapter(org.codehaus.plexus.classworlds.realm.ClassRealm newRealm) {
        this.realm = newRealm;
    }

    @Override
    public String getId() {
        return realm.getId();
    }

    @Override
    public ClassWorld getWorld() {
        return ClassWorldAdapter.getInstance(realm.getWorld());
    }

    @Override
    public void importFrom(String realmId, String pkgName) throws NoSuchRealmException {
        try {
            realm.importFrom(realmId, pkgName);
        } catch (org.codehaus.plexus.classworlds.realm.NoSuchRealmException e) {
            throw new NoSuchRealmException(getWorld(), e.getId());
        }
    }

    @Override
    public void addConstituent(URL constituent) {
        realm.addURL(constituent);
    }

    @Override
    public ClassRealm locateSourceRealm(String className) {
        ClassLoader importLoader = realm.getImportClassLoader(className);

        if (importLoader instanceof org.codehaus.plexus.classworlds.realm.ClassRealm) {
            return ClassRealmAdapter.getInstance((org.codehaus.plexus.classworlds.realm.ClassRealm) importLoader);
        } else {
            return null;
        }
    }

    @Override
    public void setParent(ClassRealm classRealm) {
        if (classRealm != null) {
            realm.setParentRealm(ClassRealmReverseAdapter.getInstance(classRealm));
        }
    }

    @Override
    public ClassRealm createChildRealm(String id) throws DuplicateRealmException {
        try {
            return ClassRealmAdapter.getInstance(realm.createChildRealm(id));
        } catch (org.codehaus.plexus.classworlds.realm.DuplicateRealmException e) {
            throw new DuplicateRealmException(getWorld(), e.getId());
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return realm;
    }

    @Override
    public ClassRealm getParent() {
        return ClassRealmAdapter.getInstance(realm.getParentRealm());
    }

    public ClassRealm getParentRealm() {
        return ClassRealmAdapter.getInstance(realm.getParentRealm());
    }

    @Override
    public URL[] getConstituents() {
        return realm.getURLs();
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        return realm.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        return realm.getResource(trimLeadingSlash(name));
    }

    @Override
    public Enumeration findResources(String name) throws IOException {
        return realm.findResources(trimLeadingSlash(name));
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return realm.getResourceAsStream(trimLeadingSlash(name));
    }

    @Override
    public void display() {
        realm.display();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassRealm)) {
            return false;
        }

        return getId().equals(((ClassRealm) o).getId());
    }

    /**
     * Provides backward-compat with the old classworlds which accepted resource names with leading slashes.
     */
    private String trimLeadingSlash(String resource) {
        if (resource != null && resource.startsWith("/")) {
            return resource.substring(1);
        } else {
            return resource;
        }
    }
}
