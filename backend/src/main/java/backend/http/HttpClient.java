package backend.http;

import java.util.Map;

/**
 * The {@code HttpClient} interface defines methods for making HTTP requests.
 * It provides methods for sending GET and POST requests with specified headers.
 *
 * @author Eva Ray
 */
public interface HttpClient {

    /**
     * Sends a GET request to the specified URL with the provided headers.
     *
     * @param url     the URL to send the GET request to
     * @param headers a map of headers to include in the request
     * @return the response body as a String
     * @throws Exception if an error occurs while making the request
     */
    String get(String url, Map<String, String> headers) throws Exception;

    /**
     * Sends a POST request to the specified URL with the provided body and headers.
     *
     * @param url     the URL to send the POST request to
     * @param body    the body of the POST request
     * @param headers a map of headers to include in the request
     * @return the response body as a String
     * @throws Exception if an error occurs while making the request
     */
    String post(String url, String body, Map<String, String> headers) throws Exception;
}