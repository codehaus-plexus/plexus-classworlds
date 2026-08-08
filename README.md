# Plexus Classworlds

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.plexus/plexus-classworlds.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.plexus/plexus-classworlds)
[![GitHub CI](https://github.com/codehaus-plexus/plexus-classworlds/workflows/GitHub%20CI/badge.svg)](https://github.com/codehaus-plexus/plexus-classworlds/actions)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/codehaus/plexus/plexus-classworlds/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/codehaus/plexus/plexus-classworlds/README.md)
[![License](https://img.shields.io/github/license/codehaus-plexus/plexus-classworlds.svg?label=License)](https://www.apache.org/licenses/LICENSE-2.0)

A classloader framework for container developers who need more than Java's built-in hierarchy.

Classworlds replaces the strict parent-child classloader tree with a pool of **realms** that can import
specific packages from each other — a directed graph rather than a hierarchy. That is how Maven keeps a
plugin's dependencies from colliding with its own, and how it loads plugins in isolation from one another.

It also ships a [launcher](https://codehaus-plexus.github.io/plexus-classworlds/launcher.html) that builds
the realms from a configuration file and invokes an application's `main` method in the right one. Maven's
own startup scripts use it.

## Status

Maintained, conservatively. Maven's startup depends on this, so changes are deliberate and public API is
kept compatible.

## Using it

```xml
<dependency>
  <groupId>org.codehaus.plexus</groupId>
  <artifactId>plexus-classworlds</artifactId>
  <version>2.12.0</version>
</dependency>
```

Check the badge above for the current version.

## Requirements

Java 8 or later.

## Backward Compatibility

This project maintains a legacy compatibility layer in the `org.codehaus.classworlds` package.
This package **cannot be removed** without coordinating with the Eclipse Sisu and Apache Maven teams.

The compiled bytecode of `org.eclipse.sisu:org.eclipse.sisu.plexus` directly references
`org.codehaus.classworlds.ClassRealm`, `ClassRealmAdapter`, and `ClassRealmReverseAdapter`.
Removing this package causes `ClassNotFoundException` at runtime for any application using Sisu
(which includes all Maven 3+ builds).

PR #141 removed this package and had to be reverted immediately (see commit 223416e).

New code should use the `org.codehaus.plexus.classworlds` package.
See [COMPATIBILITY.md](COMPATIBILITY.md) for the full picture, including what Sisu references
and what is required before any future removal can be considered.

## Documentation

- [Project site](https://codehaus-plexus.github.io/plexus-classworlds/) — including [API usage](https://codehaus-plexus.github.io/plexus-classworlds/apiusage.html) and the [launcher](https://codehaus-plexus.github.io/plexus-classworlds/launcher.html)
- [Javadoc](https://javadoc.io/doc/org.codehaus.plexus/plexus-classworlds)
- [Release notes](https://github.com/codehaus-plexus/plexus-classworlds/releases)

## Contributing

See [CONTRIBUTING.md](https://github.com/codehaus-plexus/.github/blob/master/CONTRIBUTING.md). In short:
`mvn verify` builds, and run `mvn spotless:apply` before pushing or CI will fail on formatting.

Please report security vulnerabilities privately — see
[SECURITY.md](https://github.com/codehaus-plexus/.github/blob/master/SECURITY.md), not a public issue.
