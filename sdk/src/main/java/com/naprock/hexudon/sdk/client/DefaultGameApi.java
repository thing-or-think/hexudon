package com.naprock.hexudon.sdk.client;

import com.naprock.hexudon.sdk.api.GameApi;
import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.exception.HexudonAuthenticationException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.exception.HexudonValidationException;
import com.naprock.hexudon.sdk.exception.HexudonValidationException.ErrorResponse;
import com.naprock.hexudon.sdk.http.HttpExecutor;
import com.naprock.hexudon.sdk.http.HttpMethod;
import com.naprock.hexudon.sdk.http.HttpRequest;
import com.naprock.hexudon.sdk.http.HttpResponse;
import com.naprock.hexudon.sdk.model.request.SubmitActionRequest;
import com.naprock.hexudon.sdk.model.request.TeamRegisterRequest;
import com.naprock.hexudon.sdk.model.response.MatchConfigResponse;
import com.naprock.hexudon.sdk.model.response.MatchStateResponse;
import com.naprock.hexudon.sdk.serialization.JacksonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public final class DefaultGameApi implements GameApi {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String TEAM_ID_HEADER = "X-Team-Id";

    private static final String APPLICATION_JSON = "application/json";
    private static final String USER_AGENT = "Hexudon/1.0";
    private static final String BEARER_PREFIX = "Bearer ";

    private final HttpExecutor httpExecutor;
    private final JacksonMapper mapper;
    private final HexudonConfig config;


    public DefaultGameApi(
            HttpExecutor httpExecutor,
            JacksonMapper mapper,
            HexudonConfig config
    ) {
        this.httpExecutor = Objects.requireNonNull(httpExecutor, "httpExecutor must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
    }


    @Override
    public void registerTeam(TeamRegisterRequest request) {

        Objects.requireNonNull(request);

        HttpRequest httpRequest = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/api/game/agent-types")
                .headers(defaultHeaders())
                .body(mapper.writeValueAsBytes(request))
                .build();

        HttpResponse response = httpExecutor.execute(httpRequest);

        checkResponse(response);
    }


    @Override
    public MatchConfigResponse getMatchConfig(String gameId) {

        Objects.requireNonNull(gameId, "gameId must not be null");

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/game/config")
                .headers(defaultHeaders())
                .queryParameter("game_id", gameId)
                .build();

        return execute(request, MatchConfigResponse.class);
    }


    @Override
    public MatchStateResponse getMatchState(String gameId) {

        Objects.requireNonNull(gameId, "gameId must not be null");

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/game/state")
                .headers(defaultHeaders())
                .queryParameter("game_id", gameId)
                .build();

        return execute(request, MatchStateResponse.class);
    }


    @Override
    public void submitActions(SubmitActionRequest request) {

        Objects.requireNonNull(request);

        HttpRequest httpRequest = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/api/game/actions")
                .headers(defaultHeaders())
                .body(mapper.writeValueAsBytes(request))
                .build();

        HttpResponse response = httpExecutor.execute(httpRequest);

        checkResponse(response);
    }


    private Map<String, String> defaultHeaders() {

        return Map.of(
                CONTENT_TYPE_HEADER, APPLICATION_JSON,
                AUTHORIZATION_HEADER,
                BEARER_PREFIX + config.token(),
                USER_AGENT_HEADER,
                USER_AGENT,
                TEAM_ID_HEADER,
                config.teamId()
        );
    }


    private <T> T execute(
            HttpRequest request,
            Class<T> responseType
    ) {

        HttpResponse response = httpExecutor.execute(request);

        checkResponse(response);

        return mapper.readValue(
                response.body(),
                responseType
        );
    }


    private void checkResponse(HttpResponse response) {
        Objects.requireNonNull(response, "response must not be null");

        if (response.statusCode() < 400) {
            return;
        }

        int statusCode = response.statusCode();
        String message = new String(response.body(), StandardCharsets.UTF_8);

        switch (statusCode) {
            case 401, 403 ->
                    throw new HexudonAuthenticationException(message);

            case 400, 422 -> {
                ErrorResponse errorResponse = parseValidationError(message);
                throw new HexudonValidationException(message, errorResponse);
            }

            default ->
                    throw new HexudonServerException(message, statusCode);
        }
    }

    private ErrorResponse parseValidationError(String json) {
        return mapper.readValue(json, ErrorResponse.class);
    }
}