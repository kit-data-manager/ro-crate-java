package edu.kit.datamanager.ro_crate.preview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static edu.kit.datamanager.ro_crate.preview.DefaultPreviewGenerator.mapFromJson;
import edu.kit.datamanager.ro_crate.preview.model.ROCratePreviewModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;

/**
 * This class represents the custom preview of a crate, which means html files
 * created from outside sources.
 *
 * @author Nikola Tzotchev on 12.2.2022 г.
 * @version 1
 */
public class CustomPreview implements CratePreview {

    private final Configuration cfg;

    public CustomPreview() {
        cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(DefaultPreviewGenerator.class, "/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    }

    public ROCratePreviewModel mapFromJson(String metadata) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = (JsonNode) mapper.readValue(metadata, JsonNode.class);
        JsonNode graph = root.get("@graph");
        ROCratePreviewModel.ROCrate crate = new ROCratePreviewModel.ROCrate();
        List<ROCratePreviewModel.Dataset> datasets = new ArrayList<>();
        List<ROCratePreviewModel.File> files = new ArrayList<>();

        if (graph.isArray()) {

            for (JsonNode node : graph) {
                String id = node.get("@id").asText();
                List<String> types = new LinkedList<>();
                if (node.get("@type").isArray()) {

                    Collections.addAll(types, (String[]) mapper.convertValue(node.get("@type"), String[].class));
                } else {
                    types.add(node.get("@type").asText());
                }

                if (types.contains("Dataset") && "./".equals(id)) {
                    crate.name = node.get("name").asText();
                    crate.description = node.get("description") == null ? null : node.get("description").asText();
                    crate.type = "Dataset";
                    crate.license = node.get("license").isObject() ? node.get("license").get("@id").asText() : node.get("license").asText();
                    crate.datePublished = node.get("datePublished") == null ? null : node.get("datePublished").asText();
                    crate.hasPart = new ArrayList<>();

                    if (node.has("hasPart")) {
                        for (JsonNode part : node.get("hasPart")) {
                            ROCratePreviewModel.Part p = new ROCratePreviewModel.Part();
                            p.id = part.get("@id").asText();
                            p.name = part.get("@id").asText(); // Name will be replaced later
                            crate.hasPart.add(p);
                        }

                    }
                } else if (types.contains("Dataset")) {
                    ROCratePreviewModel.Dataset dataset = new ROCratePreviewModel.Dataset();
                    dataset.id = id;
                    dataset.name = node.get("name").asText();
                    dataset.description = node.get("description").asText();
                    datasets.add(dataset);
                } else if (types.contains("File")) {
                    ROCratePreviewModel.File file = new ROCratePreviewModel.File();
                    file.id = id;
                    file.name = node.get("name") == null ? null : node.get("name").asText();
                    file.description = node.get("description") == null ? null : node.get("description").asText();
                    file.contentSize = node.get("contentSize") == null ? null : node.get("contentSize").asText();
                    file.encodingFormat = node.get("encodingFormat") == null ? null : node.get("encodingFormat").asText();
                    files.add(file);
                }
            }
        }

        // Update Part names using dataset and file lists
        if (crate.hasPart != null) {
            for (ROCratePreviewModel.Part part : crate.hasPart) {
                for (ROCratePreviewModel.Dataset dataset : datasets) {
                    if (dataset.id.equals(part.id) && dataset.name != null) {
                        part.name = dataset.name;
                    }
                }
                for (ROCratePreviewModel.File file : files) {
                    if (file.id.equals(part.id) && file.name != null) {
                        part.name = file.name;
                    }
                }
            }
        }

        ROCratePreviewModel model = new ROCratePreviewModel();
        model.crate = crate;
        model.datasets = datasets;
        model.files = files;
        return model;
    }

    @Override
    public void saveAllToZip(ZipFile zipFile) {

    }

    @Override
    public void saveAllToFolder(File folder) {

        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("crateModel", mapFromJson(metadata));
            Template temp = cfg.getTemplate("templates/custom_preview.frm");
            Writer out = new OutputStreamWriter(new FileOutputStream("prev.html"));

            temp.process(dataModel, out);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }
}
