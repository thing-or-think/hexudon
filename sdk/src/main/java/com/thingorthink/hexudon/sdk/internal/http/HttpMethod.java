package com.thingorthink.hexudon.sdk.internal.http;


/**
 * HTTP request methods supported by Hexudon SDK internal HTTP layer.
 *
 * <p>
 * This enum represents standard HTTP verbs used when communicating
 * with Hexudon Game Server REST APIs.
 * </p>
 *
 * <p>
 * This type is package-private because it is only used internally
 * by HTTP request construction and execution.
 * </p>
 *
 * <pre>
 * HttpRequest
 *      |
 *      +---- HttpMethod.GET
 *      +---- HttpMethod.POST
 *      +---- HttpMethod.PUT
 *      +---- HttpMethod.PATCH
 *      +---- HttpMethod.DELETE
 * </pre>
 */
public enum HttpMethod {


    /**
     * HTTP GET request.
     *
     * <p>
     * Typically used for retrieving resources.
     * </p>
     */
    GET,


    /**
     * HTTP POST request.
     *
     * <p>
     * Typically used for creating resources or submitting actions.
     * </p>
     */
    POST,


    /**
     * HTTP PUT request.
     *
     * <p>
     * Typically used for replacing resources.
     * </p>
     */
    PUT,


    /**
     * HTTP PATCH request.
     *
     * <p>
     * Typically used for partial resource updates.
     * </p>
     */
    PATCH,


    /**
     * HTTP DELETE request.
     *
     * <p>
     * Typically used for removing resources.
     * </p>
     */
    DELETE
}
