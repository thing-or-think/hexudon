package com.naprock.hexudon.sdk.http.okhttp;

import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.http.HttpExecutor;
import com.naprock.hexudon.sdk.http.HttpRequest;
import com.naprock.hexudon.sdk.http.HttpResponse;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * OkHttp based implementation of HttpExecutor.
 *
 * <p>
 * This class hides all OkHttp-specific logic from SDK public API.
 */
public final class OkHttpExecutor implements HttpExecutor {

    private static final MediaType BINARY_MEDIA_TYPE =
            MediaType.parse("application/octet-stream");

    private final OkHttpClient client;
    private final HexudonConfig config;


    public OkHttpExecutor(HexudonConfig config) {

        this.config = Objects.requireNonNull(
                config,
                "config must not be null"
        );
        this.client = createClient(config);
    }

    private OkHttpClient createClient(
            HexudonConfig config
    ) {

        var httpConfig = config.httpClientConfig();

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

    @Override
    public HttpResponse execute(
            HttpRequest request
    )
            throws HexudonNetworkException,
            HexudonServerException {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );


        var retryConfig = config.retryConfig();

        int maxRetries =
                retryConfig.maxRetries();

        long delay =
                retryConfig.retryDelayMs();

        IOException lastException = null;


        for (int attempt = 0;
             attempt <= maxRetries;
             attempt++) {

            try {

                HttpResponse response =
                        executeInternal(request);


                /*
                 * Success or client error:
                 * do not retry.
                 */
                if (response.statusCode() < 500) {
                    return response;
                }


                /*
                 * Server error:
                 * retry until max attempts reached.
                 */
                if (attempt == maxRetries) {

                    throw new HexudonServerException(
                            "Server returned error status: "
                                    + response.statusCode(),
                            response.statusCode()
                    );
                }


            } catch (HexudonServerException e) {

                throw e;


            } catch (IOException e) {

                lastException = e;


                if (attempt == maxRetries) {

                    throw new HexudonNetworkException(
                            "Network error after retry attempts.",
                            lastException
                    );
                }
            }


            sleep(delay);


            delay = (long) (
                    delay * retryConfig.retryMultiplier()
            );
        }


        throw new HexudonNetworkException(
                "HTTP request failed after retries.",
                lastException
        );
    }

    private HttpResponse executeInternal(
            HttpRequest request
    ) throws IOException {


        String url = buildUrl(request);


        okhttp3.Request.Builder builder =
                new okhttp3.Request.Builder()
                        .url(url);


        /*
         * Headers
         */
        for (Map.Entry<String, String> header
                : request.headers().entrySet()) {

            builder.addHeader(
                    header.getKey(),
                    header.getValue()
            );
        }


        /*
         * Body
         */
        RequestBody body = null;

        if (request.body() != null) {

            body = RequestBody.create(
                    request.body(),
                    BINARY_MEDIA_TYPE
            );
        }


        builder.method(
                request.method().name(),
                body
        );


        try (okhttp3.Response response =
                     client.newCall(builder.build())
                             .execute()) {


            byte[] responseBody =
                    response.body() != null
                            ? response.body().bytes()
                            : new byte[0];


            return new HttpResponse(
                    response.code(),
                    response.headers().toMultimap(),
                    responseBody
            );
        }
    }



    private String buildUrl(HttpRequest request) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );


        String baseUrl = config.baseUrl();

        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(
                    0,
                    baseUrl.length() - 1
            );
        }


        String path = request.path();

        while (path.startsWith("/")) {
            path = path.substring(1);
        }


        HttpUrl.Builder builder =
                Objects.requireNonNull(
                                HttpUrl.parse(
                                        baseUrl + "/" + path
                                ),
                                "Invalid URL"
                        )
                        .newBuilder();


        /*
         * Query params
         */
        request.queryParams()
                .forEach(builder::addQueryParameter);


        return builder.build()
                .toString();
    }


    private void sleep(long millis) {

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


    @Override
    public void close() {

        client.dispatcher()
                .executorService()
                .shutdown();


        client.connectionPool()
                .evictAll();


        client.cache();
    }
}