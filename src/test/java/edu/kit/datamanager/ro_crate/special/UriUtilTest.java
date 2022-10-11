package edu.kit.datamanager.ro_crate.special;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class UriUtilTest {

    // Strings taken from
    // https://www.researchobject.org/ro-crate/1.1/data-entities.html#encoding-file-paths
    private static final String file_chinese = "面试.mp4";
    private static final String file_chinese_encoded = "%E9%9D%A2%E8%AF%95.mp4";
    private static final String file_path_spaces = "Results and Diagrams\\almost-50%.png";
    // note: "\" becomes "/" (unix style):
    private static String file_path_spaces_encoded = "Results%20and%20Diagrams/almost-50%25.png";

    @Test
    void testIsEncodedWithRoCrateSpecExamples() {
        assertTrue(UriUtil.isEncoded(file_chinese));
        assertTrue(UriUtil.isEncoded(file_chinese_encoded));
        assertFalse(UriUtil.isEncoded(file_path_spaces));
        assertTrue(UriUtil.isEncoded(file_path_spaces_encoded));
    }

    @Test
    void testIsUrlWithRoCrateSpecExamples() {
        assertFalse(UriUtil.isUrl(file_chinese));
        assertFalse(UriUtil.isUrl(file_chinese_encoded));
        assertFalse(UriUtil.isUrl(file_path_spaces));
        assertFalse(UriUtil.isUrl(file_path_spaces_encoded));
    }

    @Test
    void testIsPathWithRoCrateSpecExamples() {
        assertTrue(UriUtil.isPath(file_chinese));
        assertTrue(UriUtil.isPath(file_chinese_encoded));
        assertTrue(UriUtil.isPath(file_path_spaces));
        assertTrue(UriUtil.isPath(file_path_spaces_encoded));

    }

    @Test
    void testIsPathWithUrlExamples() {
        assertFalse(UriUtil.isPath("https://example.com/"));
        assertFalse(UriUtil.isPath("https://example.com/file.html"));
        assertFalse(UriUtil.isPath("http://example-doesnotexist.com"));
    }

    @Test
    void testEncodePreferReadableAboutFullEncoding() {
        assertEncodingIsTheSame(file_chinese);
    }

    @Test
    void testEncodeFilePathSpaces() {
        Optional<String> encoded = UriUtil.encode(file_path_spaces);
        assertEquals(file_path_spaces_encoded, encoded.get());
    }
}
