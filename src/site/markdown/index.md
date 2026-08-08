---
title: Plexus Classworlds
author: bob mcwhirter
---

# Overview

**Plexus Classworlds** is a framework for container developers
who require complex manipulation of Java's ClassLoaders.  Java's
native ClassLoader mechanisms and classes can cause
much headache and confusion for certain types of application
developers.  Projects which involve dynamic loading of components
or otherwise represent a 'container' can benefit from the classloading
control provided by **Classworlds**.

**Plexus Classworlds** provides a richer set of semantics for
class loading than Java's normal mechanisms, while still being
able to provide a ClassLoader interface to integrate
seamlessly with the Java environment.

The **Classworlds** model does away with the hierarchy
normally associated with ClassLoaders.  Instead,
[`ClassWorld`](apidocs/index.html?org/codehaus/plexus/classworlds/ClassWorld.html) provides a
pool of [`ClassRealms`](apidocs/index.html?org/codehaus/plexus/classworlds/realm/ClassRealm.html)
which can import arbitrary packages from other ClassRealms.
Effectively, **Classworlds** turns the old-style
hierarchy into a directed graph.

In a application container environment, the container may
have a realm capable of loading on the container/component
contract interfaces and classes.  Another realm is created
for each component which imports the contract classes from
the container realm.

This model allows for fine-grained control of which
classloader loads any particular class.  This form of
partial isolation can reduce the myriad strange errors
that are produced by loading classes from multiple
loaders.

In addition, **Plexus Classworlds** provides a
[launcher](launcher.html)
to assist in the creation of classloaders and `ClassRealm`s
from a configuration file and the launching of the application's `main`
method from the correct class loaded through the correct classloader.

And for seamless transition from [older **Classworlds** up to 1.1](https://github.com/codehaus/classworlds),
**Plexus Classworlds** provides
[a compatibility layer](apidocs/index.html?org/codehaus/classworlds/package-summary.html).
This package is deprecated for new code but must remain on the classpath because
`org.eclipse.sisu:org.eclipse.sisu.plexus` references it in its compiled bytecode.
See [COMPATIBILITY.md](https://github.com/codehaus-plexus/plexus-classworlds/blob/master/COMPATIBILITY.md)
for details.
