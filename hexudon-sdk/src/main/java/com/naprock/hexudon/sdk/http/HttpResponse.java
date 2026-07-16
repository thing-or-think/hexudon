package com.naprock.hexudon.sdk.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable HTTP response data object used internally by Hexudon SDK.
 *
 * <p>
 * Contains raw HTTP response information before converting
 * response body into SDK DTO objects.
 *
 * <p>
 * This record protects mutable fields to guarantee immutability.
 */
public record HttpResponse(
        int statusCode,
        Map<String, List<String>> headers,
        byte[] body
) {

    /**
     * Creates an immutable HTTP response.
     *
     * @param statusCode HTTP response status code
     * @param headers response headers
     * @param body response payload bytes
     */
    public HttpResponse {

        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException(
                    "Invalid HTTP status code: " + statusCode
            );
        }

        headers = headers == null
                ? Collections.emptyMap()
                : copyHeaders(headers);

        body = body == null
                ? null
                : body.clone();
    }

    /**
     * Returns a defensive copy of response headers.
     *
     * @return immutable response headers
     */
    @Override
    public Map<String, List<String>> headers() {
        return headers;
    }

    /**
     * Returns a defensive copy of response body.
     *
     * @return response bytes or null
     */
    @Override
    public byte[] body() {
        return body == null
                ? null
                : body.clone();
    }

    private static Map<String, List<String>> copyHeaders(
            Map<String, List<String>> source
    ) {

        return source.entrySet()
                .stream()
                .collect(
                        java.util.stream.Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                entry -> List.copyOf(entry.getValue())
                        )
                );
    }

    /**
     * Checks whether HTTP response indicates success.
     *
     * @return true when status code is 2xx
     */
    boolean isSuccessful() {
        return statusCode >= 200
                && statusCode < 300;
    }
}