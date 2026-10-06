package org.codehaus.plexus.classworlds.realm;

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

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.strategy.Strategy;
import org.codehaus.plexus.classworlds.strategy.StrategyFactory;

/**
 * The class loading gateway. Each class realm has access to a base class loader, imports form zero or more other class
 * loaders, an optional parent class loader and of course its own class path. When queried for a class/resource, a class
 * realm will always query its base class loader first before it delegates to a pluggable strategy. The strategy in turn
 * controls the order in which imported class loaders, the parent class loader and the realm itself are searched. The
 * base class loader is assumed to be capable of loading of the bootstrap classes.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @author Jason van Zyl
 */
public class ClassRealm extends URLClassLoader {

    private final ClassWorld world;

    private final String id;

    private final SortedSet<Entry> foreignImports;

    private SortedSet<Entry> parentImports;

    private final Strategy strategy;

    private ClassLoader parentClassLoader;

    private final ConcurrentMap<String, Object> lockMap;

    /**
     * Creates a new class realm.
     *
     * @param world           The class world this realm belongs to, must not be <code>null</code>.
     * @param id              The identifier for this realm, must not be <code>null</code>.
     * @param baseClassLoader The base class loader for this realm, may be <code>null</code> to use the bootstrap class
     *                        loader.
     */
    public ClassRealm(ClassWorld world, String id, ClassLoader baseClassLoader) {
        super(new URL[0], baseClassLoader);

        this.world = world;

        this.id = id;

        foreignImports = new TreeSet<>();

        strategy = StrategyFactory.getStrategy(this);

        lockMap = new ConcurrentHashMap<>();
        // We must call super.getClassLoadingLock at least once
        // to avoid NPE in super.loadClass.
        super.getClassLoadingLock(getClass().getName());
    }

    public String getId() {
        return this.id;
    }

    public ClassWorld getWorld() {
        return this.world;
    }

    public void importFromParent(String packageName) {
        if (parentImports == null) {
            parentImports = new TreeSet<>();
        }

        parentImports.add(new Entry(null, packageName));
    }

    boolean isImportedFromParent(String name) {
        if (parentImports != null && !parentImports.isEmpty()) {
            for (Entry entry : parentImports) {
                if (entry.matches(name)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    public void importFrom(String realmId, String packageName) throws NoSuchRealmException {
        importFrom(getWorld().getRealm(realmId), packageName);
    }

    public void importFrom(ClassLoader classLoader, String packageName) {
        foreignImports.add(new Entry(classLoader, packageName));
    }

    public ClassLoader getImportClassLoader(String name) {
        for (Entry entry : foreignImports) {
            if (entry.matches(name)) {
                return entry.getClassLoader();
            }
        }

        return null;
    }

    public Collection<ClassRealm> getImportRealms() {
        Collection<ClassRealm> importRealms = new HashSet<>();

        for (Entry entry : foreignImports) {
            if (entry.getClassLoader() instanceof ClassRealm) {
                importRealms.add((ClassRealm) entry.getClassLoader());
            }
        }

        return importRealms;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public ClassLoader getParentClassLoader() {
        return parentClassLoader;
    }

    public void setParentRealm(ClassRealm realm) {
        this.parentClassLoader = realm;
    }

    public ClassRealm getParentRealm() {
        return (parentClassLoader instanceof ClassRealm) ? (ClassRealm) parentClassLoader : null;
    }

    public ClassRealm createChildRealm(String id) throws DuplicateRealmException {
        ClassRealm childRealm = getWorld().newRealm(id, null);
        childRealm.setParentRealm(this);
        return childRealm;
    }

    public void addURL(URL url) {
        String urlStr = url.toExternalForm();

        if (urlStr.startsWith("jar:") && urlStr.endsWith("!/")) {
            urlStr = urlStr.substring(4, urlStr.length() - 2);

            try {
                url = new URL(urlStr);
            } catch (MalformedURLException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        super.addURL(url);
    }

    // ----------------------------------------------------------------------
    // We delegate to the Strategy here so that we can change the behavior
    // of any existing ClassRealm.
    // ----------------------------------------------------------------------

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return unsynchronizedLoadClass(name, resolve);
    }

    private Class<?> unsynchronizedLoadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // first, try loading bootstrap classes
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            // next, try loading via imports, self and parent as controlled by strategy
            return strategy.loadClass(name);
        }
    }

    // overwrites
    // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/ClassLoader.html#findClass(java.lang.String,java.lang.String)
    // introduced in Java9
    @SuppressWarnings("Since15")
    protected Class<?> findClass(String moduleName, String name) {
        if (moduleName != null) {
            return null;
        }
        try {
            return findClassInternal(name);
        } catch (ClassNotFoundException e) {
            try {
                return strategy.getRealm().findClass(name);
            } catch (ClassNotFoundException nestedException) {
                return null;
            }
        }
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        /*
         * NOTE: This gets only called from ClassLoader.loadClass(Class, boolean) while we try to check for bootstrap
         * stuff. Don't scan our class path yet, loadClassFromSelf() will do this later when called by the strategy.
         */
        throw new ClassNotFoundException(name);
    }

    protected Class<?> findClassInternal(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public URL getResource(String name) {
        URL resource = super.getResource(name);
        return resource != null ? resource : strategy.getResource(name);
    }

    public URL findResource(String name) {
        return super.findResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Collection<URL> resources = new LinkedHashSet<>(Collections.list(super.getResources(name)));
        resources.addAll(Collections.list(strategy.getResources(name)));
        return Collections.enumeration(resources);
    }

    public Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    // ----------------------------------------------------------------------------
    // Display methods
    // ----------------------------------------------------------------------------

    public void display() {
        display(System.out);
    }

    public void display(PrintStream out) {
        out.println("-----------------------------------------------------");

        for (ClassRealm cr = this; cr != null; cr = cr.getParentRealm()) {
            out.println("realm =    " + cr.getId());
            out.println("strategy = " + cr.getStrategy().getClass().getName());

            showUrls(cr, out);

            out.println();
        }

        out.println("-----------------------------------------------------");
    }

    private static void showUrls(ClassRealm classRealm, PrintStream out) {
        URL[] urls = classRealm.getURLs();

        for (int i = 0; i < urls.length; i++) {
            out.println("urls[" + i + "] = " + urls[i]);
        }

        out.println("Number of foreign imports: " + classRealm.foreignImports.size());

        for (Entry entry : classRealm.foreignImports) {
            out.println("import: " + entry);
        }

        if (classRealm.parentImports != null) {
            out.println("Number of parent imports: " + classRealm.parentImports.size());

            for (Entry entry : classRealm.parentImports) {
                out.println("import: " + entry);
            }
        }
    }

    public String toString() {
        return "ClassRealm[" + getId() + ", parent: " + getParentClassLoader() + "]";
    }

    // ---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies to load a class
    // ---------------------------------------------------------------------------------------------

    public Class<?> loadClassFromImport(String name) {
        ClassLoader importClassLoader = getImportClassLoader(name);

        if (importClassLoader != null) {
            try {
                return importClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        return null;
    }

    public Class<?> loadClassFromSelf(String name) {
        synchronized (getClassRealmLoadingLock(name)) {
            try {
                Class<?> clazz = findLoadedClass(name);

                if (clazz == null) {
                    clazz = findClassInternal(name);
                }

                return clazz;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    private Object getClassRealmLoadingLock(String name) {
        return getClassLoadingLock(name);
    }

    @Override
    protected Object getClassLoadingLock(String name) {
        Object newLock = new Object();
        Object lock = lockMap.putIfAbsent(name, newLock);
        return (lock == null) ? newLock : lock;
    }

    public Class<?> loadClassFromParent(String name) {
        ClassLoader parent = getParentClassLoader();

        if (parent != null && isImportedFromParent(name)) {
            try {
                return parent.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies to get a resource
    // ---------------------------------------------------------------------------------------------

    public URL loadResourceFromImport(String name) {
        ClassLoader importClassLoader = getImportClassLoader(name);

        if (importClassLoader != null) {
            return importClassLoader.getResource(name);
        }

        return null;
    }

    public URL loadResourceFromSelf(String name) {
        return findResource(name);
    }

    public URL loadResourceFromParent(String name) {
        ClassLoader parent = getParentClassLoader();

        if (parent != null && isImportedFromParent(name)) {
            return parent.getResource(name);
        } else {
            return null;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Search methods that can be ordered by strategies to get resources
    // ---------------------------------------------------------------------------------------------

    public Enumeration<URL> loadResourcesFromImport(String name) {
        ClassLoader importClassLoader = getImportClassLoader(name);

        if (importClassLoader != null) {
            try {
                return importClassLoader.getResources(name);
            } catch (IOException e) {
                return null;
            }
        }

        return null;
    }

    public Enumeration<URL> loadResourcesFromSelf(String name) {
        try {
            return findResources(name);
        } catch (IOException e) {
            return null;
        }
    }

    public Enumeration<URL> loadResourcesFromParent(String name) {
        ClassLoader parent = getParentClassLoader();

        if (parent != null && isImportedFromParent(name)) {
            try {
                return parent.getResources(name);
            } catch (IOException e) {
                // eat it
            }
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------
    // Packages are visible exactly where classes in them are: own class path, imports, parent
    // ---------------------------------------------------------------------------------------------

    /**
     * Realms whose package lookup is running on the current thread. Imports and parents may form cycles, which class
     * loading never walks for a missing package but {@link Package#getPackage(String)} does.
     */
    private static final ThreadLocal<Set<ClassRealm>> PACKAGE_LOOKUPS = new ThreadLocal<>();

    @Override
    @SuppressWarnings("deprecation")
    protected Package getPackage(String name) {
        if (!enterPackageLookup()) {
            return null;
        }
        try {
            Package pkg = super.getPackage(name);

            for (String className : getClassNamesIn(name, foreignImports)) {
                if (pkg != null) {
                    break;
                }
                ClassLoader importClassLoader = getImportClassLoader(className);
                if (importClassLoader != null) {
                    pkg = getPackage(importClassLoader, name);
                }
            }

            ClassLoader parent = getParentClassLoader();
            if (pkg == null && parent != null && isPackageImportedFromParent(name)) {
                pkg = getPackage(parent, name);
            }

            return pkg;
        } finally {
            exitPackageLookup();
        }
    }

    @Override
    protected Package[] getPackages() {
        if (!enterPackageLookup()) {
            return new Package[0];
        }
        try {
            Map<String, Package> packages = new LinkedHashMap<>();

            for (Package pkg : super.getPackages()) {
                packages.putIfAbsent(pkg.getName(), pkg);
            }

            for (ClassLoader importClassLoader : getImportClassLoaders()) {
                for (Package pkg : getPackages(importClassLoader)) {
                    if (isPackageImportedFrom(pkg.getName(), importClassLoader)) {
                        packages.putIfAbsent(pkg.getName(), pkg);
                    }
                }
            }

            ClassLoader parent = getParentClassLoader();
            if (parent != null) {
                for (Package pkg : getPackages(parent)) {
                    if (isPackageImportedFromParent(pkg.getName())) {
                        packages.putIfAbsent(pkg.getName(), pkg);
                    }
                }
            }

            return packages.values().toArray(new Package[0]);
        } finally {
            exitPackageLookup();
        }
    }

    /**
     * Class names standing for every way a class in the package can be routed by the given imports: one no class can
     * have, which only package imports match, plus each imported name that is a class directly inside the package.
     */
    private static Collection<String> getClassNamesIn(String packageName, Collection<Entry> imports) {
        Collection<String> classNames = new ArrayList<>();
        classNames.add(packageName + ".-");
        if (imports != null) {
            for (Entry entry : imports) {
                String importName = entry.getPackageName();
                int index = importName.lastIndexOf('.');
                if (index > 0
                        && !importName.endsWith(".*")
                        && importName.substring(0, index).equals(packageName)) {
                    classNames.add(importName);
                }
            }
        }
        return classNames;
    }

    private boolean isPackageImportedFrom(String packageName, ClassLoader importClassLoader) {
        for (String className : getClassNamesIn(packageName, foreignImports)) {
            if (getImportClassLoader(className) == importClassLoader) {
                return true;
            }
        }
        return false;
    }

    private boolean isPackageImportedFromParent(String packageName) {
        for (String className : getClassNamesIn(packageName, parentImports)) {
            if (isImportedFromParent(className)) {
                return true;
            }
        }
        return false;
    }

    private Collection<ClassLoader> getImportClassLoaders() {
        Collection<ClassLoader> classLoaders = new LinkedHashSet<>();
        for (Entry entry : foreignImports) {
            if (entry.getClassLoader() != null) {
                classLoaders.add(entry.getClassLoader());
            }
        }
        return classLoaders;
    }

    private boolean enterPackageLookup() {
        Set<ClassRealm> lookups = PACKAGE_LOOKUPS.get();
        if (lookups == null) {
            lookups = Collections.newSetFromMap(new IdentityHashMap<>());
            PACKAGE_LOOKUPS.set(lookups);
        }
        return lookups.add(this);
    }

    private void exitPackageLookup() {
        Set<ClassRealm> lookups = PACKAGE_LOOKUPS.get();
        lookups.remove(this);
        if (lookups.isEmpty()) {
            PACKAGE_LOOKUPS.remove();
        }
    }

    // ClassLoader.getDefinedPackage(s) are public since Java 9; on Java 8 only the protected getPackage(s) exist
    private static final Method GET_DEFINED_PACKAGE = findMethod("getDefinedPackage", String.class);

    private static final Method GET_DEFINED_PACKAGES = findMethod("getDefinedPackages");

    private static final Method GET_PACKAGE =
            GET_DEFINED_PACKAGE == null ? findMethod("getPackage", String.class) : null;

    private static final Method GET_PACKAGES = GET_DEFINED_PACKAGES == null ? findMethod("getPackages") : null;

    private static Method findMethod(String name, Class<?>... parameterTypes) {
        try {
            Method method = ClassLoader.class.getDeclaredMethod(name, parameterTypes);
            if (!Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
            }
            return method;
        } catch (NoSuchMethodException | RuntimeException e) {
            return null;
        }
    }

    private static Object invoke(Method method, ClassLoader classLoader, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(classLoader, args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Package getPackage(ClassLoader classLoader, String name) {
        if (classLoader instanceof ClassRealm) {
            return ((ClassRealm) classLoader).getPackage(name);
        }
        if (GET_DEFINED_PACKAGE == null) {
            return (Package) invoke(GET_PACKAGE, classLoader, name);
        }
        for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
            Package pkg = (Package) invoke(GET_DEFINED_PACKAGE, loader, name);
            if (pkg != null) {
                return pkg;
            }
        }
        return null;
    }

    private static Package[] getPackages(ClassLoader classLoader) {
        if (classLoader instanceof ClassRealm) {
            return ((ClassRealm) classLoader).getPackages();
        }
        if (GET_DEFINED_PACKAGES == null) {
            Package[] packages = (Package[]) invoke(GET_PACKAGES, classLoader);
            return packages != null ? packages : new Package[0];
        }
        Collection<Package> packages = new ArrayList<>();
        for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
            Package[] defined = (Package[]) invoke(GET_DEFINED_PACKAGES, loader);
            if (defined != null) {
                Collections.addAll(packages, defined);
            }
        }
        return packages.toArray(new Package[0]);
    }

    static {
        registerAsParallelCapable();
    }
}
