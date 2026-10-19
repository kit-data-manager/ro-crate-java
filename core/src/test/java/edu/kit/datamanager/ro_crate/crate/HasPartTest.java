package edu.kit.datamanager.ro_crate.crate;

import edu.kit.datamanager.ro_crate.Crate;
import edu.kit.datamanager.ro_crate.RoCrate;
import edu.kit.datamanager.ro_crate.entities.data.DataSetEntity;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;
import edu.kit.datamanager.ro_crate.reader.Readers;
import edu.kit.datamanager.ro_crate.writer.Writers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests related to the addDataEntity(DataEntity, String) method and the hasPart
 * property.
 */
public class HasPartTest {

    @Nested
    @DisplayName("Test for crate (not the builder!) addDataEntity(DataEntity, String)")
    class CrateHasPartTest {
        private RoCrate crate;

        @BeforeEach
        void setUp() {
            crate = new RoCrate.RoCrateBuilder(
                    "Test Crate",
                    "HasPartTest",
                    "2025",
                    "https://creativecommons.org/licenses/by/4.0/"
            ).build();
        }

        @Test
        public void givenEmptyCrate_whenAddingWithConnection_thenThrowsException() {
            // Given empty crate (default)
            // ...
            // When adding entity with connection, Throws Exception
            FileEntity d = new FileEntity.FileEntityBuilder().build();
            assertThrows(IllegalArgumentException.class, () -> this.crate.addDataEntity(d, "nonexitent"));
        }

        @Test
        public void givenEmptyCrate_whenAddingToRoot_thenConnectionExists() {
            // Given empty crate (default)
            // ...
            // When adding entity to root
            final String id = "d";
            FileEntity d = new FileEntity.FileEntityBuilder()
                    .setId(id)
                    .build();
            this.crate.addDataEntity(d, "./");
            // Then root added entity with hasPart
            assertTrue(this.crate.getRootDataEntity().hasPart(id));
            assertNotNull(this.crate.getEntityById(id));
        }

        @Test
        public void givenCrateWithFolder_whenAddingToFolder_thenConnectionExists() {
            // Given crate with folder
            this.crate = new RoCrate.RoCrateBuilder()
                    .addDataEntity(new DataSetEntity.DataSetBuilder().setId("./folder").build())
                    .build();
            // When adding entity to folder
            String dataId = "d";
            FileEntity d = new FileEntity.FileEntityBuilder()
                    .setId(dataId)
                    .build();
            this.crate.addDataEntity(d, "./folder");
            // Then this connection exists
            // Cast required because type was not yet serialized and is not yet in properties.
            assertTrue(((DataSetEntity) this.crate.getDataEntityById("./folder")).hasPart(dataId));
            assertNotNull(this.crate.getEntityById(dataId));
        }

        @Test
        public void givenCrateWithFolderWithFile_whenReadingFromDisk_thenConnectionExists(
                @TempDir Path path
        ) throws IOException {
            // Given crate from disk
            String folderId = "./folder/";
            this.crate = new RoCrate.RoCrateBuilder()
                    .addDataEntity(new DataSetEntity.DataSetBuilder().setId(folderId).build())
                    .build();

            // When adding entity to folder
            String dataId = "d";
            FileEntity d = new FileEntity.FileEntityBuilder()
                    .setId(dataId)
                    .build();
            this.crate.addDataEntity(d, folderId);

            Writers.newFolderWriter().save(this.crate, path.toString());
            Crate read = Readers.newFolderReader().readCrate(path.toString());

            // Then this connection exists
            // Note how the types are loaded when deserializing. Alternatively, you can find them in their properties.
            assertTrue(read.getDataEntityById(folderId).getTypes().contains("Dataset"));
            // Note how you can cast an entity to a dataSetEntity.
            assertTrue(read.getDataSetById(folderId).orElseThrow().hasPart(dataId));
        }
    }

    @Nested
    @DisplayName("Testing the builder addDataEntity(DataEntity, String)")
    class BuilderHasPartTest {
        private RoCrate.RoCrateBuilder builder;

        @BeforeEach
        void setUp() {
            builder = new RoCrate.RoCrateBuilder(
                    "Test Crate",
                    "HasPartTest",
                    "2025",
                    "https://creativecommons.org/licenses/by/4.0/"
            );
        }

        @Test
        public void givenEmptyCrate_whenAddingWithConnection_thenThrowsException() {
            // Given empty crate (default)
            // ...
            // When adding entity with connection, Throws Exception
            FileEntity d = new FileEntity.FileEntityBuilder().build();
            assertThrows(IllegalArgumentException.class, () -> this.builder.addDataEntity(d, "nonexitent"));
        }

        @Test
        public void givenEmptyCrate_whenAddingToRoot_thenConnectionExists() {
            // Given empty crate (default)
            // ...
            // When adding entity to root
            final String id = "d";
            FileEntity d = new FileEntity.FileEntityBuilder()
                    .setId(id)
                    .build();
            this.builder.addDataEntity(d, "./");
            // Then root added entity with hasPart
            Crate crate = this.builder.build();
            assertTrue(crate.getRootDataEntity().hasPart(id));
            assertNotNull(crate.getEntityById(id));
        }

        @Test
        public void givenCrateWithFolder_whenAddingToFolder_thenConnectionExists() {
            // Given crate with folder
            this.builder.addDataEntity(
                    new DataSetEntity.DataSetBuilder()
                            .setId("./folder")
                            .build()
            );
            // When adding entity to folder
            String dataId = "d";
            FileEntity d = new FileEntity.FileEntityBuilder()
                    .setId(dataId)
                    .build();
            this.builder.addDataEntity(d, "./folder");
            // Then this connection exists
            Crate crate = this.builder.build();
            // Cast required because type was not yet serialized and is not yet in properties.
            assertTrue(((DataSetEntity) crate.getDataEntityById("./folder")).hasPart(dataId));
            assertNotNull(crate.getEntityById(dataId));
        }

        @Test
        public void givenCrateWithFolderWithFile_whenReadingFromDisk_thenConnectionExists(
                @TempDir Path path
        ) throws IOException {
            // Given crate from disk
            String folderId = "./folder/";
            this.builder.addDataEntity(
                    new DataSetEntity.DataSetBuilder()
                            .setId(folderId)
                            .build()
            );


            // When adding entity to folder
            String dataId = "d";
            FileEntity d = new FileEntity.FileEntityBuilder()
                    .setId(dataId)
                    .build();
            this.builder.addDataEntity(d, folderId);

            Writers.newFolderWriter().save(this.builder.build(), path.toString());
            Crate read = Readers.newFolderReader().readCrate(path.toString());

            // Then this connection exists
            // Note how the types are loaded when deserializing. Alternatively, you can find them in their properties.
            assertTrue(read.getDataEntityById(folderId).getTypes().contains("Dataset"));
            // Note how you can cast an entity to a dataSetEntity.
            assertTrue(read.getDataSetById(folderId).orElseThrow().hasPart(dataId));
        }
    }
}