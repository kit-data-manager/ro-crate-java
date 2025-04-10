package edu.kit.datamanager.ro_crate.preview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.ro_crate.preview.model.ROCratePreviewModel;
import edu.kit.datamanager.ro_crate.util.ZipUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * This class generates a custom preview without requiring external
 * dependencies, i.e., rochtml. Therefore, the FreeMarker template located under
 * resources/templates/custom_preview.ftl is used.
 *
 * @author jejkal
 */
public class CustomPreview implements CratePreview {

    private final Configuration cfg;

    public CustomPreview() {
        cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(CustomPreview.class, "/");
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
                    if (node.get("license") != null) {
                        crate.license = node.get("license").isObject() ? node.get("license").get("@id").asText() : node.get("license").asText();
                    }
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
    public void saveAllToZip(ZipFile zipFile) throws IOException {
        if (zipFile == null) {
            throw new IOException("Argument zipFile must not be null.");
        }
        try {
            zipFile.extractFile("ro-crate-metadata.json", "temp");
        } catch (ZipException ex) {
            throw new IOException("ro-crate-metadata.json not found in provided ZIP.", ex);
        }

        String metadata = FileUtils.readFileToString(new File("temp/ro-crate-metadata.json"), "UTF-8");
        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("crateModel", mapFromJson(metadata));
            Template temp = cfg.getTemplate("templates/custom_preview.ftl");
            try (Writer out = new OutputStreamWriter(new FileOutputStream("temp/ro-crate-preview.html"))) {
                temp.process(dataModel, out);
                out.flush();
            }
            zipFile.addFile("temp/ro-crate-preview.html");
        } catch (TemplateException ex) {
            throw new IOException("Failed to generate preview.", ex);
        } finally {
            try {
                FileUtils.deleteDirectory(new File("temp"));
            } catch (IOException ex) {
                //ignore
            }
        }
    }

    @Override
    public void saveAllToFolder(File folder) throws IOException {
        if (folder == null || !folder.exists()) {
            throw new IOException("Preview target folder " + folder + " does not exist.");
        }
        String metadata = FileUtils.readFileToString(new File(folder, "ro-crate-metadata.json"), "UTF-8");
        try {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("crateModel", mapFromJson(metadata));
            Template temp = cfg.getTemplate("templates/custom_preview.ftl");
            try (Writer out = new OutputStreamWriter(new FileOutputStream(new File(folder, "ro-crate-preview.html")))) {
                temp.process(dataModel, out);
                out.flush();
            }
        } catch (TemplateException ex) {
            throw new IOException("Failed to generate preview.", ex);
        }
    }

    @Override
    public void saveAllToStream(String metadata, ZipOutputStream stream) throws IOException {
        try {
            //prepare metadata for template
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("crateModel", mapFromJson(metadata));

            //prepare output folder and writer
            FileUtils.forceMkdir(new File("temp"));
            //load and process template
            try (FileWriter writer = new FileWriter(new File("temp/ro-crate-preview.html"))) {
                //load and process template
                Template temp = cfg.getTemplate("templates/custom_preview.frm");
                temp.process(dataModel, writer);
                writer.flush();
            }

            ZipUtil.addFileToZipStream(stream, new File("temp/ro-crate-preview.html"), "ro-crate-preview.html");
        } catch (TemplateException ex) {
            throw new IOException("Failed to generate preview.", ex);
        } finally {
            try {
                FileUtils.deleteDirectory(new File("temp"));
            } catch (IOException ex) {
                //ignore
            }
        }
    }

}
