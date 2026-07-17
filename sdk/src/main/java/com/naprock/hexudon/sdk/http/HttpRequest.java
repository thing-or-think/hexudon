package com.naprock.hexudon.sdk.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable HTTP request data object used internally by Hexudon SDK.
 *
 * <p>
 * Contains HTTP method, API path, headers, query parameters,
 * and optional binary request body.
 */
public record HttpRequest(
        HttpMethod method,
        String path,
        Map<String, String> headers,
        Map<String, String> queryParams,
        byte[] body
) {

    public HttpRequest {

        Objects.requireNonNull(
                method,
                "method must not be null"
        );

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException(
                    "path must not be null or blank"
            );
        }

        headers = headers == null
                ? Collections.emptyMap()
                : Map.copyOf(headers);

        queryParams = queryParams == null
                ? Collections.emptyMap()
                : Map.copyOf(queryParams);

        body = body == null
                ? null
                : body.clone();
    }


    @Override
    public byte[] body() {
        return body == null
                ? null
                : body.clone();
    }


    /**
     * Creates HttpRequest builder.
     */
    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        private HttpMethod method;
        private String path;

        private final Map<String, String> headers =
                new HashMap<>();

        private final Map<String, String> queryParams =
                new HashMap<>();

        private byte[] body;


        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }


        public Builder path(String path) {
            this.path = path;
            return this;
        }


        public Builder header(
                String key,
                String value
        ) {
            this.headers.put(key, value);
            return this;
        }


        public Builder headers(
                Map<String, String> headers
        ) {
            if (headers != null) {
                this.headers.putAll(headers);
            }

            return this;
        }


        public Builder queryParameter(
                String key,
                String value
        ) {
            this.queryParams.put(key, value);
            return this;
        }


        public Builder queryParameters(
                Map<String, String> params
        ) {
            if (params != null) {
                this.queryParams.putAll(params);
            }

            return this;
        }


        public Builder body(byte[] body) {
            this.body = body;
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
}