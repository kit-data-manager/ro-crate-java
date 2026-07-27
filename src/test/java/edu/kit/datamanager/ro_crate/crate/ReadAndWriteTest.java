package edu.kit.datamanager.ro_crate.crate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.HelpFunctions;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.preview.StaticPreview;
import edu.kit.datamanager.ro_crate.reader.CrateReader;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.special.IdentifierUtils;
import edu.kit.datamanager.ro_crate.writer.CrateWriter;
import edu.kit.datamanager.ro_crate.writer.Writers;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReadAndWriteTest {

  @Test
  void testReadingAndWriting(@TempDir Path path) throws IOException {
    Path htmlFile = path.resolve("htmlFile.html");
    FileUtils.writeStringToFile(
      htmlFile.toFile(),
      "useful file",
      Charset.defaultCharset()
    );
    Path htmlDir = path.resolve("dir");
    Path fileInDir = htmlDir.resolve("file.html");
    FileUtils.writeStringToFile(
      fileInDir.toFile(),
      "fileN2",
      Charset.defaultCharset()
    );

    RoCrate crate = new RoCrate.RoCrateBuilder(
      "name",
      "description",
      "2024",
      "https://creativecommons.org/licenses/by-nc-sa/3.0/au/"
    )
      .setPreview(new StaticPreview(htmlFile.toFile(), htmlDir.toFile()))
      .build();

    Path writeDir = path.resolve("crate");

    Writers.newFolderWriter().save(crate, writeDir.toAbsolutePath().toString());

    CrateReader<String> reader = Readers.newFolderReader();
    Crate newCrate = reader.readCrate(writeDir.toAbsolutePath().toString());

    // the preview files as well as the metadata file should not be included here
    assertEquals(0, newCrate.getUntrackedFiles().size());

    HelpFunctions.compareTwoCrateJson(newCrate, crate);
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  void testReadCrateWithHasPartHierarchy() throws IOException {
    CrateReader<String> reader = Readers.newFolderReader();
    RoCrate crate = reader.readCrate(
      ReadAndWriteTest.class.getResource("/crates/hasPartHierarchy").getPath()
    );
    assertEquals(1, crate.getAllContextualEntities().size());
    assertEquals(6, crate.getAllDataEntities().size());
  }

  @Test
  void testEncodedIdsFindTheirPaths(@TempDir Path tempDir) throws IOException {
    RoCrate.RoCrateBuilder builder = new RoCrate.RoCrateBuilder();
    {
      FileEntity.FileEntityBuilder dataEntityBuilder =
        new FileEntity.FileEntityBuilder();
      dataEntityBuilder.setId("id 1");
      dataEntityBuilder.addTypes(List.of("File"));
      UUID uuid = UUID.randomUUID();
      Path path = tempDir.resolve(uuid.toString());
      Files.writeString(path, "File");
      dataEntityBuilder.setLocation(path);

      builder.addDataEntity(dataEntityBuilder.build());
    }
    {
      FileEntity.FileEntityBuilder dataEntityBuilder =
        new FileEntity.FileEntityBuilder();
      dataEntityBuilder.setId("id1");
      dataEntityBuilder.addTypes(List.of("File"));
      UUID uuid = UUID.randomUUID();
      Path path = tempDir.resolve(uuid.toString());
      Files.writeString(path, "File");
      dataEntityBuilder.setLocation(path);

      builder.addDataEntity(dataEntityBuilder.build());
    }
    {
      FileEntity.FileEntityBuilder dataEntityBuilder =
        new FileEntity.FileEntityBuilder();
      dataEntityBuilder.setId("id面试1");
      dataEntityBuilder.addTypes(List.of("File"));
      UUID uuid = UUID.randomUUID();
      Path path = tempDir.resolve(uuid.toString());
      Files.writeString(path, "File");
      dataEntityBuilder.setLocation(path);

      builder.addDataEntity(dataEntityBuilder.build());
    }
    Path location = tempDir.resolve("out");
    {
      RoCrate crate = builder.build();
      CrateWriter<String> writer = Writers.newFolderWriter();
      writer.save(crate, location.toString());
    }
    {
      CrateReader<String> roCrateReader = Readers.newFolderReader();

      RoCrate roCrate = roCrateReader.readCrate(
        location.toString()
      );
      for (DataEntity dataEntity : roCrate.getAllDataEntities()) {
        System.out.println(dataEntity.getId() + ": " + dataEntity.getPath());
      }
      for (DataEntity dataEntity : roCrate.getAllDataEntities()) {
        assertNotNull(
          dataEntity.getPath(),
          "Path of ID: " + dataEntity.getId()
        );
      }
    }
  }

  /**
   * Test we detect files which use the encoded IDs as filename,
   * as well as ones which use the decoded filename.
   */
  @Test
  void testDetectingEncodedFileNames(@TempDir Path tempDir) throws IOException {
    // This is how we add the id. But the space will be encoded
    String id = "id 42";
    // This is how we get it out (the encoded id as it will exist in the crate)
    String idEncoded = IdentifierUtils.encode(id).orElseThrow();

    RoCrate.RoCrateBuilder builder = new RoCrate.RoCrateBuilder();

    {
      // Add file entity without a file with id
      FileEntity.FileEntityBuilder fileEntityBuilder =
        new FileEntity.FileEntityBuilder();
      fileEntityBuilder.setId(id);
      fileEntityBuilder.addTypes(List.of("File"));

      builder.addDataEntity(fileEntityBuilder.build());
    }

    Path cratepath1 = tempDir.resolve("test1");
    {
      Writers.newFolderWriter().save(builder.build(), cratepath1.toString());
      // add file manually (decoded id)
      Path filepath = cratepath1.resolve(id);
      Files.writeString(filepath, "File");
    }

    Path cratepath2 = tempDir.resolve("test2");
    {
      Writers.newFolderWriter().save(builder.build(), cratepath2.toString());
      // add file manually (encoded id)
      Path filepath = cratepath2.resolve(idEncoded);
      Files.writeString(filepath, "File");
    }

    {
      RoCrate crate = Readers.newFolderReader().readCrate(
        cratepath1.toString()
      );

      DataEntity entity = crate.getDataEntityById(idEncoded);
      assertEquals(idEncoded, entity.getId());

      Path filepath = entity.getPath();
      assertNotNull(filepath);
      assertTrue(Files.exists(filepath));
      assertEquals(id, filepath.getFileName().toString());
    }

    {
      RoCrate crate = Readers.newFolderReader().readCrate(
        cratepath2.toString()
      );

      DataEntity entity = crate.getDataEntityById(idEncoded);
      assertEquals(idEncoded, entity.getId());

      Path filepath = entity.getPath();
      assertNotNull(filepath);
      assertTrue(Files.exists(filepath));
      assertEquals(idEncoded, filepath.getFileName().toString());
    }
  }

  @Test
  void testFilenamesAreSelfHealing(@TempDir Path tempDir) throws IOException {
    // This is how we add the id. But the space will be encoded
    String id = "id 42";
    // This is how we get it out (the encoded id as it will exist in the crate)
    String idEncoded = IdentifierUtils.encode(id).orElseThrow();

    RoCrate.RoCrateBuilder builder = new RoCrate.RoCrateBuilder();

    // add dummy file
    Path filepath_outside = tempDir.resolve("someFile.txt");
    Files.writeString(filepath_outside, "File");

    {
      // Add file entity without a file with id
      FileEntity.FileEntityBuilder fileEntityBuilder =
        new FileEntity.FileEntityBuilder();
      fileEntityBuilder.setId(id);
      fileEntityBuilder.addTypes(List.of("File"));
      fileEntityBuilder.setLocation(filepath_outside);

      builder.addDataEntity(fileEntityBuilder.build());
    }

    Path cratepath = tempDir.resolve("test1");
    {
      Writers.newFolderWriter().save(builder.build(), cratepath.toString());
      Path currentFilePath = cratepath.resolve(id);
      assertTrue(currentFilePath.toFile().exists());
      // swap file names
      Path newFilePath = cratepath.resolve(idEncoded);
      Files.move(currentFilePath, newFilePath);
      assertFalse(currentFilePath.toFile().exists());
      assertTrue(newFilePath.toFile().exists());
    }

    {
        RoCrate crate = Readers.newFolderReader().readCrate(
          cratepath.toString()
        );

        DataEntity entity = crate.getDataEntityById(idEncoded);
        assertEquals(idEncoded, entity.getId());

        // Even if a file's name is encoded, the path will work as expected
        Path filepath = entity.getPath();
        assertNotNull(filepath);
        assertTrue(Files.exists(filepath));
        assertEquals(idEncoded, filepath.getFileName().toString());
    }

    // When saving the crate again, the file will be renamed to the decoded id
    Path cratepath2 = tempDir.resolve("test2");
    {
      Writers.newFolderWriter().save(builder.build(), cratepath2.toString());
    }

    {
        RoCrate crate = Readers.newFolderReader().readCrate(
          cratepath2.toString()
        );

        DataEntity entity = crate.getDataEntityById(idEncoded);
        assertEquals(idEncoded, entity.getId());

        // The filename is now decoded to the original id
        Path filepath = entity.getPath();
        assertNotNull(filepath);
        assertTrue(Files.exists(filepath));
        assertEquals(id, filepath.getFileName().toString());
    }

  }
}
