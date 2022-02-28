package edu.kit.crate.entities.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.crate.entities.serializers.HasPartSerializer;
import edu.kit.crate.entities.serializers.ObjectNodeSerializer;
import edu.kit.crate.objectmapper.MyObjectMapper;
import java.util.HashSet;
import java.util.Set;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * @author Nikola Tzotchev on 5.2.2022 г.
 * @version 1
 */
public class DataSetEntity extends DataEntity {

  public static final String TYPE = "Dataset";

  @JsonSerialize(using = HasPartSerializer.class)
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Set<String> hasPart;

  public DataSetEntity(ADataSetBuilder<?> entityBuilder) {
    super(entityBuilder);
    this.hasPart = entityBuilder.hasPart;
    this.addType(TYPE);
  }
  public void removeFromHasPart(String str) {
    this.hasPart.remove(str);
  }
  @Override
  public void saveToZip(ZipFile zipFile) throws ZipException {
    if (this.getLocation() != null) {
      zipFile.addFolder(this.getLocation());
    }
  }

  public void addToHasPart(String id) {
    this.hasPart.add(id);
  }

  public boolean hasInHasPart(String id) {
    return this.hasPart.contains(id);
  }

  abstract static class ADataSetBuilder<T extends ADataEntityBuilder<T>> extends
      ADataEntityBuilder<T> {

    Set<String> hasPart;


    public ADataSetBuilder() {
      this.hasPart = new HashSet<>();
    }

    public T setHasPart(Set<String> hastPart) {
      this.hasPart = hastPart;
      return self();
    }

    public T addToHasPart(DataEntity dataEntity) {
      this.hasPart.add(dataEntity.getId());
      return self();
    }

    public T addToHasPart(String dataEntity) {
      this.hasPart.add(dataEntity);
      return self();
    }

    @Override
    abstract public DataSetEntity build();
  }

  static final public class DataSetBuilder extends ADataSetBuilder<DataSetBuilder> {

    @Override
    public DataSetBuilder self() {
      return this;
    }

    @Override
    public DataSetEntity build() {
      return new DataSetEntity(this);
    }
  }
}
