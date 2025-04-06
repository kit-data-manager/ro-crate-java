/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.kit.datamanager.ro_crate.preview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.ro_crate.util.JsonLdExpander;
import static edu.kit.datamanager.ro_crate.util.JsonLdExpander.expandAndPrune;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.lingala.zip4j.ZipFile;

/**
 *
 * @author jejkal
 */
public class DefaultPreviewGenerator {

    public static class ROCrateModel {

        public ROCrate crate;
        public List<Dataset> datasets;
        public List<File> files;

        public ROCrate getCrate() {
            return crate;
        }

        public List<Dataset> getDatasets() {
            return datasets;
        }

        public List<File> getFiles() {
            return files;
        }

    }

    public static class ROCrate {

        public String name;
        public String description;
        public String type;
        public String license;
        public String datePublished;
        public List<Part> hasPart;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public String getLicense() {
            return license;
        }

        public String getDatePublished() {
            return datePublished;
        }

        public List<Part> getHasPart() {
            return hasPart;
        }

    }

    public static class Part {

        public String id;
        public String name;
    }

    public static class Dataset {

        public String id;
        public String name;
        public String description;
    }

    public static class File {

        public String id;
        public String name;
        public String description;
        public String contentSize;
        public String encodingFormat;
    }

    public static Map<String, Object> mapFromJson(String metadata) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        // Read JSON File
        Map<String, Object> root = mapper.readValue(metadata, new TypeReference<Map<String, Object>>() {
        });

        // Extract `@graph` as a list of maps
        List<Map<String, Object>> graph = (List<Map<String, Object>>) root.get("@graph");

        // Rename `@graph` to `graph` before passing to FreeMarker
        root.put("graph", graph);
        root.remove("@graph");

        return root;

        /* ROCrate crate = new ROCrate();
        List<Dataset> datasets = new ArrayList<>();
        List<File> files = new ArrayList<>();

        if (graph.isArray()) {

            for (JsonNode node : graph) {
                String id = node.get("@id").asText();
                String type = node.get("@type").asText();

                if ("Dataset".equals(type) && "./".equals(id)) {
                    crate.name = node.get("name").asText();
                    crate.description = node.get("description").asText();
                    crate.type = type;
                    crate.license = node.get("license").get("@id").asText();
                    crate.datePublished = node.get("datePublished").asText();
                    crate.hasPart = new ArrayList<>();

                    if (node.has("hasPart")) {
                        for (JsonNode part : node.get("hasPart")) {
                            Part p = new Part();
                            p.id = part.get("@id").asText();
                            p.name = "Unknown"; // Name will be set later
                            crate.hasPart.add(p);
                        }
                    }
                } else if ("Dataset".equals(type)) {
                    Dataset dataset = new Dataset();
                    dataset.id = id;
                    dataset.name = node.get("name").asText();
                    dataset.description = node.get("description").asText();
                    datasets.add(dataset);
                } else if ("File".equals(type)) {
                    File file = new File();
                    file.id = id;
                    file.name = node.get("name").asText();
                    file.description = node.get("description").asText();
                    file.contentSize = node.get("contentSize").asText();
                    file.encodingFormat = node.get("encodingFormat").asText();
                    files.add(file);
                }
            }
        }

        // Update Part names using dataset and file lists
        for (Part part : crate.hasPart) {
            for (Dataset dataset : datasets) {
                if (dataset.id.equals(part.id)) {
                    part.name = dataset.name;
                }
            }
            for (File file : files) {
                if (file.id.equals(part.id)) {
                    part.name = file.name;
                }
            }
        }

        ROCrateModel model = new ROCrateModel();
        model.crate = crate;
        model.datasets = datasets;
        model.files = files;
        //return model;
        return null;*/
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
        /*  ROCrateModel model = mapFromJson(metadata);

        Map<String, Object> input = new HashMap<String, Object>();
        input.put("model", model);

        CodeResolver codeResolver = new ResourceCodeResolver("templates"); // This is the directory where your .jte files are located.
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

        TemplateOutput output = new StringOutput();
        templateEngine.render("default_preview.jte", input, output);
        System.out.println(output.toString());
        FileWriter w = new FileWriter(new java.io.File(folder, "ro-crate-preview.html"));
        w.write(output.toString());
        w.flush();
        w.close();*/
        // Create your Configuration instance, and specify if up to what FreeMarker
// version (here 2.3.34) do you want to apply the fixes that are not 100%
// backward-compatible. See the Configuration JavaDoc for details.
        Configuration cfg = new Configuration();

// Specify the source where the template files come from. Here I set a
// plain directory for it, but non-file-system sources are possible too:
        cfg.setClassForTemplateLoading(DefaultPreviewGenerator.class, "/");//DirectoryForTemplateLoading(new File("/where/you/store/templates"));

// From here we will set the settings recommended for new projects. These
// aren't the defaults for backward compatibilty.
// Set the preferred charset template files are stored in. UTF-8 is
// a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");

// Sets how errors will appear.
// During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        JsonLdExpander ep = new JsonLdExpander();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode expanded = ep.expandAndPrune(mapper.readTree(metadata));
            mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, expanded);
            Map<String, Object> model = mapper.readValue(mapper.writeValueAsString(expanded), new TypeReference<>() {
            });
            System.out.println("MOD " + model);
            Template temp = cfg.getTemplate("templates/extended_preview.frm");
            Writer out = new OutputStreamWriter(new FileOutputStream("prev.html"));

            temp.process(model, out);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

}
