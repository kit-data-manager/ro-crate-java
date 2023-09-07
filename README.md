# ro-crate-java

[![Java CI with Gradle](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/gradle.yml)
[![Coverage Status](https://coveralls.io/repos/github/kit-data-manager/ro-crate-java/badge.svg)](https://coveralls.io/github/kit-data-manager/ro-crate-java)
[![CodeQL](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/codeql-analysis.yml)
[![Publish to Maven Central / OSSRH](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/publishRelease.yml/badge.svg)](https://github.com/kit-data-manager/ro-crate-java/actions/workflows/publishRelease.yml)

A Java library to create and modify RO-Crates.
The aim of this implementation is to **not** require too deep knowledge of the specification,
and avoiding crates which do not fully comply to the specification, at the same time.

## Use it in your application

- [Instructions for your build manager (e.g., Gradle, Maven, etc.)](https://central.sonatype.com/artifact/edu.kit.datamanager/ro-crate-java/1.1.0)
- [Quick-Start](#quick-start)
- [Adapting Specification Examples](#adapting-the-specification-examples)
- [Related Publications](https://publikationen.bibliothek.kit.edu/publikationslisten/get.php?referencing=all&external_publications=kit&lang=de&format=html&style=kit-3lines-title_b-authors-other&consider_suborganizations=true&order=desc%20year&contributors=%5B%5B%5B%5D%2C%5B%22p20751.105%22%5D%5D%5D&title_contains=crate)

## Build the library / documentation

Build and run tests: `./gradlew build`  
Build documentation: `./gradlew javadoc`

On Windows, replace `./gradlew` with `gradlew.bat`.

## RO-Crate Specification Compatibility

- ✅ Version 1.1
- 🛠️ Version 1.2-DRAFT
  - ✅ Reading and writing crates with additional profiles or specifications ([examples for reading](src/test/java/edu/kit/datamanager/ro_crate/reader/RoCrateReaderSpec12Test.java), [examples for writing](src/test/java/edu/kit/datamanager/ro_crate/writer/RoCrateWriterSpec12Test.java))
  - ✅ Adding profiles or other specifications to a crate ([examples](src/test/java/edu/kit/datamanager/ro_crate/crate/BuilderSpec12Test.java))

## Quick-start
### Example for a basic crate from [RO-Crate website](https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#ro-crate-metadata-file-descriptor)
```java
RoCrate roCrate = new RoCrateBuilder("name", "description").build();
```

### Example adding a File (Data Entity) and a context pair
```java
RoCrate roCrate = new RoCrateBuilder("name", "description")
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

Note: Adding an entity with an ID ("@id") which is already being used by another entity in the crate, the old entity will be overwritten.

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

A `DataEntity` and its subclasses can have a local file associated with them,
instead of one located on the web (which link is the ID of the data entity). Example:

Example adding file:
```java
new FileEntity.FileEntityBuilder()
    .setId("new_file.txt")
    .setSource(new File("file"))
    .addProperty("description", "my new file that I added")
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

### Writing Crate to folder or zip file

Writing to folder:
```java
RoCrateWriter folderRoCrateWriter = new RoCrateWriter(new FolderWriter());
folderRoCrateWriter.save(roCrate, "destination");
```

Writing to zip file:
```java
RoCrateWriter roCrateZipWriter = new RoCrateWriter(new ZipWriter());
roCrateZipWriter.save(roCrate, "destination");
```

More writing strategies can be implemented, if required.

### Reading / importing Crate from folder or zip

Reading from folder:
```java
RoCrateReader roCrateFolderReader = new RoCrateReader(new FolderReader());
RoCrate res = roCrateFolderReader.readCrate("source");
```

Reading from zip file:
```java
RoCrateReader roCrateFolderReader = new RoCrateReader(new ZipReader());
RoCrate crate = roCrateFolderReader.readCrate("source");
```

### RO-Crate Website (HTML preview file)
By setting the preview to an `AutomaticPreview`, the library will automatically create a preview using the [ro-crate-html-js](https://www.npmjs.com/package/ro-crate-html-js) tool.
It has to be installed using `npm install --global ro-crate-html-js` in order to use it.
If you want to use a custom-made preview, you can set it using the `CustomPreview` class. `AutomaticPreview` is currently **not** set by default.
```java
RoCrate roCrate = new RoCrateBuilder("name", "description")
    .setPreview(new AutomaticPreview())
    .build();
```

### RO-Crate validation (machine-readable crate profiles)
Right now, the only implemented way of validating a RO-crate is to use a [JSON-Schema](https://json-schema.org/) that the crates metadata JSON file should match. JSON-Schema is an established standard and therefore a good choice for a crate profile. Example:

```java
Validator validator = new Validator(new JsonSchemaValidation("./schema.json"));
boolean valid = validator.validate(crate);
```

## Adapting the specification examples

This section describes how to generate the [official specifications examples](https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#minimal-example-of-ro-crate). Each example first shows the ro-crate-metadata.json and, below that, the required Java code to generate it.

### [Minimal example](https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#minimal-example-of-ro-crate)

```json
{ "@context": "https://w3id.org/ro/crate/1.1/context", 
  "@graph": [

 {
    "@type": "CreativeWork",
    "@id": "ro-crate-metadata.json",
    "conformsTo": {"@id": "https://w3id.org/ro/crate/1.1"},
    "about": {"@id": "./"}
 },  
 {
    "@id": "./",
    "identifier": "https://doi.org/10.4225/59/59672c09f4a4b",
    "@type": "Dataset",
    "datePublished": "2017",
    "name": "Data files associated with the manuscript:Effects of facilitated family case conferencing for ...",
    "description": "Palliative care planning for nursing home residents with advanced dementia ...",
    "license": {"@id": "https://creativecommons.org/licenses/by-nc-sa/3.0/au/"}
 },
 {
  "@id": "https://creativecommons.org/licenses/by-nc-sa/3.0/au/",
  "@type": "CreativeWork",
  "description": "This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/au/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.",
  "identifier": "https://creativecommons.org/licenses/by-nc-sa/3.0/au/",
  "name": "Attribution-NonCommercial-ShareAlike 3.0 Australia (CC BY-NC-SA 3.0 AU)"
 }
 ]
}
```

Here, everything is created manually.
For the following examples, more convenient creation methods are used.

```java
  RoCrate crate = new RoCrate();

    ContextualEntity license = new ContextualEntity.ContextualEntityBuilder()
        .addType("CreativeWork")
        .setId("https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addProperty("description", "This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Australia License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/au/ or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.")
        .addProperty("identifier", "https://creativecommons.org/licenses/by-nc-sa/3.0/au/")
        .addProperty("name", "Attribution-NonCommercial-ShareAlike 3.0 Australia (CC BY-NC-SA 3.0 AU)")
        .build();

    crate.setRootDataEntity(new RootDataEntity.RootDataEntityBuilder()
        .addProperty("identifier", "https://doi.org/10.4225/59/59672c09f4a4b")
        .addProperty("datePublished", "2017")
        .addProperty("name", "Data files associated with the manuscript:Effects of facilitated family case conferencing for ...")
        .addProperty("description", "Palliative care planning for nursing home residents with advanced dementia ...")
        .setLicense(license)
        .build());

    crate.setJsonDescriptor(new ContextualEntity.ContextualEntityBuilder()
        .setId("ro-crate-metadata.json")
        .addType("CreativeWork")
        .addIdProperty("about", "./")
        .addIdProperty("conformsTo", "https://w3id.org/ro/crate/1.1")
        .build()
    );
    crate.addContextualEntity(license);
```

### [Example with files](https://www.researchobject.org/ro-crate/1.1/data-entities.html#example-linking-to-a-file-and-folders)

```json
{ "@context": "https://w3id.org/ro/crate/1.1/context",
  "@graph": [
    {
      "@type": "CreativeWork",
      "@id": "ro-crate-metadata.json",
      "conformsTo": {"@id": "https://w3id.org/ro/crate/1.1"},
      "about": {"@id": "./"}
    },  
    {
      "@id": "./",
      "@type": [
        "Dataset"
      ],
      "hasPart": [
        {
          "@id": "cp7glop.ai"
        },
        {
          "@id": "lots_of_little_files/"
        }
      ]
    },
    {
      "@id": "cp7glop.ai",
      "@type": "File",
      "name": "Diagram showing trend to increase",
      "contentSize": "383766",
      "description": "Illustrator file for Glop Pot",
      "encodingFormat": "application/pdf"
    },
    {
      "@id": "lots_of_little_files/",
      "@type": "Dataset",
      "name": "Too many files",
      "description": "This directory contains many small files, that we're not going to describe in detail."
    }
  ]
}
```

Here we use the inner builder classes for the construction of the crate.
Doing so, the Metadata File Descriptor and the Root Data Entity entities are added automatically.
`setSource()` is used to provide the actual location of these Data Entities (if they are not remote).
The Data Entity file in the crate will have the name of the entity's ID.

```java
  RoCrate crate = new RoCrate.RoCrateBuilder()
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("cp7glop.ai")
                .setSource(new File("path to file"))
                .addProperty("name", "Diagram showing trend to increase")
                .addProperty("contentSize", "383766")
                .addProperty("description", "Illustrator file for Glop Pot")
                .setEncodingFormat("application/pdf")
                .build()
        )
        .addDataEntity(
            new DataSetEntity.DataSetBuilder()
                .setId("lots_of_little_files/")
                .setSource(new File("path_to_files"))
                .addProperty("name", "Too many files")
                .addProperty("description", "This directory contains many small files, that we're not going to describe in detail.")
                .build()
        )
        .build();
```

### [Example with web resources](https://www.researchobject.org/ro-crate/1.1/data-entities.html#web-based-data-entities)

```json
{ "@context": "https://w3id.org/ro/crate/1.1/context",
  "@graph": [
    {
        "@type": "CreativeWork",
        "@id": "ro-crate-metadata.json",
        "conformsTo": {"@id": "https://w3id.org/ro/crate/1.1"},
        "about": {"@id": "./"}
  },  
  {
    "@id": "./",
    "@type": [
      "Dataset"
    ],
    "hasPart": [
      {
        "@id": "survey-responses-2019.csv"
      },
      {
        "@id": "https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf"
      },
      ]
  },
  {
    "@id": "survey-responses-2019.csv",
    "@type": "File",
    "name": "Survey responses",
    "contentSize": "26452",
    "encodingFormat": "text/csv"
  },
  {
    "@id": "https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf",
    "@type": "File",
    "name": "RO-Crate specification",
    "contentSize": "310691",
    "description": "RO-Crate specification",
    "encodingFormat": "application/pdf"
  }
]
}
```

The web resource does not use `.setSource()`, but uses the ID to indicate the file's location.

```java
 RoCrate crate = new RoCrate.RoCrateBuilder()
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("survey-responses-2019.csv")
                .setSource(new File("README.md"))
                .addProperty("name", "Survey responses")
                .addProperty("contentSize", "26452")
                .setEncodingFormat("text/csv")
                .build()
        )
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("https://zenodo.org/record/3541888/files/ro-crate-1.0.0.pdf")
                .addProperty("name", "RO-Crate specification")
                .addProperty("contentSize", "310691")
                .addProperty("description", "RO-Crate specification")
                .setEncodingFormat("application/pdf")
                .build()
        )
        .build();
```

### [Example with file, author, location](https://www.researchobject.org/ro-crate/1.1/appendix/jsonld.html)

```json
{ "@context": "https://w3id.org/ro/crate/1.1/context",
  "@graph": [

    {
      "@type": "CreativeWork",
      "@id": "ro-crate-metadata.json",
      "conformsTo": {"@id": "https://w3id.org/ro/crate/1.1"},
      "about": {"@id": "./"},
      "description": "RO-Crate Metadata File Descriptor (this file)"
    },
    {
      "@id": "./",
      "@type": "Dataset",
      "name": "Example RO-Crate",
      "description": "The RO-Crate Root Data Entity",
      "hasPart": [
        {"@id": "data1.txt"},
        {"@id": "data2.txt"}
      ]
    },


    {
      "@id": "data1.txt",
      "@type": "File",
      "description": "One of hopefully many Data Entities",
      "author": {"@id": "#alice"},
      "contentLocation":  {"@id": "http://sws.geonames.org/8152662/"}
    },
    {
      "@id": "data2.txt",
      "@type": "File"
    },

    {
      "@id": "#alice",
      "@type": "Person",
      "name": "Alice",
      "description": "One of hopefully many Contextual Entities"
    },
    {
      "@id": "http://sws.geonames.org/8152662/",
      "@type": "Place",
      "name": "Catalina Park"
    }
 ]
}
```

If there is no special method for including relative entities (ID properties) one can use `.addIdProperty("key","value")`.

```java
 PersonEntity alice = new PersonEntity.PersonEntityBuilder()
        .setId("#alice")
        .addProperty("name", "Alice")
        .addProperty("description", "One of hopefully many Contextual Entities")
        .build();
    PlaceEntity park = new PlaceEntity.PlaceEntityBuilder()
        .setId("http://sws.geonames.org/8152662/")
        .addProperty("name", "Catalina Park")
        .build();

    RoCrate crate = new RoCrate.RoCrateBuilder("Example RO-Crate", "The RO-Crate Root Data Entity")
        .addContextualEntity(park)
        .addContextualEntity(alice)
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("data2.txt")
                .setSource(new File(.......))
                .build()
        )
        .addDataEntity(
            new FileEntity.FileEntityBuilder()
                .setId("data1.txt")
                .setSource(new File(.......))
                .addProperty("description", "One of hopefully many Data Entities")
                .addAuthor(alice.getId())
                .addIdProperty("contentLocation", park)
                .build()
        )
        .build();

```
### [Example with computational workflow](https://www.researchobject.org/ro-crate/1.1/workflows.html#complete-workflow-example)

```json
{ "@context": "https://w3id.org/ro/crate/1.1/context", 
  "@graph": [
    {
      "@type": "CreativeWork",
      "@id": "ro-crate-metadata.json",
      "conformsTo": {"@id": "https://w3id.org/ro/crate/1.1"},
      "about": {"@id": "./"}
    },
    {
      "@id": "./",
      "@type": "Dataset",
      "hasPart": [
          { "@id": "workflow/alignment.knime" }
      ]
    },
    {
      "@id": "workflow/alignment.knime",  
      "@type": ["File", "SoftwareSourceCode", "ComputationalWorkflow"],
      "conformsTo": 
        {"@id": "https://bioschemas.org/profiles/ComputationalWorkflow/0.5-DRAFT-2020_07_21/"},
      "name": "Sequence alignment workflow",
      "programmingLanguage": {"@id": "#knime"},
      "creator": {"@id": "#alice"},
      "dateCreated": "2020-05-23",
      "license": { "@id": "https://spdx.org/licenses/CC-BY-NC-SA-4.0"},
      "input": [
        { "@id": "#36aadbd4-4a2d-4e33-83b4-0cbf6a6a8c5b"}
      ],
      "output": [
        { "@id": "#6c703fee-6af7-4fdb-a57d-9e8bc4486044"},
        { "@id": "#2f32b861-e43c-401f-8c42-04fd84273bdf"}
      ],
      "sdPublisher": {"@id": "#workflow-hub"},
      "url": "http://example.com/workflows/alignment",
      "version": "0.5.0"
    },
    {
      "@id": "#36aadbd4-4a2d-4e33-83b4-0cbf6a6a8c5b",
      "@type": "FormalParameter",
      "conformsTo": {"@id": "https://bioschemas.org/profiles/FormalParameter/0.1-DRAFT-2020_07_21/"},
      "name": "genome_sequence",
      "valueRequired": true,
      "additionalType": {"@id": "http://edamontology.org/data_2977"},
      "format": {"@id": "http://edamontology.org/format_1929"}
    },
    {
      "@id": "#6c703fee-6af7-4fdb-a57d-9e8bc4486044",
      "@type": "FormalParameter",
      "conformsTo": {"@id": "https://bioschemas.org/profiles/FormalParameter/0.1-DRAFT-2020_07_21/"},
      "name": "cleaned_sequence",
      "additionalType": {"@id": "http://edamontology.org/data_2977"},
      "encodingFormat": {"@id": "http://edamontology.org/format_2572"}
    },
    {
      "@id": "#2f32b861-e43c-401f-8c42-04fd84273bdf",
      "@type": "FormalParameter",
      "conformsTo": {"@id": "https://bioschemas.org/profiles/FormalParameter/0.1-DRAFT-2020_07_21/"},
      "name": "sequence_alignment",
      "additionalType": {"@id": "http://edamontology.org/data_1383"},
      "encodingFormat": {"@id": "http://edamontology.org/format_1982"}
    },
    {
      "@id": "https://spdx.org/licenses/CC-BY-NC-SA-4.0",
      "@type": "CreativeWork",
      "name": "Creative Commons Attribution Non Commercial Share Alike 4.0 International",
      "alternateName": "CC-BY-NC-SA-4.0"
    },
    {
      "@id": "#knime",
      "@type": "ProgrammingLanguage",
      "name": "KNIME Analytics Platform",
      "alternateName": "KNIME",
      "url": "https://www.knime.com/whats-new-in-knime-41",
      "version": "4.1.3"
    },
    {
      "@id": "#alice",
      "@type": "Person",
      "name": "Alice Brown"
    },
    {
      "@id": "#workflow-hub",
      "@type": "Organization",
      "name": "Example Workflow Hub",
      "url":"http://example.com/workflows/"
    },
    {
      "@id": "http://edamontology.org/format_1929",
      "@type": "Thing",
      "name": "FASTA sequence format"
    },
    {
      "@id": "http://edamontology.org/format_1982",
      "@type": "Thing",
      "name": "ClustalW alignment format"
    },
    {
      "@id": "http://edamontology.org/format_2572",
      "@type": "Thing",
      "name": "BAM format"
    },
    {
      "@id": "http://edamontology.org/data_2977",
      "@type": "Thing",
      "name": "Nucleic acid sequence"
    },
    {
      "@id": "http://edamontology.org/data_1383",
      "@type": "Thing",
      "name": "Nucleic acid sequence alignment"
    }
  ]
}
```


```java
   ContextualEntity license = new ContextualEntity.ContextualEntityBuilder()
        .addType("CreativeWork")
        .setId("https://spdx.org/licenses/CC-BY-NC-SA-4.0")
        .addProperty("name", "Creative Commons Attribution Non Commercial Share Alike 4.0 International")
        .addProperty("alternateName", "CC-BY-NC-SA-4.0")
        .build();
    ContextualEntity knime = new ContextualEntity.ContextualEntityBuilder()
        .setId("#knime")
        .addType("ProgrammingLanguage")
        .addProperty("name", "KNIME Analytics Platform")
        .addProperty("alternateName", "KNIME")
        .addProperty("url", "https://www.knime.com/whats-new-in-knime-41")
        .addProperty("version", "4.1.3")
        .build();
    OrganizationEntity workflowHub = new OrganizationEntity.OrganizationEntityBuilder()
        .setId("#workflow-hub")
        .addProperty("name", "Example Workflow Hub")
        .addProperty("url", "http://example.com/workflows/")
        .build();
    ContextualEntity fasta = new ContextualEntity.ContextualEntityBuilder()
        .setId("http://edamontology.org/format_1929")
        .addType("Thing")
        .addProperty("name", "FASTA sequence format")
        .build();
    ContextualEntity clustalW = new ContextualEntity.ContextualEntityBuilder()
        .setId("http://edamontology.org/format_1982")
        .addType("Thing")
        .addProperty("name", "ClustalW alignment format")
        .build();
    ContextualEntity ban = new ContextualEntity.ContextualEntityBuilder()
        .setId("http://edamontology.org/format_2572")
        .addType("Thing")
        .addProperty("name", "BAM format")
        .build();
    ContextualEntity nucSec = new ContextualEntity.ContextualEntityBuilder()
        .setId("http://edamontology.org/data_2977")
        .addType("Thing")
        .addProperty("name", "Nucleic acid sequence")
        .build();
    ContextualEntity nucAlign = new ContextualEntity.ContextualEntityBuilder()
        .setId("http://edamontology.org/data_1383")
        .addType("Thing")
        .addProperty("name", "Nucleic acid sequence alignment")
        .build();
    PersonEntity alice = new PersonEntity.PersonEntityBuilder()
        .setId("#alice")
        .addProperty("name", "Alice Brown")
        .build();
    ContextualEntity requiredParam = new ContextualEntity.ContextualEntityBuilder()
        .addType("FormalParameter")
        .setId("#36aadbd4-4a2d-4e33-83b4-0cbf6a6a8c5b")
        .addProperty("name", "genome_sequence")
        .addProperty("valueRequired", true)
        .addIdProperty("conformsTo", "https://bioschemas.org/profiles/FormalParameter/0.1-DRAFT-2020_07_21/")
        .addIdProperty("additionalType", nucSec)
        .addIdProperty("encodingFormat", fasta)
        .build();

    ContextualEntity clnParam = new ContextualEntity.ContextualEntityBuilder()
        .addType("FormalParameter")
        .setId("#6c703fee-6af7-4fdb-a57d-9e8bc4486044")
        .addProperty("name", "cleaned_sequence")
        .addIdProperty("conformsTo", "https://bioschemas.org/profiles/FormalParameter/0.1-DRAFT-2020_07_21/")
        .addIdProperty("additionalType", nucSec)
        .addIdProperty("encodingFormat", ban)
        .build();

    ContextualEntity alignParam = new ContextualEntity.ContextualEntityBuilder()
        .addType("FormalParameter")
        .setId("#2f32b861-e43c-401f-8c42-04fd84273bdf")
        .addProperty("name", "sequence_alignment")
        .addIdProperty("conformsTo", "https://bioschemas.org/profiles/FormalParameter/0.1-DRAFT-2020_07_21/")
        .addIdProperty("additionalType", nucAlign)
        .addIdProperty("encodingFormat", clustalW)
        .build();

    RoCrate crate = new RoCrate.RoCrateBuilder("Example RO-Crate", "The RO-Crate Root Data Entity")
        .addContextualEntity(license)
        .addContextualEntity(knime)
        .addContextualEntity(workflowHub)
        .addContextualEntity(fasta)
        .addContextualEntity(clustalW)
        .addContextualEntity(ban)
        .addContextualEntity(nucSec)
        .addContextualEntity(nucAlign)
        .addContextualEntity(alice)
        .addContextualEntity(requiredParam)
        .addContextualEntity(clnParam)
        .addContextualEntity(alignParam)
        .addDataEntity(
            new WorkflowEntity.WorkflowEntityBuilder()
                .setId("workflow/alignment.knime")
                .setSource(new File("src"))
                .addIdProperty("conformsTo", "https://bioschemas.org/profiles/ComputationalWorkflow/0.5-DRAFT-2020_07_21/")
                .addProperty("name", "Sequence alignment workflow")
                .addIdProperty("programmingLanguage", "#knime")
                .addAuthor("#alice")
                .addProperty("dateCreated", "2020-05-23")
                .setLicense("https://spdx.org/licenses/CC-BY-NC-SA-4.0")
                .addInput("#36aadbd4-4a2d-4e33-83b4-0cbf6a6a8c5b")
                .addOutput("#6c703fee-6af7-4fdb-a57d-9e8bc4486044")
                .addOutput("#2f32b861-e43c-401f-8c42-04fd84273bdf")
                .addProperty("url", "http://example.com/workflows/alignment")
                .addProperty("version", "0.5.0")
                .addIdProperty("sdPublisher", "#workflow-hub")
                .build()

        )
        .build();
```
