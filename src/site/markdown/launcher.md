---
title: App Launching
author: bob mcwhirter
---

# Launcher Introduction

## Purpose

In order to reduce the number of classloading projects,
**Plexus Classworlds** replaces forehead
for application launching.

The main problems to solve in application launching include
locating all of application's JARs, configuring the initial
classloaders, and invoking the `main` entry method.

The [launcher facilities](apidocs/index.html?org/codehaus/plexus/classworlds/launcher/package-summary.html)
of **Classworlds** simplify
the process of locating application jars.  A common idiom is
to have a script which starts the JVM with only the
`plexus-classworlds.jar` in the classpath and a system
property to specify the location of a launcher configuration.
Additionally, typically a property specifying the application installation
location is passed on the command-line.

```

$JAVA_HOME/bin/java \
    -classpath $APP_HOME/boot/plexus-classworlds-2.5.2.jar \
    -Dclassworlds.conf=$APP_HOME/etc/classworlds.conf \
    -Dapp.home=$APP_HOME \
    org.codehaus.plexus.classworlds.launcher.Launcher \
    $*

```

# Configuration

## Entry Point Definition

The entry-point class and realm must be specified
using the `main is` directive before
specifying realm definitions.

```

main is com.werken.projectz.Server from app

```

## System Properties Definition

System properties can be set before and after the entry point, but before realms:

```

set <property> [[using <properties filename>]] [[default <default value>]]

```

## Realm Definitions

At least one **Classworlds** realm must be defined
within the configuration file.  The syntax for starting a
realm definition is `[realm.name]`.  All lines
following the realm header are considered directives for
that realm.  The realm definition continues either until
another realm is defined or until the end of the file is
reached.

```

[realm.one]
    ...
    ...
[realm.two]
    ...
    ...
[realm.three]
    ...
    ...

```

Within a realm definition, three directives are available:
`load`, `optionally` and `import`.

The `load` and `optionally`
directives specify a class source to be used for loading
classes in the realm: the only difference is that in case of absent source,
`load` fails but `optionally` does not.
Any loaded source that contain a star (`*`) in the file name is
replaced by the list of files that match the filename prefix and suffix.
System properties may be referred to using `${propname}` notation.
The `load` and `optionally` directives are equivalent to the
`addURL(..)` method of `ClassRealm`.

```

[app]
    load ${app.home}/lib/*.jar
    optionally ${app.home}/lib/ext/*.jar
    load ${tools.jar}

```

The `import` directive specifies that certain
packages should be imported and loaded by way of another
realm.  The `import` directive is equivalent
to the `importFrom(..)` method of
`ClassRealm`.

```

[app]
    ...
  
[subcomponent]
    import com.werken.projectz.Foo from app
    ...

```

## Entry point methods

**Classworlds** can be used to invoke any existing
application's `main()` method.  Using the standard
entry point does not allow for gaining access to the
`ClassWorld` of the application, but not all
applications will need it at run-time.

For those applications that do require the `ClassWorld`
instance, an alternative entry-point method signature can be
provided.  Simply add a `ClassWorld` parameter to
the standard `main` parameter list.

```

public class MyApp
{
    public static void main( String[] args, ClassWorld world )
    {
        ...     
    }
}

```
