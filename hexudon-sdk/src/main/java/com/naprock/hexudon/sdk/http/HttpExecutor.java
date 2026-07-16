package com.naprock.hexudon.sdk.http;

import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;

/**
 * Internal abstraction for synchronous HTTP communication.
 *
 * <p>
 * This interface hides concrete HTTP client implementation
 * from SDK business logic.
 *
 * <p>
 * Implementations are responsible for:
 * <ul>
 *     <li>Executing HTTP requests.</li>
 *     <li>Handling network failures.</li>
 *     <li>Applying retry policy for server errors.</li>
 *     <li>Managing connection resources.</li>
 * </ul>
 */
public interface HttpExecutor extends AutoCloseable {

    /**
     * Executes a synchronous HTTP request.
     *
     * @param request SDK HTTP request
     * @return SDK HTTP response
     * @throws HexudonNetworkException when network communication fails
     * @throws HexudonServerException when server returns unrecoverable error
     */
    HttpResponse execute(HttpRequest request)
            throws HexudonNetworkException, HexudonServerException;


    /**
     * Releases HTTP client resources.
     */
    @Override
    void close();
}