# RObachelor

Java library for managing RO-Crates.

#How to use

## Creating
### Example for a basic crate from [RO-Crate website](https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#ro-crate-metadata-file-descriptor)
```java
ROCrate roCrate = new ROCrate.ROCrateBuilder("name", "description").build();
```

### Example adding a File (Data Entity) and a context pair
```java
ROCrate roCrate = new ROCrate.ROCrateBuilder("name", "description")
    .addValuePairToContext("Station", "www.station.com")
    .addURLToContext("contextUrl")
    .addDataEntity(
      new FileEntity.FileEntityBuilder()
        .setId("survey-responses-2019.csv")
        .addProperty("name", "Survey responses")
        .addProperty("contentSize", "26452")
        .addProperty("encodingFormat", "text/csv")
        .build()
    )
    .addDataEntity()
    ...
    ...
    .addContextualEntity()
    ...
    ...
    .build();
```
(in order to add a whole context url you can use `.addURLToContext("url")`)

In the above example instead of a FileEntity, other supported DataEntities are:
1. DataSetEntity
2. FileEntity (used in the example above)
3. WorkflowEntity

Note: if you add an entity with @id that is already in the crate, the old one will be overwritten.

if you want to add something that is missing you can use **DataEntity** and add all the thinks you want example:
```java
new DataEntity.DataEntityBuilder()
    .addType("CreativeWork")
    .addId("ID")
    .addProperty("property from schema.org/Creativework", "value")
    .build();
```
Note that here you are supposed to add the type of your data entity because it is not known.

A DataEntity and its subclasses can have a local present file associated with them,
instead of one located in the web (which link is the id of the data entity).

Example adding file:
```java
new FileEntity.FileEntityBuilder()
    .setId("new_file.txt")
    .setLocation(new File("file"))
    .addProperty("description", "my new file that I added")
    .build();
```

### Contextual Entities
Contextual entities cannot be associated with a file (they are pure metadata).

To add a contextual entity ot a crate you use the function `.addContextualEntity(ContextualEntity entity)`
Some types of implemented entities are:
1. OrganizationEntity
2. PersonEntity
3. PlaceEntity

If you have to add another type of contextual entity you just use the base class `ContextualEntity`

The library provides for now a way to automatically create contextual entities from external providers (ex. [ORCID](https://orcid.org/) and [ROR](https://ror.org/))
using the classes (`ORCIDProvider` and `RORProvider`).
```java
PersonEntity person = ORCIDProvider.getPerson("https://orcid.org/*")
OrganizationEntity organization = RORProvider.getOrganization("https://ror.org/*");
```

### Folder or Zip writing 
1. Folder
```java
ROCrateWriter folderRoCrateWriter = new ROCrateWriter(new FolderWriter());
folderRoCrateWriter.save(roCrate, "destination");
```
2. Zip
```java
ROCrateWriter roCrateZipWriter = new ROCrateWriter(new ZipWriter());
roCrateZipWriter.save(roCrate, "destination"); 
```

### Folder or Zip reading (importing)
1. Folder
```java
ROCrateReader roCrateFolderReader = new ROCrateReader(new FolderReader());
ROCrate res = roCrateFolderReader.readCrate("source");
```
2. Zip
```java
ROCrateReader roCrateFolderReader = new ROCrateReader(new ZipReader());
ROCrate crate = roCrateFolderReader.readCrate("source");
```

### RO-Crate Website
By setting the preview to an `AutomaticPreview` the library will automatically create
a preview using the [ro-crate-html-js](https://www.npmjs.com/package/ro-crate-html-js) tool.
It has to be installed using `npm install ro-crate-html-js` in order to use it.
If you want to use a custom-made preview you can set it using the `CustomPreview` class.
(AutomaticPreview is not set by default)
```java
ROCrate roCrate = new ROCrate.ROCrateBuilder("name", "description")
    .setPreview(new AutomaticPreview())
    .build();
```

### RO-Crate validation
Right now the only implemented way of validating a RO-crate is to use a json-schema that the crate json should match.
```java
Validator validator = new Validator(new JsonSchemaValidation(String locationOfSchema));
bool valid = validator.validate(crate);
```