package edu.kit.crate.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.ROCrate;
import edu.kit.crate.IROCrate;
import edu.kit.crate.context.IROCrateMetadataContext;
import edu.kit.crate.context.ROCrateMetadataContext;
import edu.kit.crate.entities.contextual.ContextualEntity;
import edu.kit.crate.entities.data.DataEntity;
import edu.kit.crate.entities.data.RootDataEntity;
import java.io.File;
import java.nio.file.Path;

/**
 * @author Nikola Tzotchev on 9.2.2022 г.
 * @version 1
 */
public class ROCrateReader {

  private IReaderStrategy reader;
  private IROCrate crate;

  public ROCrateReader(IReaderStrategy reader) {
    this.reader = reader;
  }

  /**
   * This function will read the location (using one of the specified strategies) and then
   * build the relation between the entities
   * @param location the location of the ro-crate to be read
   * @return the read RO-crate
   */
  public IROCrate readCrate(String location) {
    crate = new ROCrate();
    // get the ro-crate-medata.json
    ObjectNode metadataJson = reader.readMetadataJson(location);
    // get the content of the crate
    File files = reader.readContent(location);
    // entities merge

    JsonNode context = metadataJson.get("@context");

    IROCrateMetadataContext crateContext = new ROCrateMetadataContext(context);
    this.crate.setMetadataContext(crateContext);
    JsonNode graph = metadataJson.get("@graph");

    if (graph.isArray()) {

      graph = setRootEntities((ArrayNode) graph);

      for (JsonNode node : graph) {
        // if the id is in the root has part we should add this entity as data entity
        if (this.crate.getRootDataEntity().hasInHasPart(node.get("@id").asText())) {
          // data entity
          DataEntity dataEntity = new DataEntity.DataEntityBuilder().setAll(node.deepCopy()).build();
          File loc = checkFolderHasFile(dataEntity.getId(), files);
          dataEntity.setLocation(loc);
          this.crate.addDataEntity(dataEntity);
        } else {
          // contextual entity
          this.crate.addContextualEntity(
              new ContextualEntity.ContextualEntityBuilder().setAll(node.deepCopy()).build());
        }
      }
    }
    return this.crate;
  }

  private File checkFolderHasFile(String id, File file) {
    Path path = file.toPath().resolve(id);
    if (path.toFile().exists()) {
      return path.toFile();
    }
    return null;
  }
  // gets the entities that every crate should have
  // we will need the root dataset to distinguish between data entities and contextual entities
  private ArrayNode setRootEntities(ArrayNode graph) {

    // for now, we make an empty ArrayNode and putt all the entities that still need to be processed there
    var graphCopy = graph.deepCopy();
    graphCopy.removeAll();

    for (JsonNode node : graph) {
      JsonNode type = node.get("@type");
      if (!type.isArray()) {

        String t = type.asText();

        if (t.equals("Dataset") && node.get("@id").asText().equals("./")) {
          // set root data entity
          this.crate.setRootDataEntity(
              new RootDataEntity.RootDataEntityBuilder().setAll(node.deepCopy()).build()
          );
        } else if (t.equals("CreativeWork") && node.get("@id").asText()
            .equals("ro-crate-metadata.json")) {
          // set the json descriptor
          this.crate.setJsonDescriptor(
              new DataEntity.DataEntityBuilder().setAll(node.deepCopy()).build()
          );
        } else {
          graphCopy.add(node);
        }
      }
    }
    return graphCopy;
  }
}
