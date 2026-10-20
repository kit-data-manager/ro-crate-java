# Migrating to the Jackson-free core

Starting with version 3, ro-crate-java is split into a Jackson-free core and
pluggable JSON backends. This guide explains what changes for your code when
you upgrade from the previous, Jackson-based 2.x API.

**Summary**

- The artifact coordinates for the Jackson 2 backend are unchanged
  (`edu.kit.datamanager:ro-crate-java`), so your dependency declaration keeps
  working.
- The high-level API (the `RoCrate` builder and the entity builders) is
  source-compatible. Most code needs no changes at all.
- Code that used Jackson types from the library (`ObjectNode`, `JsonNode`,
  `ArrayNode`, …) must switch to the neutral JSON model of the core. The
  method names and semantics deliberately mirror Jackson, so this is mostly a
  type rename.
- A few internal Jackson classes were removed (see
  [Removed classes](#removed-classes)).

## Modules

| Module     | Artifact                                                     | Purpose                                                                                     |
| ---------- | ------------------------------------------------------------ | ------------------------------------------------------------------------------------------- |
| `core`     | (internal, pulled in transitively)                           | Crate model, reader/writer, validation API, neutral JSON model — no JSON library dependency |
| `jackson2` | `edu.kit.datamanager:ro-crate-java` (historical coordinates) | Jackson 2 backend, for existing consumers                                                   |
| `jackson3` | `edu.kit.datamanager:ro-crate-java-jackson3`                 | Jackson 3 (`tools.jackson`) backend                                                         |

The core contains no JSON code itself. The backend module provides it, and the
core selects the backend at runtime with Java's `ServiceLoader`: the backend
jar carries a `META-INF/services` file that names its implementation classes,
and the core loads whatever is listed there. Exactly one backend must be on
the classpath. Depending on `edu.kit.datamanager:ro-crate-java` puts the
Jackson 2 backend there; `edu.kit.datamanager:ro-crate-java-jackson3` puts the
Jackson 3 backend there. The two core entry points that use it are `Json`
(JSON input and output) and `Validation` (schema validation).

## Type mapping

The Jackson types that used to leak into the public API are replaced by the
neutral JSON model from the **`core`** module, package
`edu.kit.datamanager.ro_crate.json`:

| Before (`com.fasterxml.jackson.databind…`) | After (`edu.kit.datamanager.ro_crate.json…`) |
| ------------------------------------------ | -------------------------------------------- |
| `JsonNode`                                 | `JsonValue`                                  |
| `ObjectNode`                               | `JsonObject`                                 |
| `ArrayNode`                                | `JsonArray`                                  |
| `TextNode`                                 | `JsonString`                                 |
| `IntNode` / `LongNode` / `DoubleNode`      | `JsonNumber`                                 |
| `BooleanNode`                              | `JsonBoolean`                                |
| `NullNode`                                 | `JsonNull`                                   |
| `MissingNode`                              | `JsonMissing`                                |

The neutral model is not a general-purpose JSON library: it implements exactly
the subset of Jackson's API the library needs, with the same method names and
semantics (`path()`, `get()`, `has()`, `asText()`, `fields()`, `size()`,
iteration, …).

## Changing the imports

Before (Jackson types came from `com.fasterxml.jackson.databind…`):

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

ObjectNode properties = entity.getProperties();
JsonNode name = properties.path("name");
```

After (the neutral model lives in the `core` module,
`edu.kit.datamanager.ro_crate.json`):

```java
import edu.kit.datamanager.ro_crate.json.JsonObject;
import edu.kit.datamanager.ro_crate.json.JsonValue;

JsonObject properties = entity.getProperties();
JsonValue name = properties.path("name");
```

## Common changes

### Properties

The method names are unchanged. Only the types in the signatures are now neutral.
Scalar `addProperty` overloads (`String`, `int`, `double`, `boolean`) exist on
the builders, so most property code needs no JSON types at all. The
`addProperty(String, JsonValue)` overload replaces the old
`addProperty(String, JsonNode)`:

```java
import edu.kit.datamanager.ro_crate.json.JsonString;
import edu.kit.datamanager.ro_crate.entities.data.FileEntity;

// was: builder.addProperty("description", new TextNode("long description"))
builder.addProperty("description", new JsonString("long description"));
```

### Building JSON trees

Before, you would have used Jackson's `ObjectMapper` or `JsonNodeFactory`.
Now use the factory methods of `edu.kit.datamanager.ro_crate.json.Json`
(**`core`** module):

```java
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonArray;
import edu.kit.datamanager.ro_crate.json.JsonObject;

JsonObject obj = Json.objectNode()
    .put("name", "Result table")
    .put("contentSize", 26452);
JsonArray tags = Json.arrayNode().add("science").add("survey");

builder.setAllIfValid(obj);   // was: setAll(ObjectNode); setAll is deprecated
```

### Parsing JSON

The `edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper` class was
removed. Parse with the core's `Json` entry point instead, which delegates to
the active backend:

```java
import edu.kit.datamanager.ro_crate.json.Json;
import edu.kit.datamanager.ro_crate.json.JsonValue;

JsonValue parsed = Json.parse(new java.io.File("ro-crate-metadata.json"));
```

## Converting between Jackson and the neutral model

If your code works with its own JSON trees, for example parsed with your own
`ObjectMapper`, convert them at the boundary to the neutral model. Both
backend modules provide the same helpers on their adapter class. With the
Jackson 2 backend (`edu.kit.datamanager:ro-crate-java`, package
`edu.kit.datamanager.ro_crate.json.jackson2`):

```java
import com.fasterxml.jackson.databind.JsonNode;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.json.jackson2.Jackson2JsonAdapter;

JsonNode jacksonNode = yourObjectMapper.readTree(json);
JsonValue neutral = Jackson2JsonAdapter.toNeutral(jacksonNode);
// and back, if needed:
JsonNode again = Jackson2JsonAdapter.toJackson(neutral);
```

With the Jackson 3 backend (`edu.kit.datamanager:ro-crate-java-jackson3`,
package `edu.kit.datamanager.ro_crate.json.jackson3`), the same methods exist
for Jackson 3 node types:

```java
import tools.jackson.databind.JsonNode;
import edu.kit.datamanager.ro_crate.json.JsonValue;
import edu.kit.datamanager.ro_crate.json.jackson3.Jackson3JsonAdapter;

JsonNode jacksonNode = yourJsonMapper.readTree(json);
JsonValue neutral = Jackson3JsonAdapter.toNeutral(jacksonNode);
// and back, if needed:
JsonNode again = Jackson3JsonAdapter.toJackson(neutral);
```

If your application uses Jackson 3, depend on the Jackson 3 artifact; the
classpath then contains only one Jackson version. The neutral model covers
only the subset of the JSON API the library needs, so do advanced tree
manipulation on your own nodes before converting.

## Providing your own validator

Validation behaves like the JSON backends: the core defines the interfaces
`edu.kit.datamanager.ro_crate.validation.ValidatorStrategy` and
`edu.kit.datamanager.ro_crate.entities.validation.EntityValidationStrategy`,
the backend modules implement them, and the implementations are listed in a
`META-INF/services` file and loaded with `ServiceLoader`. The JSON-schema
implementation keeps its historical fully qualified name in the `jackson2`
module (`edu.kit.datamanager.ro_crate.validation.JsonSchemaValidation`), with
neutral JSON types in its signatures.

To provide your own validator, implement the interface and list your class in
a file named
`META-INF/services/edu.kit.datamanager.ro_crate.validation.ValidatorStrategy`
inside your jar.

## Removed classes

Internal classes without a replacement:

- `edu.kit.datamanager.ro_crate.objectmapper.MyObjectMapper` — use
  `edu.kit.datamanager.ro_crate.json.Json.parse` / `Json.write` (see above).
- `edu.kit.datamanager.ro_crate.entities.serializers.HasPartSerializer` and
  `edu.kit.datamanager.ro_crate.entities.serializers.ObjectNodeSerializer` —
  serialization is handled by the backend's `JsonAdapter`.
