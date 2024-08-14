package edu.kit.datamanager.ro_crate.special;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

import com.apicatalog.jsonld.uri.UriUtils;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class defines methods regarding URIs in general, which in RO-Crate
 * context means usually a valid, resolvable URL or a relative file path.
 *
 * The purpose is to have a simple abstraction where the way e.g. a URL is
 * checked can be changed and tested easily for the whole library.
 */
public class UriUtil {

    /**
     * Hidden constructor, as this class only has static methods.
     */
    private UriUtil() {
    }

    /**
     * Returns true, if the given String can not be used as an identifier in
     * RO-Crate.
     *
     * @param uri the given URI. Usually a URL or relative file path.
     * @return true if url is decoded, false if it is not.
     */
    public static boolean isNotValidUri(String uri) {
        return !isValidUri(uri);
    }

    /**
     * Returns true, if the given String is encoded and can be used as an
     * identifier in RO-Crate.
     *
     * @param uri the given URI. Usually a URL or relative file path.
     * @return trie if the url is encoded, false if it is not.
     */
    public static boolean isValidUri(String uri) {
        return UriUtils.isURI(uri) || isLdBlankNode(uri);
    }

    /**
     * Returns true, if the given string is a url.
     *
     * @param uri the given string
     * @return true if it is a url, false otherwise.
     */
    public static boolean isUrl(String uri) {
        try {
            return asUrl(encode(uri).get()).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tests if the given String is a URL, and if so, returns it.
     *
     * @param uriAsString the given String which will be tested.
     * @return the url, if it is one.
     */
    public static Optional<URL> asUrl(String uriAsString) {
        try {
            URI uri = new URI(uriAsString);
            if (uri.isAbsolute()) {
                return Optional.of(uri.toURL());
            }
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new IllegalArgumentException("This Data Entity remote ID does not resolve to a valid URL.");
        }
        return Optional.empty();
    }

    /**
     * Returns true, if the given string is a file path.
     *
     * @param uri the given string
     * @return true if it is a path, false otherwise.
     */
    public static boolean isPath(String uri) {
        return asPath(uri).isPresent();
    }

    /**
     * Tests if the given String is a file path, and if so, returns it.
     *
     * @param uri the given String which will be tested.
     * @return the path, if it is one.
     */
    public static Optional<Path> asPath(String uri) {
        try {
            Path u = Path.of(uri);
            if (!isUrl(uri)) {
                return Optional.of(u);
            }
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Encodes a string using the description if the ro-crate-specification.
     *
     * @param uri the string to encode.
     * @return the encoded version of the given string, if possible.
     */
    public static Optional<String> encode(String uri) {
        String result = uri;
        // according to
        // https://www.researchobject.org/ro-crate/1.1/data-entities.html#encoding-file-paths
        // a file path may not be fully encoded and may contain international unicode
        // characters. So we try a "soft"-encoding first, and if this is not yet valid,
        // we really fully encode it.

        result = result.replace("\\", "/");
        result = result.replace("%", "%25");
        result = result.replace(" ", "%20");
        return Optional.of(result);
    }

    public static Optional<String> decode(String uri) {
        if (isNotValidUri(uri) || isLdBlankNode(uri)) {
            return Optional.of(uri);
        }
        return Optional.of(URLDecoder.decode(uri, StandardCharsets.UTF_8));
    }

    /**
     * Returns true if the given string is a blank node identifier in the Linked
     * Data world.
     *
     * @param uri the given String
     * @return true if the string is a blank node. False if not.
     */
    private static boolean isLdBlankNode(String uri) {
        return uri.startsWith("_:");
    }

    /**
     * Returns true, if the URLs domain exists.
     *
     * @param url the given URL
     * @return true if domain exists.
     */
    public static boolean hasValidDomain(String url) {
        if (isNotValidUri(url)) {
            String encoded = encode(url).get();
            return asUrl(encoded) != null;
        }
        return asUrl(url) != null;
    }

    /**
     * checks if the given string is correctly encoded.
     *
     * @param uri the given string
     * @return true if the given string is correctly encoded.
     */
    public static boolean isEncoded(String uri) {
        String decoded = decode(uri).get();
        Optional<String> encoded = encode(decoded);
        String result = null;
        if (encoded.isPresent()) {
            result = encoded.get();
        }
        return uri.equals(result);
    }
}
