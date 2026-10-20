# ro-crate-java

[![Java CI with Gradle](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/gradle.yml)
[![Coverage Status](https://coveralls.io/repos/github/kit-data-manager/ro-crate-java/badge.svg)](https://coveralls.io/github/kit-data-manager/ro-crate-java)
[![CodeQL](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/codeql-analysis.yml)
[![Publish to Maven Central / OSSRH](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/publishRelease.yml/badge.svg)](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/publishRelease.yml)

A Java library to create and modify RO-Crates.
The aim of this implementation is to **not** require too deep knowledge of the specification,
and avoiding crates which do not fully comply to the specification, at the same time.

## Use it in your application

- [Instructions for your build manager (e.g., Gradle, Maven, etc.)](https://central.sonatype.com/artifact/edu.kit.datamanager/ro-crate-java)
- [Quick-Start](#quick-start)
- [Migration guide: from the Jackson-based 2.x API to the Jackson-free core](MIGRATION.md)
- [JavaDoc Documentation](https://javadoc.io/doc/edu.kit.datamanager/ro-crate-java)
- [Related Publications](https://publikationen.bibliothek.kit.edu/publikationslisten/get.php?referencing=all&external_publications=kit&lang=de&format=html&style=kit-3lines-title_b-authors-other&consider_suborganizations=true&order=desc%20year&contributors=%5B%5B%5B%5D%2C%5B%22p20751.105%22%5D%5D%5D&title_contains=crate)

## Modules

The library is split into a Jackson-free core plus two JSON backend modules:

- **`core`** — the crate model, reader/writer and validation API, working on a
  neutral JSON tree model (`edu.kit.datamanager.ro_crate.json`). It has no
  dependency on any JSON library. JSON (de-)serialization and JSON-schema
  validation are plugged in via the `JsonAdapter` and `ValidatorStrategy`
  service-provider interfaces, discovered with Java's `ServiceLoader`:
  exactly one backend must be on the classpath.
- **`jackson2`** — the Jackson 2 backend. It publishes under the historical
  coordinates `edu.kit.datamanager:ro-crate-java`, so existing consumers keep
  resolving the same artifact; it transitively pulls in the core.
- **`jackson3`** — the Jackson 3 backend (`tools.jackson`), published as
  `edu.kit.datamanager:ro-crate-java-jackson3`.

To use the Jackson 3 backend, depend on the Jackson 3 artifact instead of the
historical one, e.g. in Gradle:

```groovy
implementation "edu.kit.datamanager:ro-crate-java-jackson3:<version>"
```

No further configuration is needed: the backend registers itself via
`META-INF/services`. At runtime, the core's JSON and validation entry points
(the `edu.kit.datamanager.ro_crate.json.Json` and
`edu.kit.datamanager.ro_crate.validation.Validation` classes) discover it with
`ServiceLoader`, so they use whichever backend is on the classpath. If you do
not care about the JSON backend, do nothing: keep depending on
`edu.kit.datamanager:ro-crate-java` as before — the backend comes with it.

## Build the library / documentation

- Building (with tests): `./gradlew clean build`
- Building (without tests): `./gradlew clean build -x test`
- Building with release profile: `./gradlew -Dprofile=release clean build`
- Doing a release: `./gradlew -Dprofile=release clean build release`
    - Will prompt you about version number to use and next version number
    - Will make a git tag which can later be used in a GitHub release
        - A GitHub release will trigger the CI for publication. See also `.github/workflows/publishRelease.yml`.
- Build documentation: `./gradlew javadoc`

On Windows, replace `./gradlew` with `gradlew.bat`.

## Examples

The `examples` module contains small, self-contained example programs. Each
example exists twice: once written against the current API (run with either
backend) and once against the released 2.1.2 artifact (`examples/src/legacy`).
A parity test compares the outputs byte-for-byte, so the examples double as a
behavior-comparison suite between the released version and the current
development state.

- Run every variant and compare: `./gradlew :examples:test`
- Run a single variant: `./gradlew :examples:runLegacyCreateMinimalCrate`,
  `./gradlew :examples:runCurrent-jackson2CreateMinimalCrate`,
  `./gradlew :examples:runCurrent-jackson3CreateMinimalCrate`
- Outputs are written to `examples/build/examples/<variant>/<example>.txt`
- List available example tasks: `./gradlew :examples:tasks --group examples`

Each variant runs in its own JVM instance, since the JSON backends register via
`META-INF/services` and must not share a classpath.

## RO-Crate Specification Compatibility

- ✅ [Version 1.1](https://www.researchobject.org/ro-crate/1.1/) ([Extracted examples as well-described unit tests/guide](core/src/test/java/edu/kit/datamanager/ro_crate/examples/ExamplesOfSpecificationV1p1Test.java))
- 🛠️ Version 1.2-DRAFT
    - ✅ Reading and writing crates with additional profiles or specifications ([examples for reading](core/src/test/java/edu/kit/datamanager/ro_crate/reader/RoCrateReaderSpec12Test.java), [examples for writing](core/src/test/java/edu/kit/datamanager/ro_crate/writer/RoCrateWriterSpec12Test.java))
    - ✅ Adding profiles or other specifications to a crate ([examples](core/src/test/java/edu/kit/datamanager/ro_crate/crate/BuilderSpec12Test.java))

## Quick-start

` ro-crate-java` makes use of the builder pattern to guide the user to create a valid RO-Crate, similar to:

```java
RoCrate myFirstCrate = STARTER_CRATE
    .addDataEntity(
        new FileEntity.FileEntityBuilder()
            .setId("path/within/crate/survey-responses-2019.csv")
            .setLocation(Paths.get("path/to/current/location/experiment.csv"))
            .addProperty("name", "Survey responses")
            .addProperty("contentSize", "26452")
            .addProperty("encodingFormat", "text/csv")
            .build()
    )
    .addDataEntity(/*...*/)
    .addContextualEntity(/*...*/)
    .build();
```

A built or imported crate can of course also be modified afterwards. Take a look at our further documentation:

- **There is a well-documented example-driven guide in [LearnByExampleTest.java](core/src/test/java/edu/kit/datamanager/ro_crate/examples/LearnByExampleTest.java) to help you get started.**
- You may also be interested in the examples we extracted from the [specification in version 1.1](https://www.researchobject.org/ro-crate/1.1/), which are available in [ExamplesOfSpecificationV1p1Test.java](core/src/test/java/edu/kit/datamanager/ro_crate/examples/ExamplesOfSpecificationV1p1Test.java).
- There is a [module with all well-described guiding tests](core/src/test/java/edu/kit/datamanager/ro_crate/examples/) available.
- The [JavaDoc Documentation](https://javadoc.io/doc/edu.kit.datamanager/ro-crate-java) is also available online.
