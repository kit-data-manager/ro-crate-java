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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
   *
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
    HashSet<String> usedFiles = new HashSet<>();


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
          if (loc != null) {
            usedFiles.add(loc.getPath());
          }
          dataEntity.setLocation(loc);
          this.crate.addDataEntity(dataEntity, false);
        } else {
          // contextual entity
          this.crate.addContextualEntity(
              new ContextualEntity.ContextualEntityBuilder().setAll(node.deepCopy()).build());
        }
      }
    }
    var itr = files.listFiles();//FileUtils.iterateFilesAndDirs(files, TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
    List<File> list = new ArrayList<>() ;
    for (var f : itr) {
      if (!usedFiles.contains(f.getPath())) {
        list.add(f);
      }
    }
    this.crate.setUntrackedFiles(list);
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
    // use the algorithm described here: https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#finding-the-root-data-entity
    for (int i = 0; i < graph.size() ; i++) {
      JsonNode node = graph.get(i);
      JsonNode type = node.get("conformsTo");
      if (type != null) {
        String uri = type.get("@id").asText();
        if (uri.matches("https://w3id.org/ro/crate/.*")) {
          this.crate.setJsonDescriptor(
              new DataEntity.DataEntityBuilder().setAll(node.deepCopy()).build());
          graphCopy.remove(i);
          String id = node.get("about").get("@id").asText();
          for (int j = 0; j < graphCopy.size() ;j++ ) {
            ObjectNode secondIteration = graphCopy.get(j).deepCopy();
            if (secondIteration.get("@id").asText().equals(id)) {
              // root data entity
              this.crate.setRootDataEntity(
                  new RootDataEntity.RootDataEntityBuilder().setAll(secondIteration.deepCopy()).build()
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
