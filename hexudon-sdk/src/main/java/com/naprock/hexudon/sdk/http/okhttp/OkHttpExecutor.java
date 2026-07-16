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
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;

/**
 * OkHttp based implementation of HttpExecutor.
 *
 * <p>
 * This class hides all OkHttp-specific logic from SDK public API.
 */
final class OkHttpExecutor implements HttpExecutor {

    private final OkHttpClient client;
    private final HexudonConfig config;


    OkHttpExecutor(HexudonConfig config) {

        if (config == null) {
            throw new NullPointerException(
                    "config must not be null"
            );
        }

        this.config = config;
        this.client = createClient(config);
    }


    private OkHttpClient createClient(
            HexudonConfig config
    ) {

        var httpConfig = config.httpClientConfig();

        return new OkHttpClient.Builder()
                .connectTimeout(
                        Duration.ofMillis(httpConfig.connectTimeoutMs())
                )
                .readTimeout(
                        Duration.ofMillis(httpConfig.readTimeoutMs())
                )
                .writeTimeout(
                        Duration.ofMillis(httpConfig.writeTimeoutMs())
                )
                .build();
    }

    @Override
    public HttpResponse execute(
            HttpRequest request
    )
            throws HexudonNetworkException,
            HexudonServerException {

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }


        int maxRetry =
                config.retryConfig()
                        .maxRetries();

        long delay =
                config.retryConfig()
                        .retryDelayMs();


        Exception lastException = null;


        for (int attempt = 0;
             attempt <= maxRetry;
             attempt++) {

            try {

                HttpResponse response =
                        executeInternal(request);


                if (response.statusCode() >= 500) {

                    if (attempt < maxRetry) {

                        sleep(delay);
                        delay *= 2;
                        continue;
                    }


                    throw new HexudonServerException(
                            "Server returned status "
                                    + response.statusCode(),
                            response.statusCode()
                    );
                }


                return response;


            } catch (IOException e) {

                lastException = e;


                if (attempt < maxRetry) {

                    sleep(delay);
                    delay *= 2;
                    continue;
                }


                throw new HexudonNetworkException(
                        "HTTP request failed",
                        e
                );
            }
        }


        throw new HexudonNetworkException(
                "HTTP request failed after retries",
                lastException
        );
    }


    private HttpResponse executeInternal(
            HttpRequest request
    ) throws IOException {


        Request.Builder builder =
                new Request.Builder()
                        .url(buildUrl(request));


        request.headers()
                .forEach(builder::addHeader);


        if (request.body() != null) {

            builder.method(
                    request.method(),
                    RequestBody.create(
                            request.body(),
                            MediaType.parse(
                                    "application/octet-stream"
                            )
                    )
            );

        } else {

            builder.method(
                    request.method(),
                    null
            );
        }


        try (Response response =
                     client.newCall(
                             builder.build()
                     ).execute()) {


            return new HttpResponse(
                    response.code(),
                    response.headers()
                            .toMultimap(),
                    response.body() == null
                            ? null
                            : response.body().bytes()
            );
        }
    }


    private String buildUrl(
            HttpRequest request
    ) {

        HttpUrl.Builder builder =
                HttpUrl.parse(
                                config.baseUrl()
                                        + request.path()
                        )
                        .newBuilder();


        request.queryParams()
                .forEach(
                        builder::addQueryParameter
                );


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
    }
}