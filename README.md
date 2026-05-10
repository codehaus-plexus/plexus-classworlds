Plexus-Classworlds
==================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/codehaus-plexus/plexus-classworlds.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.plexus/plexus-classworlds.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.plexus/plexus-classworlds)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/codehaus/plexus/plexus-classworlds/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/codehaus/plexus/plexus-classworlds/README.md)
![Build Status](https://github.com/codehaus-plexus/plexus-classworlds/workflows/GitHub%20CI/badge.svg)

Current master is now at https://github.com/codehaus-plexus/plexus-classworlds

Backward Compatibility
----------------------

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
