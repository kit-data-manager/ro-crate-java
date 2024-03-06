package edu.kit.datamanager.ro_crate.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.context.CrateMetadataContext;
import edu.kit.datamanager.ro_crate.context.RoCrateMetadataContext;
import edu.kit.datamanager.ro_crate.entities.contextual.ContextualEntity;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.RootDataEntity;
import edu.kit.datamanager.ro_crate.special.UriUtil;
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class allows reading crates from the outside into the library in order
 * to inspect or modify it.
 * 
 * The class takes a strategy to support different ways of importing the crates.
 * (from zip, folder, etc.)
 */
public class RoCrateReader {

  private final ReaderStrategy reader;

  public RoCrateReader(ReaderStrategy reader) {
    this.reader = reader;
  }

  /**
   * This function will read the location (using one of the specified strategies) and then
   * build the relation between the entities.
   *
   * @param location the location of the ro-crate to be read
   * @return the read RO-crate
   */
  public RoCrate readCrate(String location) {
    RoCrate crate = new RoCrate();
    // get the ro-crate-medata.json
    ObjectNode metadataJson = reader.readMetadataJson(location);
    // get the content of the crate
    File files = reader.readContent(location);

    // this set will contain the files that are associated with entities
    HashSet<String> usedFiles = new HashSet<>();
    usedFiles.add(new File(location).toPath().resolve("ro-crate-metadata.json").toFile().getPath());
    usedFiles.add(new File(location).toPath().resolve("ro-crate-preview.html").toFile().getPath());
    usedFiles.add(new File(location).toPath().resolve("ro-crate-preview_files").toFile().getPath());

    JsonNode context = metadataJson.get("@context");

    CrateMetadataContext crateContext = new RoCrateMetadataContext(context);
    crate.setMetadataContext(crateContext);
    JsonNode graph = metadataJson.get("@graph");

    if (graph.isArray()) {

      graph = setRootEntities(crate, (ArrayNode) graph);
      for (JsonNode node : graph) {
        // if the id is in the root hasPart list, we know this entity is a data entity
        RootDataEntity root = crate.getRootDataEntity();
        if (root != null && root.hasInHasPart(node.get("@id").asText())) {
          File loc = checkFolderHasFile(node.get("@id").asText(), files);
          if (loc != null) {
            usedFiles.add(loc.getPath());
          }
          // data entity
          DataEntity dataEntity = new DataEntity.DataEntityBuilder()
              .setAll(node.deepCopy())
              .setSource(loc)
              .build();
          crate.addDataEntity(dataEntity, false);
        } else {
          // contextual entity
          crate.addContextualEntity(
              new ContextualEntity.ContextualEntityBuilder().setAll(node.deepCopy()).build());
        }
      }
    }
    var itr = files.listFiles();
    List<File> list = new ArrayList<>();
    for (var f : itr) {
      if (!usedFiles.contains(f.getPath())) {
        list.add(f);
      }
    }
    crate.setUntrackedFiles(list);
    Validator defaultValidation = new Validator(new JsonSchemaValidation());
    defaultValidation.validate(crate);
    return crate;
  }

  private File checkFolderHasFile(String id, File file) {
    Path path = file.toPath().resolve(UriUtil.decode(id).get());
    if (path.toFile().exists()) {
      return path.toFile();
    }
    return null;
  }

  // gets the entities that every crate should have
  // we will need the root dataset to distinguish between data entities and contextual entities
  private ArrayNode setRootEntities(RoCrate crate, ArrayNode graph) {

    // for now, we make an empty ArrayNode and putt all the entities
    // that still need to be processed there
    var graphCopy = graph.deepCopy();
    // use the algorithm described here: https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#finding-the-root-data-entity
    for (int i = 0; i < graph.size(); i++) {
      JsonNode node = graph.get(i);
      JsonNode type = node.get("conformsTo");
      if (type != null) {
        String uri = type.get("@id").asText();
        if (uri.startsWith("https://w3id.org/ro/crate/")) {
          crate.setJsonDescriptor(
              new ContextualEntity.ContextualEntityBuilder().setAll(node.deepCopy()).build());
          graphCopy.remove(i);
          String id = node.get("about").get("@id").asText();
          for (int j = 0; j < graphCopy.size(); j++) {
            ObjectNode secondIteration = graphCopy.get(j).deepCopy();
            if (secondIteration.get("@id").asText().equals(id)) {
              // root data entity
              JsonNode hasPartNode = secondIteration.get("hasPart");
              Set<String> hasPartSet = new HashSet<>();
              if (hasPartNode != null) {
                if (hasPartNode.isArray()) {
                  for (var e : hasPartNode) {
                    hasPartSet.add(e.get("@id").asText());
                  }
                } else if (hasPartNode.isObject()) {
                  hasPartSet.add(hasPartNode.get("@id").asText());
                }
              }
              secondIteration.remove("hasPart");
              crate.setRootDataEntity(
                  new RootDataEntity.RootDataEntityBuilder()
                      .setAll(secondIteration.deepCopy())
                      .setHasPart(hasPartSet)
                      .build()
              );
              graphCopy.remove(j);
              break;
            }
          }
        }
      }
    }
    return graphCopy;
  }
}

