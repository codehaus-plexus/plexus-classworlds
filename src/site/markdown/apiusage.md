---
title: Classworlds API Usage
author: bob mcwhirter
---

# Classworlds API Usage

The Java API can be used to create new realms and connect
realms together through importation of specific packages.

The core of the **Classworlds** infrastructure is
the
[ClassWorld](apidocs/index.html?org/codehaus/plexus/classworlds/ClassWorld.html)
class.  An application must create a `ClassWorld` instance.
It is advisable to store the instance as a singleton or some other
handy location.

```

ClassWorld world = new ClassWorld();

```

Once a `ClassWorld` is created, realms within it
can be created.  These realms effectively only allow loading
of the core JVM classes initially.

```

ClassWorld world = new ClassWorld();
ClassRealm containerRealm    = world.newRealm( "container" );
ClassRealm logComponentRealm = world.newRealm( "logComponent" );

```

In order to make each `ClassRealm` useful, constituents
in form of URLs must be added to it where each can provide certain classes.
The URL must return either a JAR or a directory on the default file system.

```

containerRealm.addURL( containerJarUrl );
logComponentRealm.addURL( logComponentJarUrl );

```

`ClassRealm`s can optionally be filtered to further restrict which classes/resources
are exposed. The filter is provided as additional argument to `world.newRealm( "filteredcontainer", myPredicate );`

Now, links between the various realms need to be created to allow
classes loaded from one to be available to classes loaded in another.

```

logComponentRealm.importFrom( "container", 
                              "com.werken.projectz.component" );

```

The container implementation can then be loaded from its realm
and used.

```

Class containerClass = containerRealm.loadClass( CONTAINER_CLASSNAME );
MyContainer container = (MyContainer) containerClass.newInstance();
Thread.currentThread().setContextClassLoader( containerRealm.getClassLoader() );
container.run();

```

Ideally, the container itself would be responsible for creating
a `ClassRealm` for each component that's loaded, and
importing the component contract interfaces into the component's
`ClassRealm` and using `loadClass(..)`
to gain entry into the sandboxed component realm.
