package com.naprock.hexudon.sdk.client;

import com.naprock.hexudon.sdk.api.PracticeApi;
import com.naprock.hexudon.sdk.http.HttpExecutor;
import com.naprock.hexudon.sdk.http.HttpMethod;
import com.naprock.hexudon.sdk.http.HttpRequest;
import com.naprock.hexudon.sdk.http.HttpResponse;
import com.naprock.hexudon.sdk.model.request.PracticeCopyRequest;
import com.naprock.hexudon.sdk.model.request.PracticeSubmitRequest;
import com.naprock.hexudon.sdk.serialization.JacksonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

final class DefaultPracticeApi implements PracticeApi {

    private static final String PRACTICE_ACTIONS_ENDPOINT = "/api/game/practice/actions";
    private static final String PRACTICE_PEER_ENDPOINT = "/api/game/practice/peer";
    private static final String PRACTICE_COPY_ENDPOINT = "/api/game/practice/copy";
    private static final String PRACTICE_RESET_ENDPOINT = "/api/game/practice/reset";

    private final HttpExecutor httpExecutor;
    private final JacksonMapper mapper;

    DefaultPracticeApi(HttpExecutor httpExecutor, JacksonMapper mapper) {
        this.httpExecutor = Objects.requireNonNull(httpExecutor, "httpExecutor must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    @Override
    public void submitPracticeActions(PracticeSubmitRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        byte[] body = mapper.writeValueAsBytes(request);

        HttpRequest httpRequest = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path(PRACTICE_ACTIONS_ENDPOINT)
                .body(body)
                .build();

        httpExecutor.execute(httpRequest);
    }

    @Override
    public String getPracticePeerState(String gameId) {
        Objects.requireNonNull(gameId, "gameId must not be null");

        HttpRequest httpRequest = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path(PRACTICE_PEER_ENDPOINT)
                .queryParameter("game_id", gameId)
                .build();

        HttpResponse response = httpExecutor.execute(httpRequest);

        return new String(response.body(), StandardCharsets.UTF_8);
    }

    @Override
    public void copyPracticeState(PracticeCopyRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        byte[] body = mapper.writeValueAsBytes(request);

        HttpRequest httpRequest = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path(PRACTICE_COPY_ENDPOINT)
                .body(body)
                .build();

        httpExecutor.execute(httpRequest);
    }

    @Override
    public void resetPractice(String gameId) {
        Objects.requireNonNull(gameId, "gameId must not be null");

        byte[] body = mapper.writeValueAsBytes(
                Map.of("game_id", gameId)
        );

        HttpRequest httpRequest = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path(PRACTICE_RESET_ENDPOINT)
                .body(body)
                .build();

        httpExecutor.execute(httpRequest);
    }
}