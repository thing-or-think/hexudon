package com.naprock.hexudon.sdk.internal.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Immutable representation of an outgoing HTTP request.
 *
 * <p>
 * This record encapsulates all information required to execute
 * an HTTP call:
 * </p>
 *
 * <ul>
 *     <li>HTTP method.</li>
 *     <li>Relative endpoint path.</li>
 *     <li>HTTP headers.</li>
 *     <li>Query parameters.</li>
 *     <li>Optional binary request body.</li>
 * </ul>
 *
 * <p>
 * This class belongs to the internal HTTP layer and is not exposed
 * to SDK consumers.
 * </p>
 *
 * @param method HTTP method
 * @param path relative API endpoint path
 * @param headers HTTP headers
 * @param queryParams query parameters
 * @param body request payload
 */
public record HttpRequest(

        HttpMethod method,

        String path,

        Map<String, String> headers,

        Map<String, String> queryParams,

        byte[] body

) {


    /**
     * Compact constructor.
     *
     * <p>
     * Ensures request immutability by:
     * </p>
     *
     * <ul>
     *     <li>Rejecting null required fields.</li>
     *     <li>Creating immutable map copies.</li>
     *     <li>Cloning mutable byte arrays.</li>
     * </ul>
     *
     * @throws NullPointerException
     *         if method, path, headers or queryParams is null
     */
    public HttpRequest {

        Objects.requireNonNull(
                method,
                "method must not be null"
        );

        Objects.requireNonNull(
                path,
                "path must not be null"
        );

        Objects.requireNonNull(
                headers,
                "headers must not be null"
        );

        Objects.requireNonNull(
                queryParams,
                "queryParams must not be null"
        );


        headers =
                Map.copyOf(headers);


        queryParams =
                Map.copyOf(queryParams);


        if (body != null) {

            body =
                    body.clone();
        }
    }



    @Override
    public byte[] body() {
        return body == null
                ? null
                : body.clone();
    }

    /**
     * Creates a GET request.
     *
     * @param path relative API path
     * @param headers HTTP headers
     * @return HttpRequest instance
     */
    public static HttpRequest get(String path, Map<String, String> headers) {
        return new HttpRequest(HttpMethod.GET, path, headers, Map.of(), null);
    }

    /**
     * Creates a POST request.
     *
     * @param path relative API path
     * @param headers HTTP headers
     * @param body request body
     * @return HttpRequest instance
     */
    public static HttpRequest post(String path, Map<String, String> headers, byte[] body) {
        return new HttpRequest(HttpMethod.POST, path, headers, Map.of(), body);
    }

    /**
     * Creates a new request builder.
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link HttpRequest}.
     */
    public static final class Builder {

        private HttpMethod method;
        private String path;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final Map<String, String> queryParams = new LinkedHashMap<>();
        private byte[] body;

        private Builder() {
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder queryParam(String name, String value) {
            queryParams.put(name, value);
            return this;
        }

        public Builder queryParams(Map<String, String> queryParams) {
            this.queryParams.putAll(queryParams);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body == null ? null : body.clone();
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(
                    method,
                    path,
                    headers,
                    queryParams,
                    body
            );
        }
    }

    @Override
    public String toString() {
        return "HttpRequest[" +
                "method=" + method +
                ", path=" + path +
                ", headers=" + headers +
                ", queryParams=" + queryParams +
                ", body=" + (
                body == null
                        ? null
                        : new String(body, StandardCharsets.UTF_8)
        ) +
                "]";
    }
}
