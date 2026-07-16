package com.naprock.hexudon.sdk.http;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable HTTP request data object used internally by Hexudon SDK.
 *
 * <p>
 * Contains HTTP method, API path, headers, query parameters,
 * and optional binary request body.
 *
 * <p>
 * This record creates defensive copies of mutable fields
 * to guarantee full immutability.
 */
public record HttpRequest(
        String method,
        String path,
        Map<String, String> headers,
        Map<String, String> queryParams,
        byte[] body
) {

    /**
     * Creates an immutable HTTP request.
     *
     * @param method HTTP method (GET, POST, DELETE, ...)
     * @param path relative API path
     * @param headers HTTP headers
     * @param queryParams URL query parameters
     * @param body request payload bytes
     */
    public HttpRequest {

        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException(
                    "method must not be null or blank"
            );
        }

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

    /**
     * Returns a defensive copy of request body.
     *
     * @return copied body bytes or null
     */
    @Override
    public byte[] body() {
        return body == null
                ? null
                : body.clone();
    }
}