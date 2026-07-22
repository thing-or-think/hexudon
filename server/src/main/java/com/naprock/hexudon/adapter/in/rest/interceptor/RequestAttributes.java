package com.naprock.hexudon.adapter.in.rest.interceptor;

/**
 * Defines request attribute keys used within the HTTP request lifecycle.
 *
 * <p>These constants are used by interceptors, filters, and controllers
 * to store and retrieve request-scoped data from {@code HttpServletRequest}.
 *
 * <p>This is a utility class and cannot be instantiated.
 */
public final class RequestAttributes {

    /**
     * Request attribute key for the authenticated request context.
     */
    public static final String REQUEST_CONTEXT = "REQUEST_CONTEXT";

    private RequestAttributes() {
        throw new AssertionError("Utility class should not be instantiated.");
    }
}