package com.thingorthink.hexudon.sdk.internal.http;

import java.io.IOException;


/**
 * Abstraction for synchronous HTTP request execution.
 *
 * <p>
 * This interface hides the underlying HTTP client implementation
 * from SDK business APIs.
 * </p>
 *
 * <p>
 * Implementations are responsible for:
 * </p>
 *
 * <ul>
 *     <li>Sending HTTP requests.</li>
 *     <li>Receiving HTTP responses.</li>
 *     <li>Managing network resources.</li>
 * </ul>
 *
 * <p>
 * Typical implementation:
 * </p>
 *
 * <pre>
 * HttpExecutor
 *       |
 *       +---- OkHttpExecutor
 * </pre>
 *
 * <p>
 * This interface is internal and not exposed to SDK consumers.
 * </p>
 *
 * @see HttpRequest
 * @see HttpResponse
 */
public interface HttpExecutor extends AutoCloseable {



    /**
     * Executes a synchronous HTTP request.
     *
     * <p>
     * The implementation should send the given request and return
     * the received response.
     * </p>
     *
     * @param request HTTP request definition
     *
     * @return HTTP response
     *
     * @throws IOException when network communication fails
     *
     * @throws NullPointerException
     *         if request is null
     */
    HttpResponse execute(
            HttpRequest request
    ) throws IOException;



    /**
     * Releases resources held by this executor.
     *
     * <p>
     * Implementations should close underlying HTTP clients,
     * connection pools, thread pools, or other resources.
     * </p>
     *
     * @throws Exception when resource closing fails
     */
    @Override
    void close() throws Exception;
}
