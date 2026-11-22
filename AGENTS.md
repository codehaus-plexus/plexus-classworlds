# Agent guide for plexus-classworlds

`org.codehaus.plexus:plexus-classworlds` (`bundle` packaging) is the classloader framework Maven
starts with: realms form a graph that imports specific packages from one another, and the launcher
builds those realms from a configuration file before invoking an application's `main`.
`COMPATIBILITY.md` explains why the legacy `org.codehaus.classworlds` classes must stay for Sisu's
`ClassRealmConverter`; treat that as a binary contract, not dead code. Requires Java 8 or later
(`javaVersion` 8 from the `plexus` parent), and the JAR sets
`Automatic-Module-Name: org.codehaus.plexus.classworlds`; keep that name stable.

## Build and test

- `mvn verify` builds. CI calls the shared `codehaus-plexus/.github` build workflow (default
  `mvn verify javadoc:javadoc`, on the shared JDK and operating system matrix) and then the shared
  deploy workflow.
- Run `mvn spotless:apply` on JDK 17 to 21 before pushing, or the formatting check fails.
- Unit tests use JUnit 5. Surefire enables assertions (`-ea`) only for `org.codehaus.classworlds`
  and `org.codehaus.plexus.classworlds`, uses `forkCount` 1 and redirects test output to files; do
  not drop those settings.
- `maven-dependency-plugin` copies Ant, log4j-api and jakarta.xml.bind-api test fixtures in
  `generate-test-resources`; keep those items when adding tests that depend on them.
- The configuration files and JARs under `src/test/test-data` are test fixtures that tests read by
  name; do not tidy them as documentation.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- Realm or `ClassWorld` API behavior: its Javadoc, and `src/site/markdown/apiusage.md`. This includes
  which packages and classes a realm sees through imports and its parent.
- Launcher configuration syntax or startup behavior: `src/site/markdown/launcher.md`.
- The overview, or the legacy `org.codehaus.classworlds` layer: `src/site/markdown/index.md` and
  `COMPATIBILITY.md`; keep the Backward Compatibility section of `README.md` in step.
- A page added or removed: the menu in `src/site/site.xml`.
- The Java requirement, or how to build and release: `README.md`.
- A new convention, build gate or test fixture: this file.

