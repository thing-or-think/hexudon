package com.naprock.hexudon.sdk.internal.http;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Immutable representation of an HTTP response received from server.
 *
 * <p>
 * This record encapsulates response information returned by the
 * internal HTTP execution layer:
 * </p>
 *
 * <ul>
 *     <li>HTTP status code.</li>
 *     <li>Response headers.</li>
 *     <li>Binary response body.</li>
 * </ul>
 *
 * <p>
 * This class is part of the internal HTTP abstraction and is not
 * exposed to SDK consumers.
 * </p>
 *
 * @param statusCode HTTP status code
 * @param headers response headers
 * @param body response payload
 */
public record HttpResponse(

        int statusCode,

        Map<String, List<String>> headers,

        byte[] body

) {


    /**
     * Compact constructor.
     *
     * <p>
     * Ensures immutability by:
     * </p>
     *
     * <ul>
     *     <li>Rejecting null headers.</li>
     *     <li>Creating immutable header copies.</li>
     *     <li>Cloning response body.</li>
     * </ul>
     *
     * @throws NullPointerException
     *         if headers is null
     */
    public HttpResponse {

        Objects.requireNonNull(
                headers,
                "headers must not be null"
        );


        headers =
                headers.entrySet()
                        .stream()
                        .collect(
                                java.util.stream.Collectors.toUnmodifiableMap(
                                        Map.Entry::getKey,
                                        entry -> List.copyOf(
                                                entry.getValue()
                                        )
                                )
                        );


        if (body != null) {

            body =
                    body.clone();
        }
    }



    /**
     * Returns response body safely.
     *
     * <p>
     * A cloned array is returned to prevent external mutation.
     * </p>
     *
     * @return cloned response body or null
     */
    @Override
    public byte[] body() {

        return body == null
                ? null
                : body.clone();
    }
}