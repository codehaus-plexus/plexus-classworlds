/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.plexus.classworlds.realm;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * A realm that defines a class owns the {@link Package} of that class, even when an import or the parent exposes a
 * package of the same name. On Java 8 the JDK asks the overridable {@code getPackage(String)} while it defines a class,
 * so the realm must not answer from its imports at that point.
 */
class ClassRealmOwnPackageTest {
    private final ClassWorld world = new ClassWorld();

    private final List<ClassRealm> unregistered = new ArrayList<>();

    @TempDir
    Path dir;

    @AfterEach
    void tearDown() throws IOException {
        // open jars keep the temporary directory from being deleted on Windows
        for (ClassRealm realm : unregistered) {
            realm.close();
        }
        world.close();
    }

    @Test
    void classImportDoesNotReplaceOwnPackage() throws Exception {
        newForeignRealm(false);
        ClassRealm own = newOwnRealm(false);
        own.importFrom("foreign", "a.Y");

        Class<?> x = own.loadClass("a.X");

        assertSame(own, x.getClassLoader());
        assertEquals("1.0", x.getPackage().getImplementationVersion());
        assertSame(x.getPackage(), own.getPackage("a"));
    }

    @Test
    void packageImportDoesNotReplaceOwnPackage() throws Exception {
        newForeignRealm(false);
        ClassRealm own = newOwnRealm(false);
        own.importFrom("foreign", "a");

        Class<?> x = own.loadClass("a.X");

        assertSame(own, x.getClassLoader());
        assertEquals("1.0", x.getPackage().getImplementationVersion());
    }

    @Test
    void parentImportDoesNotReplaceOwnPackage() throws Exception {
        ClassRealm foreign = newForeignRealm(false);
        ClassRealm own = newOwnRealm(false);
        own.setParentRealm(foreign);
        own.importFromParent("a.Y");

        Class<?> x = own.loadClass("a.X");

        assertSame(own, x.getClassLoader());
        assertEquals("1.0", x.getPackage().getImplementationVersion());
    }

    @Test
    void sealedImportedPackageDoesNotBreakOwnClass() throws Exception {
        ClassRealm foreign = newForeignRealm(true);
        ClassRealm own = newOwnRealm(false);
        own.importFrom("foreign", "a.Y");

        Class<?> x = own.loadClass("a.X");

        assertEquals("1.0", x.getPackage().getImplementationVersion());
        assertNotSame(foreign.loadClass("a.Y").getPackage(), x.getPackage());
    }

    @Test
    void sealedOwnPackageDoesNotBreakOnImportedPackage() throws Exception {
        newForeignRealm(false);
        ClassRealm own = newOwnRealm(true);
        own.importFrom("foreign", "a.Y");

        Class<?> x = own.loadClass("a.X");

        assertNotNull(x.getPackage());
        assertEquals("1.0", x.getPackage().getImplementationVersion());
    }

    @Test
    void importsOfRealmBaseLoaderDoNotReplaceOwnPackage() throws Exception {
        newForeignRealm(true);
        ClassRealm base = world.newRealm("base", null);
        base.importFrom("foreign", "a.Y");
        ClassRealm own = world.newRealm("own", base);
        own.addURL(jar("own.jar", "X", "1.0", false).toUri().toURL());

        Class<?> x = own.loadClass("a.X");

        assertSame(own, x.getClassLoader());
        assertEquals("1.0", x.getPackage().getImplementationVersion());
    }

    @Test
    void definePackageIgnoresImportedPackage() throws Exception {
        newForeignRealm(false);
        DefiningRealm own = new DefiningRealm(world, "own");
        unregistered.add(own);
        own.importFrom("foreign", "a.Y");

        Package defined = own.define("a");

        assertSame(defined, own.getPackage("a"));
    }

    /** Lets a test call {@code definePackage} the way a subclass in a container may. */
    private static final class DefiningRealm extends ClassRealm {
        DefiningRealm(ClassWorld world, String id) {
            super(world, id, null);
        }

        Package define(String name) {
            return definePackage(name, null, null, null, null, null, null, (URL) null);
        }
    }

    /** A realm whose jar holds {@code a.Y} at version 2.0 and which has already defined its package {@code a}. */
    private ClassRealm newForeignRealm(boolean sealed) throws Exception {
        ClassRealm realm = world.newRealm("foreign", null);
        realm.addURL(jar("foreign.jar", "Y", "2.0", sealed).toUri().toURL());
        assertNotNull(realm.loadClass("a.Y").getPackage());
        return realm;
    }

    /** A realm in the same world whose jar holds {@code a.X} at version 1.0. */
    private ClassRealm newOwnRealm(boolean sealed) throws Exception {
        ClassRealm realm = world.newRealm("own", null);
        realm.addURL(jar("own.jar", "X", "1.0", sealed).toUri().toURL());
        return realm;
    }

    private Path jar(String name, String className, String version, boolean sealed) throws IOException {
        Path sources = Files.createDirectories(dir.resolve(name + "-src"));
        Path classes = Files.createDirectories(dir.resolve(name + "-classes"));
        Path source = sources.resolve(className + ".java");
        Files.write(source, ("package a; public class " + className + " {}").getBytes("UTF-8"));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assumeTrue(compiler != null, "these tests compile their jars and need a JDK, not a JRE");
        if (compiler.run(null, null, null, "-d", classes.toString(), source.toString()) != 0) {
            throw new IOException("compilation failed for " + source);
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, version);
        if (sealed) {
            manifest.getMainAttributes().put(Attributes.Name.SEALED, "true");
        }
        Path jar = dir.resolve(name);
        try (OutputStream out = Files.newOutputStream(jar);
                JarOutputStream jos = new JarOutputStream(out, manifest)) {
            jos.putNextEntry(new JarEntry("a/" + className + ".class"));
            jos.write(Files.readAllBytes(classes.resolve("a").resolve(className + ".class")));
            jos.closeEntry();
        }
        return jar;
    }
}
