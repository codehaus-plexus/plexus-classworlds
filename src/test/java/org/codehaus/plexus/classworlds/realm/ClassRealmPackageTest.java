/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.plexus.classworlds.realm;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.probe.PackageProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Packages are visible from a realm exactly when classes in them are: through its own class path, an import, or the
 * parent when the parent imports allow it.
 */
class ClassRealmPackageTest extends AbstractClassWorldsTestCase {
    private ClassWorld world;

    private ClassRealm realmA;

    private Package packageA;

    @TempDir
    Path probeDir;

    @BeforeEach
    void setUp() throws Exception {
        world = new ClassWorld();
        realmA = world.newRealm("realmA", null);
        realmA.addURL(getJarUrl("a.jar"));
        packageA = realmA.loadClass("a.A").getPackage();
        assertNotNull(packageA);

        String resource = PackageProbe.class.getName().replace('.', '/') + ".class";
        Path target = probeDir.resolve(resource);
        Files.createDirectories(target.getParent());
        try (InputStream in = PackageProbe.class.getClassLoader().getResourceAsStream(resource)) {
            Files.copy(in, target);
        }
    }

    @Test
    void packageGetPackageSeesForeignImport() throws Exception {
        ClassRealm realmB = newProbeRealm("realmB");
        realmB.importFrom("realmA", "a");

        assertSame(packageA, probeGetPackage(realmB, "a"));
    }

    @Test
    void packageGetPackageSeesParentImport() throws Exception {
        ClassRealm child = newProbeRealm("child");
        child.setParentRealm(realmA);
        child.importFromParent("a");

        assertSame(packageA, probeGetPackage(child, "a"));
    }

    @Test
    void packageNotImportedStaysInvisible() throws Exception {
        ClassRealm realmB = newProbeRealm("realmB");
        realmB.importFrom("realmA", "b");

        assertNull(probeGetPackage(realmB, "a"));
        assertNull(realmB.getPackage("a"));
    }

    @Test
    void parentPackageOutsideParentImportsStaysInvisible() throws Exception {
        ClassRealm child = newProbeRealm("child");
        child.setParentRealm(realmA);
        child.importFromParent("b");

        assertNull(child.getPackage("a"));
        assertFalse(names(child.getPackages()).contains("a"));
    }

    @Test
    void filteredRealmDoesNotExposeFilteredPackage() throws Exception {
        ClassRealm filtered = world.newRealm("filtered", null, name -> !name.startsWith("a/"));
        filtered.addURL(getJarUrl("a.jar"));
        ClassRealm realmB = world.newRealm("realmB", null);
        realmB.importFrom("filtered", "a");

        assertNull(realmB.getPackage("a"));
    }

    @Test
    void firstMatchingImportDecides() throws Exception {
        ClassRealm empty = world.newRealm("empty", null);
        ClassRealm realmB = world.newRealm("realmB", null);
        realmB.importFrom("realmA", "a");
        realmB.importFrom("empty", "a.A");

        // a.A is loaded from realmA, so the package comes from realmA as well
        assertSame(packageA, realmB.getPackage("a"));
    }

    @Test
    void getPackagesListsImportedPackagesOnly() throws Exception {
        ClassRealm realmC = world.newRealm("realmC", null);
        realmC.addURL(getJarUrl("b.jar"));
        realmC.loadClass("b.B");
        ClassRealm realmB = newProbeRealm("realmB");
        realmB.importFrom("realmA", "a");

        assertTrue(Arrays.asList(probeGetPackages(realmB)).contains(packageA));
        assertFalse(names(realmB.getPackages()).contains("b"));
    }

    @Test
    void importCycleDoesNotRecurse() throws Exception {
        ClassRealm realmB = world.newRealm("realmB", null);
        ClassRealm realmC = world.newRealm("realmC", null);
        realmB.importFrom("realmC", "missing");
        realmC.importFrom("realmB", "missing");
        realmB.setParentRealm(realmC);
        realmC.setParentRealm(realmB);

        assertNull(realmB.getPackage("missing"));
        assertFalse(names(realmB.getPackages()).contains("missing"));
    }

    private ClassRealm newProbeRealm(String id) throws Exception {
        ClassRealm realm = world.newRealm(id, null);
        realm.addURL(probeDir.toUri().toURL());
        return realm;
    }

    private static Package probeGetPackage(ClassRealm realm, String name) throws Exception {
        return (Package) probeMethod(realm, "getPackage", String.class).invoke(null, name);
    }

    private static Package[] probeGetPackages(ClassRealm realm) throws Exception {
        return (Package[]) probeMethod(realm, "getPackages").invoke(null);
    }

    private static Method probeMethod(ClassRealm realm, String name, Class<?>... parameterTypes) throws Exception {
        Class<?> probe = realm.loadClass(PackageProbe.class.getName());
        assertSame(realm, probe.getClassLoader(), "probe must be defined by the realm under test");
        return probe.getMethod(name, parameterTypes);
    }

    private static java.util.List<String> names(Package[] packages) {
        return Arrays.stream(packages).map(Package::getName).collect(java.util.stream.Collectors.toList());
    }
}
