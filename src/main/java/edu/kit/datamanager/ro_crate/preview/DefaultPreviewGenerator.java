/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.kit.datamanager.ro_crate.preview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.ResourceCodeResolver;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

/**
 *
 * @author jejkal
 */
public class DefaultPreviewGenerator {

    public static class ROCrateModel {

        public ROCrate crate;
        public List<Dataset> datasets;
        public List<File> files;
    }

    public static class ROCrate {

        public String name;
        public String description;
        public String type;
        public String license;
        public String datePublished;
        public List<Part> hasPart;
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

    public static ROCrateModel mapFromJson(String metadata) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(metadata);
        JsonNode graph = root.get("@graph");

        ROCrate crate = new ROCrate();
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
        return model;
    }

    public static void generatePreview(String metadata, ZipFile zip) throws IOException {
        ROCrateModel model = mapFromJson(metadata);

        Map<String, Object> input = new HashMap<String, Object>();
        input.put("model", model);

        CodeResolver codeResolver = new ResourceCodeResolver("templates"); // This is the directory where your .jte files are located.
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        TemplateOutput output = new StringOutput();
        templateEngine.render("default_preview.jte", input, output);
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setFileNameInZip("ro-crate-preview.html");
        zip.addStream(new ByteArrayInputStream(output.toString().getBytes()), zipParameters);
    }

    public static void generatePreview(String metadata, java.io.File folder) throws IOException {
        ROCrateModel model = mapFromJson(metadata);

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
        w.close();

    }

}
