package edu.kit.datamanager.ro_crate.preview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.preview.model.ROCratePreviewModel;
import edu.kit.datamanager.ro_crate.preview.model.ROCratePreviewModel.Dataset;
import edu.kit.datamanager.ro_crate.preview.model.ROCratePreviewModel.Part;
import edu.kit.datamanager.ro_crate.preview.model.ROCratePreviewModel.ROCrate;
import edu.kit.datamanager.ro_crate.util.JsonLdExpander;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
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

/**
 *
 * @author jejkal
 */
public class DefaultPreviewGenerator {

    public static ROCratePreviewModel mapFromJson(String metadata) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = (JsonNode) mapper.readValue(metadata, JsonNode.class);
        JsonNode graph = root.get("@graph");
        ROCrate crate = new ROCrate();
        List<Dataset> datasets = new ArrayList<>();
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
                            Part p = new Part();
                            p.id = part.get("@id").asText();
                            p.name = part.get("@id").asText(); // Name will be replaced later
                            crate.hasPart.add(p);
                        }

                    }
                } else if (types.contains("Dataset")) {
                    Dataset dataset = new Dataset();
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
            for (Part part : crate.hasPart) {
                for (Dataset dataset : datasets) {
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

    public static void generatePreview(String metadata, ZipFile zip) throws IOException {
        /* ROCrateModel model = mapFromJson(metadata);

        Map<String, Object> input = new HashMap<String, Object>();
        input.put("model", model);

        CodeResolver codeResolver = new ResourceCodeResolver("templates"); // This is the directory where your .jte files are located.
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        TemplateOutput output = new StringOutput();
        templateEngine.render("default_preview.jte", input, output);
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip("ro-crate-preview.html");
        zip.addStream(new ByteArrayInputStream(output.toString().getBytes()), zipParameters);*/
    }

    public static void generatePreview(String metadata, java.io.File folder) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(DefaultPreviewGenerator.class, "/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

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
