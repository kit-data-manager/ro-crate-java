package edu.kit.crate.entities.serializers;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class ObjectNodeSerializer extends StdSerializer<ObjectNode> {

  public ObjectNodeSerializer() {
    this(null);
  }

  protected ObjectNodeSerializer(Class<ObjectNode> t) {
    super(t);
  }

  @Override
  public void serialize(ObjectNode value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonGenerationException {

    Iterator<Entry<String, JsonNode>> fields = value.fields();

    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      String fieldName = field.getKey();
      JsonNode fieldValue = field.getValue();
      if (fieldValue.isNull()) {
        continue;
      }
      // if the type array contains only one type set it as String
      //  if (fieldName.equals("@type")) {
      if (fieldValue.isArray()) {
        if (fieldValue.isEmpty()) {
          continue;
        }
        if (fieldValue.size() == 1) {
          fieldValue = fieldValue.get(0);
        }
      }
      jgen.writeFieldName(fieldName);
      jgen.writeTree(fieldValue);
    }
  }

  @Override
  public boolean isUnwrappingSerializer() {
    return true;
  }

}
