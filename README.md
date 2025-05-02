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
- [Adapting Specification Examples](#adapting-the-specification-examples)
- [Related Publications](https://publikationen.bibliothek.kit.edu/publikationslisten/get.php?referencing=all&external_publications=kit&lang=de&format=html&style=kit-3lines-title_b-authors-other&consider_suborganizations=true&order=desc%20year&contributors=%5B%5B%5B%5D%2C%5B%22p20751.105%22%5D%5D%5D&title_contains=crate)

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

## RO-Crate Specification Compatibility

- ✅ Version 1.1
- 🛠️ Version 1.2-DRAFT
  - ✅ Reading and writing crates with additional profiles or specifications ([examples for reading](src/test/java/edu/kit/datamanager/ro_crate/reader/RoCrateReaderSpec12Test.java), [examples for writing](src/test/java/edu/kit/datamanager/ro_crate/writer/RoCrateWriterSpec12Test.java))
  - ✅ Adding profiles or other specifications to a crate ([examples](src/test/java/edu/kit/datamanager/ro_crate/crate/BuilderSpec12Test.java))

## Quick-start
### Example for a basic crate from [RO-Crate website](https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#ro-crate-metadata-file-descriptor)
```java
RoCrate roCrate = new RoCrateBuilder("name", "description", "datePublished", "licenseIdentifier").build();
```

### Example adding a File (Data Entity) and a context pair
```java
RoCrate roCrate = new RoCrateBuilder("name", "description", "datePublished", "licenseIdentifier")
    .addValuePairToContext("Station", "www.station.com")
    .addUrlToContext("contextUrl")
    .addDataEntity(
      new FileEntity.FileEntityBuilder()
        .setId("survey-responses-2019.csv")
        .addProperty("name", "Survey responses")
        .addProperty("contentSize", "26452")
        .addProperty("encodingFormat", "text/csv")
        .build()
    )
    .addDataEntity(...)
    ...
    .addContextualEntity(...)
    ...
    .build();
```

The library currently comes with three specialized DataEntities:

1. `DataSetEntity`
2. `FileEntity` (used in the example above)
3. `WorkflowEntity`

If another type of `DataEntity` is required, the base class `DataEntity` can be used. Example:
```java
new DataEntity.DataEntityBuilder()
    .addType("CreativeWork")
    .setId("ID")
    .addProperty("property from schema.org/Creativework", "value")
    .build();
```
Note that here you are supposed to add the type of your `DataEntity` because it is not known.

A `DataEntity` and its subclasses can have a file located on the web. Example:

Example adding file:
```java
new FileEntity.FileEntityBuilder()
    .addContent(URI.create("https://github.com/kit-data-manager/ro-crate-java/issues/5"))
    .addProperty("description", "my new file that I added")
    .build();
```

A `DataEntity` and its subclasses can have a local file associated with them,
instead of one located on the web (which link is the ID of the data entity). Example:

Example adding file:
```java
new FileEntity.FileEntityBuilder()
    .addContent(Paths.get("file"), "new_file.txt")
    .addProperty("description", "my new local file that I added")
    .build();
```

### Contextual Entities

Contextual entities cannot be associated with a file (they are pure metadata).

To add a contextual entity to a crate you use the function `.addContextualEntity(ContextualEntity entity)`.
Some types of derived/specializes entities are:
1. `OrganizationEntity`
2. `PersonEntity`
3. `PlaceEntity`

If you need another type of contextual entity, use the base class `ContextualEntity`.

The library provides a way to automatically create contextual entities from external providers. Currently, support for [ORCID](https://orcid.org/) and [ROR](https://ror.org/) is implemented. Example:
```java
PersonEntity person = ORCIDProvider.getPerson("https://orcid.org/*")
OrganizationEntity organization = RORProvider.getOrganization("https://ror.org/*");
```

### Writing Crate to folder, zip file, or zip stream

Writing to folder:
```java
RoCrateWriter folderRoCrateWriter = new RoCrateWriter(new FolderWriter());
folderRoCrateWriter.save(roCrate, "destinationFolder");
```

Writing to zip file:
```java
RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
roCrateZipWriter.save(roCrate, "destinationFolder");
```

Writing to zip stream:
```java
RoCrateWriter roCrateZipStreamWriter = new RoCrateWriter(new ZipStreamWriter());
roCrateZipStreamWriter.save(roCrate, outputStream);
```

More writing strategies can be implemented, if required.

### Reading / importing Crate from folder or zip

Reading from folder:
```java
RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
RoCrate res = roCrateFolderReader.readCrate("destinationFolder");
```

Reading from zip file:
```java
RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipReader());
RoCrate crate = roCrateFolderReader.readCrate("sourceZipFile");
```

Reading from zip stream:
```java
RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipStreamReader());
RoCrate crate = roCrateFolderReader.readCrate(inputStream);
```

### RO-Crate Website (HTML preview file)
ro-crate-java offers tree different kinds of previews:

* AutomaticPreview: Uses third-party library [ro-crate-html-js](https://www.npmjs.com/package/ro-crate-html-js), which must be installed separately. 
* CustomPreview: Pure Java-based preview using an included template processed by the FreeMarker template engine. At the same time, CustomPreview is the fallback for AutomaticPreview if ro-crate-html-js is not installed.
* StaticPreview: Allows to provide a static HTML page (including additional dependencies, e.g., CSS, JS) which is then shipped with the RO-Crate. 

When creating a new RO-Crate using the builder, the default setting is to use CustomPreview. If you want to change this behaviour, thr preview method is set as follows: 

```java
RoCrate roCrate = new RoCrateBuilder("name", "description", "datePublished", "licenseIdentifier")
    .setPreview(new AutomaticPreview())
    .build();
```

Keep in mind that, if you want to use AutomaticPreview, you have to install ro-crate-html-js via `npm install --global ro-crate-html-js` first. 

For StaticPreview, the constuctor is a bit different, such that it looks as follows: 

```java
File pathToMainPreviewHtml = new File("localPath");
File pathToAdditionalFiles = new File("localFolder");
RoCrate roCrate = new RoCrateBuilder("name", "description", "datePublished", "licenseIdentifier")
    .setPreview(new StaticPreview(pathToMainPreviewHtml, pathToAdditionalFiles))
    .build();
```

### RO-Crate validation (machine-readable crate profiles)
Right now, the only implemented way of validating a RO-crate is to use a [JSON-Schema](https://json-schema.org/) that the crates metadata JSON file should match. JSON-Schema is an established standard and therefore a good choice for a crate profile. Example:

```java
Validator validator = new Validator(new JsonSchemaValidation("./schema.json"));
boolean valid = validator.validate(crate);
```

## Adapting the specification examples

We have an [example module with unit tests](src/test/java/edu/kit/datamanager/ro_crate/example/), describing how to generate the official [examples from the specification](https://www.researchobject.org/ro-crate/1.1/).
Specifically, the examples for the specification in version 1.1 are available in [ExamplesOfSpecificationV1p1Test.java](src/test/java/edu/kit/datamanager/ro_crate/examples/ExamplesOfSpecificationV1p1Test.java).
