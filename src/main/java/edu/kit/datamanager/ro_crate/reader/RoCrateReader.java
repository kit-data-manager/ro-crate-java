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
import edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation;
import edu.kit.datamanager.ro_crate.validation.Validator;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This class allows reading crates from the outside into the library in order
 * to inspect or modify it.
 * 
 * The class takes a strategy to support different ways of importing the crates.
 * (from zip, folder, etc.)
 */
public class RoCrateReader {

  /**
   * If the number of JSON entities in the crate is larger than this number,
   * parallelization will be used.
   */
  private static final int PARALLELIZATION_THRESHOLD = 100;

  private static final String FILE_PREVIEW_FILES = "ro-crate-preview_files";
  private static final String FILE_PREVIEW_HTML = "ro-crate-preview.html";
  private static final String FILE_METADATA_JSON = "ro-crate-metadata.json";

  protected static final String SPECIFICATION_PREFIX = "https://w3id.org/ro/crate/";

  protected static final String PROP_ABOUT = "about";
  protected static final String PROP_CONTEXT = "@context";
  protected static final String PROP_CONFORMS_TO = "conformsTo";
  protected static final String PROP_GRAPH = "@graph";
  protected static final String PROP_HAS_PART = "hasPart";
  protected static final String PROP_ID = "@id";

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
    // get the ro-crate-medata.json
    ObjectNode metadataJson = reader.readMetadataJson(location);
    // get the content of the crate
    File files = reader.readContent(location);
    
    // this set will contain the files that are associated with entities
    HashSet<String> usedFiles = new HashSet<>();
    usedFiles.add(new File(location).toPath().resolve(FILE_METADATA_JSON).toFile().getPath());
    usedFiles.add(new File(location).toPath().resolve(FILE_PREVIEW_HTML).toFile().getPath());
    usedFiles.add(new File(location).toPath().resolve(FILE_PREVIEW_FILES).toFile().getPath());
    
    JsonNode context = metadataJson.get(PROP_CONTEXT);
    
    CrateMetadataContext crateContext = new RoCrateMetadataContext(context);
    RoCrate crate = new RoCrate();
    crate.setMetadataContext(crateContext);
    JsonNode graph = metadataJson.get(PROP_GRAPH);

    if (graph.isArray()) {

      moveRootEntitiesFromGraphToCrate(crate, (ArrayNode) graph);
      for (JsonNode node : graph) {
        // if the id is in the root hasPart list, we know this entity is a data entity
        RootDataEntity root = crate.getRootDataEntity();
        if (root != null && root.hasInHasPart(node.get(PROP_ID).asText())) {
          File loc = checkFolderHasFile(node.get(PROP_ID).asText(), files);
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

  protected File checkFolderHasFile(String id, File file) {
    Path path = file.toPath().resolve(URLDecoder.decode(id, StandardCharsets.UTF_8));
    if (path.toFile().exists()) {
      return path.toFile();
    }
    return null;
  }

  /**
   * Moves the descriptor and the root entity from the graph to the crate.
   * 
   * Extracts the root data entity and the Metadata File Descriptor from the graph
   * and inserts them into the crate object. It also deletes it from the graph.
   * We will need the root dataset to distinguish between data entities and
   * contextual entities.
   * 
   * @param crate the crate, which will have the entities set, if available in the
   *              graph.
   * @param graph the graph of the Metadata JSON file, where the entities are
   *              extracted from.
   * @return the given graph, but without the root data entity and the Metadata
   *         File Descriptor (JSON descriptor).
   */
  protected void moveRootEntitiesFromGraphToCrate(RoCrate crate, ArrayNode graph) {
    // use the algorithm described here:
    // https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#finding-the-root-data-entity
    boolean isParallel = graph.size() > PARALLELIZATION_THRESHOLD;
    Optional<JsonNode> maybeDescriptor = StreamSupport.stream(graph.spliterator(), isParallel)
        // "2. if the conformsTo property is a URI that starts with
        // https://w3id.org/ro/crate/"
        .filter(node -> node.path(PROP_CONFORMS_TO).path(PROP_ID).asText().startsWith(SPECIFICATION_PREFIX))
        // "3. from this entity’s about object keep the @id URI as variable root"
        .filter(node -> node.path(PROP_ABOUT).path(PROP_ID).isTextual())
        // There should be only one descriptor. If multiple exist, we take the first
        // one.
        .findFirst();

    maybeDescriptor.ifPresent(descriptor -> {
      removeJsonNodeFromArrayNode(graph, descriptor);
      setCrateDescriptor(crate, descriptor);

      Optional<ObjectNode> maybeRoot = extractRoot(graph, descriptor);

      maybeRoot.ifPresent(root -> {
        Set<String> hasPartIds = extractHasPartIds(root);

        crate.setRootDataEntity(
            new RootDataEntity.RootDataEntityBuilder()
                .setAll(root.deepCopy())
                .setHasPart(hasPartIds)
                .build());

        removeJsonNodeFromArrayNode(graph, root);
      });
    });
  }

  /**
   * Extracts the root entity from the graph, using the information from the
   * descriptor.
   * 
   * Basically implements step 5 of the algorithm described here:
   * https://www.researchobject.org/ro-crate/1.1/root-data-entity.html#finding-the-root-data-entity
   * 
   * @param graph      the graph from the metadata JSON-LD file
   * @param descriptor the RO-Crate descriptor
   * @return the root entity, if found
   */
  private Optional<ObjectNode> extractRoot(ArrayNode graph, JsonNode descriptor) {
    String rootId = descriptor.get(PROP_ABOUT).get(PROP_ID).asText();
    boolean isParallel = graph.size() > PARALLELIZATION_THRESHOLD;
    return StreamSupport.stream(graph.spliterator(), isParallel)
        // root is an object (filter + conversion)
        .filter(JsonNode::isObject)
        .map(JsonNode::<ObjectNode>deepCopy)
        // "5. if the entity has an @id URI that matches root return it"
        .filter(node -> node.path(PROP_ID).asText().equals(rootId))
        .findFirst();
  }

  private Set<String> extractHasPartIds(ObjectNode root) {
    JsonNode hasPartNode = root.path(PROP_HAS_PART);
    boolean isParallel = hasPartNode.isArray() && hasPartNode.size() > PARALLELIZATION_THRESHOLD;
    Set<String> hasPartIds = StreamSupport.stream(hasPartNode.spliterator(), isParallel)
        .map(hasPart -> hasPart.path(PROP_ID).asText())
        .filter(text -> !text.isBlank())
        .collect(Collectors.toSet());
    if (hasPartIds.isEmpty() && hasPartNode.path(PROP_ID).isTextual()) {
      hasPartIds.add(hasPartNode.path(PROP_ID).asText());
    }
    return hasPartIds;
  }

  private void removeJsonNodeFromArrayNode(ArrayNode array, JsonNode node) {
    for (int i = 0; i < array.size(); i++) {
      if (array.get(i).equals(node)) {
        array.remove(i);
        return;
      }
    }
  }

  private void setCrateDescriptor(RoCrate crate, JsonNode descriptor) {
    ContextualEntity descriptorEntity = new ContextualEntity.ContextualEntityBuilder()
        .setAll(descriptor.deepCopy())
        .build();
    crate.setJsonDescriptor(descriptorEntity);
  }
}

