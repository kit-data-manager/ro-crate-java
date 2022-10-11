package edu.kit.datamanager.ro_crate.special;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class UriUtilTest {

    // Strings taken from
    // https://www.researchobject.org/ro-crate/1.1/data-entities.html#encoding-file-paths
    static final String file_chinese = "面试.mp4";
    static final String file_chinese_encoded = "%E9%9D%A2%E8%AF%95.mp4";
    static final String file_path_spaces = "Results and Diagrams\\almost-50%.png";
    // note: "\" becomes "/" (unix style):
    static final String file_path_spaces_encoded = "Results%20and%20Diagrams/almost-50%25.png";

    // taken from
    // https://www.researchobject.org/ro-crate/1.1/appendix/jsonld.html#describing-entities-in-json-ld
    static final String url_simple_valid = "http://example.com/";
    static final String url_orcid_valid = "https://orcid.org/0000-0002-1825-0097";
    static final String entity_reference_simple = "#alice";
    static final String entity_reference_generated = "#ac0bd781-7d91-4cdf-b2ad-7305921c7650";
    static final String entity_blank_node_id = "_:alice";

    // Other tests
    static final String url_with_spaces = "https://example.com/file with spaces";
    static final String url_with_spaces_encoded = "https://example.com/file%20with%20spaces";

    public static Stream<Arguments> encodingExamplesProvider() {
        return Stream.of(
                // before encoding , after encoding
                Arguments.of(file_path_spaces, file_path_spaces_encoded),
                Arguments.of(url_with_spaces, url_with_spaces_encoded),
                // double encoding does not happen
                Arguments.of(file_chinese_encoded, file_chinese_encoded),
                Arguments.of(file_path_spaces_encoded, file_path_spaces_encoded),
                Arguments.of(url_with_spaces_encoded, url_with_spaces_encoded),
                // some things should stay as they are according to the specification
                Arguments.of(file_chinese, file_chinese),
                Arguments.of(url_simple_valid, url_simple_valid),
                Arguments.of(url_orcid_valid, url_orcid_valid),
                Arguments.of(entity_reference_simple, entity_reference_simple),
                Arguments.of(entity_reference_generated, entity_reference_generated),
                Arguments.of(entity_blank_node_id, entity_blank_node_id)
        );
    }

    /**
     * Encoding works with the examples from the specification.
     */
    @Test
    void testIsEncodedWithRoCrateSpecExamples() {
        assertTrue(UriUtil.isEncoded(file_chinese));
        assertTrue(UriUtil.isEncoded(file_chinese_encoded));
        assertFalse(UriUtil.isEncoded(file_path_spaces));
        assertTrue(UriUtil.isEncoded(file_path_spaces_encoded));
    }

    /**
     * Decoding works with the examples from the specification.
     */
    @Test
    void testIsDecodedWithRoCrateSpecExamples() {
        assertFalse(UriUtil.isDecoded(file_chinese));
        assertFalse(UriUtil.isDecoded(file_chinese_encoded));
        assertTrue(UriUtil.isDecoded(file_path_spaces));
        assertFalse(UriUtil.isDecoded(file_path_spaces_encoded));
    }

    /**
     * Detecting URLs works with the examples from the specification.
     */
    @Test
    void testIsUrlWithRoCrateSpecExamples() {
        assertFalse(UriUtil.isUrl(file_chinese));
        assertFalse(UriUtil.isUrl(file_chinese_encoded));
        assertFalse(UriUtil.isUrl(file_path_spaces));
        assertFalse(UriUtil.isUrl(file_path_spaces_encoded));
    }

    /**
     * Detecting paths works with the examples from the specification.
     */
    @Test
    void testIsPathWithRoCrateSpecExamples() {
        assertTrue(UriUtil.isPath(file_chinese));
        assertTrue(UriUtil.isPath(file_chinese_encoded));
        assertTrue(UriUtil.isPath(file_path_spaces));
        assertTrue(UriUtil.isPath(file_path_spaces_encoded));

    }

    /**
     * Detecting and differentiating given URLs from paths works.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            url_simple_valid,
            url_orcid_valid,
            url_with_spaces,
            url_with_spaces_encoded,
            "https://example.com/file.html",
            "http://example-doesnotexist.com",
    })
    void testIsPathAndisUrlWithUrls(String url) {
        assertFalse(UriUtil.isPath(url));
        assertTrue(UriUtil.isUrl(url));
    }

    /**
     * Detecting and differentiating given paths from URLs works.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            file_chinese,
            file_chinese_encoded,
            file_path_spaces,
            file_path_spaces_encoded,
            "./file.html",
            "file.html",
    })
    void testIsPathAndisUrlWithPaths(String path) {
        assertTrue(UriUtil.isPath(path));
        assertFalse(UriUtil.isUrl(path));
    }

    /**
     * Tests the encoding function for several "before-after" pairs.
     * 
     * This includes normal tests,
     * checks that double-encoding does not happen,
     * and makes sure that some examples and exceptions of the specification are
     * handled correctly.
     */
    @ParameterizedTest(name = "testEncodeWith {0} and {1}")
    @MethodSource("edu.kit.datamanager.ro_crate.special.UriUtilTest#encodingExamplesProvider")
    void testEncode(String example_unencoded, String example_encoded) {
        Optional<String> encoded = UriUtil.encode(example_unencoded);
        assertEquals(example_encoded, encoded.get());
        assertTrue(UriUtil.isEncoded(encoded.get()));
    }

    /**
     * Same as testEncode, but with the decode function.
     * 
     * Uses the same input examples, but in the reverse direction.
     */
    @ParameterizedTest(name = "testDecodeWith {0} and {1}")
    @MethodSource("edu.kit.datamanager.ro_crate.special.UriUtilTest#encodingExamplesProvider")
    void testDecode(String example_unencoded, String example_encoded) {
        Optional<String> decoded = UriUtil.decode(example_encoded);
        assertEquals(example_unencoded, decoded.get());
        assertTrue(UriUtil.isDecoded(decoded.get()));
    }
}
