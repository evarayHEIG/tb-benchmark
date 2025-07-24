package backend.http;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.net.http.HttpClient.Version;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * The {@code JavaHttpClient} class implements the {@link backend.http.HttpClient} interface
 * using Java's built-in HTTP client. It provides methods to perform GET and POST requests
 * with support for custom headers and error handling.
 *
 * @author Eva Ray
 */
public class JavaHttpClient implements backend.http.HttpClient {

    // The HTTP client instance used for making requests
    private final HttpClient client;

    /**
     * Constructs a new {@code JavaHttpClient} instance with default settings.
     * The client is configured to use HTTP/2 and has a connection timeout of 10 seconds.
     */
    public JavaHttpClient() {
        this.client = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Sends an HTTP request with the specified headers and request builder.
     *
     * @param headers         a map of headers to include in the request
     * @param requestBuilder  the builder for the HTTP request
     * @return the response body as a string
     * @throws java.io.IOException if an I/O error occurs during the request
     * @throws InterruptedException if the request is interrupted
     */
    private String send(Map<String, String> headers, HttpRequest.Builder requestBuilder) throws java.io.IOException, InterruptedException {
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        HttpResponse<String> response = client.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("HTTP request failed with status: " + response.statusCode() + ", body: " + response.body());
        }
    }

    /**
     * Sends a GET request to the specified URL with optional headers.
     *
     * @param url     the URL to send the GET request to
     * @param headers a map of headers to include in the request
     * @return the response body as a string
     * @throws Exception if an error occurs during the request
     */
    @Override
    public String get(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));

        return send(headers, requestBuilder);
    }

    /**
     * Sends a POST request to the specified URL with the given body and optional headers.
     *
     * @param url     the URL to send the POST request to
     * @param body    the body of the POST request
     * @param headers a map of headers to include in the request
     * @return the response body as a string
     * @throws Exception if an error occurs during the request
     */
    @Override
    public String post(String url, String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30));

        return send(headers, requestBuilder);
    }
}