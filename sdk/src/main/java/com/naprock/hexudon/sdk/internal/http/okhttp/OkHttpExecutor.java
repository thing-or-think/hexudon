package com.naprock.hexudon.sdk.internal.http.okhttp;

import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.internal.http.HttpExecutor;
import com.naprock.hexudon.sdk.internal.http.HttpRequest;
import com.naprock.hexudon.sdk.internal.http.HttpResponse;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.HttpUrl;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * OkHttp based implementation of {@link HttpExecutor}.
 *
 * <p>
 * This class is responsible for:
 * </p>
 *
 * <ul>
 *     <li>Managing OkHttpClient lifecycle.</li>
 *     <li>Converting internal {@link HttpRequest} into OkHttp requests.</li>
 *     <li>Executing synchronous HTTP calls.</li>
 *     <li>Applying retry with exponential backoff.</li>
 * </ul>
 *
 * <p>
 * Retry is applied only for:
 * </p>
 *
 * <ul>
 *     <li>Network failures.</li>
 *     <li>HTTP 5xx server errors.</li>
 * </ul>
 *
 * <p>
 * HTTP 4xx responses are returned immediately because they represent
 * client-side errors.
 * </p>
 *
 * <p>
 * OkHttpClient is thread-safe, therefore this executor can safely be
 * shared between multiple API clients.
 * </p>
 */
public final class OkHttpExecutor implements HttpExecutor {


    private static final MediaType JSON_MEDIA_TYPE =
            MediaType.get("application/json");


    private final OkHttpClient client;

    private final HexudonConfig config;



    /**
     * Creates OkHttpExecutor.
     *
     * @param config SDK configuration
     *
     * @throws NullPointerException if config is null
     */
    public OkHttpExecutor(
            HexudonConfig config
    ) {

        this.config =
                Objects.requireNonNull(
                        config,
                        "config must not be null"
                );


        this.client =
                createClient(config);
    }



    /**
     * Creates configured OkHttpClient.
     *
     * @param config SDK configuration
     *
     * @return configured OkHttpClient
     */
    private OkHttpClient createClient(
            HexudonConfig config
    ) {

        var httpConfig =
                config.httpClientConfig();


        return new OkHttpClient.Builder()

                .connectTimeout(
                        Duration.ofMillis(
                                httpConfig.connectTimeoutMs()
                        )
                )

                .readTimeout(
                        Duration.ofMillis(
                                httpConfig.readTimeoutMs()
                        )
                )

                .writeTimeout(
                        Duration.ofMillis(
                                httpConfig.writeTimeoutMs()
                        )
                )

                .build();
    }



    /**
     * Executes HTTP request with retry policy.
     *
     * @param request internal HTTP request
     *
     * @return HTTP response
     *
     * @throws HexudonNetworkException
     *         when network communication fails
     * @throws HexudonServerException
     *         when server remains unavailable
     */
    @Override
    public HttpResponse execute(
            HttpRequest request
    ) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );


        var retryConfig =
                config.retryConfig();


        int maxRetries =
                retryConfig.maxRetries();


        long delay =
                retryConfig.retryDelayMs();


        double multiplier =
                retryConfig.retryMultiplier();



        for (int attempt = 0;
             attempt <= maxRetries;
             attempt++) {


            try {

                HttpResponse response =
                        executeInternal(request);


                if (response.statusCode() < 500) {

                    return response;
                }


                if (attempt == maxRetries) {

                    throw new HexudonServerException(
                            "Server error after retries",
                            response.statusCode()
                    );
                }


            } catch (IOException e) {


                if (attempt == maxRetries) {

                    throw new HexudonNetworkException(
                            "Network error after retries",
                            e
                    );
                }
            }


            sleep(delay);


            delay =
                    (long) (
                            delay * multiplier
                    );
        }


        throw new HexudonNetworkException(
                "Unable to execute request"
        );
    }



    /**
     * Executes one HTTP request without retry.
     *
     * @param request internal HTTP request
     *
     * @return HTTP response
     *
     * @throws IOException network error
     */
    private HttpResponse executeInternal(
            HttpRequest request
    ) throws IOException {


        String url =
                buildUrl(request);


        Request.Builder builder =
                new Request.Builder()
                        .url(url);



        request.headers()
                .forEach(
                        builder::addHeader
                );


        RequestBody body =
                request.body() == null
                        ? null
                        : RequestBody.create(
                        request.body(),
                        JSON_MEDIA_TYPE
                );



        switch (request.method()) {

            case GET ->
                    builder.get();

            case POST ->
                    builder.post(
                            body != null
                                    ? body
                                    : RequestBody.create(
                                    new byte[0],
                                    JSON_MEDIA_TYPE
                            )
                    );

            case PUT ->
                    builder.put(body);

            case PATCH ->
                    builder.patch(body);

            case DELETE ->
                    builder.delete(body);

        }


        Request okhttpRequest =
                builder.build();



        try (
                Response response =
                        client.newCall(okhttpRequest)
                                .execute()
        ) {

            byte[] responseBody =
                    response.body() == null
                            ? null
                            : response.body()
                            .bytes();


            Map<String, List<String>> headers =
                    response.headers()
                            .toMultimap();



            return new HttpResponse(
                    response.code(),
                    headers,
                    responseBody
            );
        }
    }



    /**
     * Builds full URL from request path and query parameters.
     *
     * @param request HTTP request
     *
     * @return full URL
     */
    private String buildUrl(
            HttpRequest request
    ) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );


        String baseUrl =
                config.baseUrl()
                        .replaceAll(
                                "/$",
                                ""
                        );


        String path =
                request.path()
                        .replaceAll(
                                "^/",
                                ""
                        );


        HttpUrl.Builder builder =
                Objects.requireNonNull(
                                HttpUrl.parse(
                                        baseUrl + "/" + path
                                )
                        )
                        .newBuilder();



        request.queryParams()
                .forEach(
                        builder::addQueryParameter
                );


        return builder.build()
                .toString();
    }



    /**
     * Sleeps between retry attempts.
     *
     * @param millis delay duration
     *
     * @throws HexudonNetworkException
     *         when thread is interrupted
     */
    private void sleep(
            long millis
    ) {

        try {

            Thread.sleep(millis);

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();


            throw new HexudonNetworkException(
                    "Retry interrupted",
                    e
            );
        }
    }



    /**
     * Releases OkHttp resources.
     */
    @Override
    public void close() {


        client.dispatcher()
                .executorService()
                .shutdown();


        client.connectionPool()
                .evictAll();
    }
}
