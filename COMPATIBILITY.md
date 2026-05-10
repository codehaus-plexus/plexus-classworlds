# Legacy `org.codehaus.classworlds` Compatibility Layer

## Summary

The `org.codehaus.classworlds` package is the original Classworlds 1.x API (pre-2010). It has been
deprecated since 2014 (version 2.5.2) in favour of `org.codehaus.plexus.classworlds`. It cannot
be removed without coordinating with the Eclipse Sisu and Apache Maven teams.

## Why it cannot simply be deleted

In April 2026, PR #141 removed this package after 12 years of deprecation, expecting that all
consumers had migrated. The change was reverted within days (commit 223416e) because:

**`org.eclipse.sisu:org.eclipse.sisu.plexus` (the DI framework used by Maven 3+) contains compiled
bytecode that directly references classes in this package.**

Inspecting `org.eclipse.sisu.plexus-1.0.0.jar`:

```
org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter
  references:
    org/codehaus/classworlds/ClassRealm
    org/codehaus/classworlds/ClassRealmAdapter
    org/codehaus/classworlds/ClassRealmReverseAdapter
```

`ClassRealmConverter` is a core Sisu component that handles injection of `ClassRealm` instances
into Plexus components. Without these three classes on the classpath at runtime, any Maven 3+
build or any application embedding Sisu fails with `ClassNotFoundException` or
`NoClassDefFoundError` before it can do any useful work.

This is a **binary dependency on a released artifact**, not a source-level dependency. The fix
cannot be on the plexus-classworlds side alone.

## What is in the compatibility layer

```
org.codehaus.classworlds
├── ClassRealm                    (interface)          <- referenced by Sisu
├── ClassRealmAdapter             (wraps new → old)    <- referenced by Sisu
├── ClassRealmReverseAdapter      (wraps old → new)    <- referenced by Sisu
├── ClassWorld                    (interface)
├── ClassWorldAdapter             (wraps new → old)
├── ClassWorldReverseAdapter      (wraps old → new)
├── DefaultClassRealm             (adapter impl)
├── ClassWorldException
├── DuplicateRealmException
├── NoSuchRealmException
├── ConfigurationException
├── Configurator
├── ConfiguratorAdapter
├── Launcher
├── BytesURLConnection
└── BytesURLStreamHandler
```

The adapter classes use the standard Adapter pattern to bridge between the old interface and the
modern `org.codehaus.plexus.classworlds.realm.ClassRealm` implementation.

## Rules for working with this package

Do not remove classes or change any public API in `org.codehaus.classworlds`.

Do not add new API to this package. Any new features belong in `org.codehaus.plexus.classworlds`.

Before any release that touches this package, verify with the Sisu test suite:

```bash
mvn clean install                          # build plexus-classworlds locally
cd /path/to/sisu.plexus
mvn clean test -Dplexus-classworlds.version=<snapshot>
```

## Removing this package in the future

Removal requires the following preconditions to all be true:

1. A release of `org.eclipse.sisu:org.eclipse.sisu.plexus` ships that no longer references
   `org.codehaus.classworlds.*` in its bytecode.
2. That Sisu release has been adopted by a stable Apache Maven release.
3. The Maven release is widely deployed (typically means it shipped in at least two Maven minor
   versions and toolchains like Maven Wrapper have picked it up).

Until those conditions are met, this package stays.

When those conditions are met, the removal should be done in a **major version** of
plexus-classworlds with a clear upgrade notice in the release notes.

## References

- Original PR that removed the package: https://github.com/codehaus-plexus/plexus-classworlds/pull/141
- Original removal issue: https://github.com/codehaus-plexus/plexus-classworlds/issues/130
- Revert commit: 223416e
- Eclipse Sisu: https://github.com/eclipse/sisu.plexus

