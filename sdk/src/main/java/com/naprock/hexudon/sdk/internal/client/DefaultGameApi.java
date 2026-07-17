package com.naprock.hexudon.sdk.internal.client;

import com.naprock.hexudon.sdk.api.GameApi;
import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.exception.HexudonAuthenticationException;
import com.naprock.hexudon.sdk.exception.HexudonNetworkException;
import com.naprock.hexudon.sdk.exception.HexudonServerException;
import com.naprock.hexudon.sdk.exception.HexudonValidationException;
import com.naprock.hexudon.sdk.internal.dto.request.SubmitActionRequest;
import com.naprock.hexudon.sdk.internal.dto.response.MatchConfigResponse;
import com.naprock.hexudon.sdk.internal.dto.response.MatchStateResponse;
import com.naprock.hexudon.sdk.internal.dto.response.TeamResponse;
import com.naprock.hexudon.sdk.internal.http.HttpExecutor;
import com.naprock.hexudon.sdk.internal.http.HttpMethod;
import com.naprock.hexudon.sdk.internal.http.HttpRequest;
import com.naprock.hexudon.sdk.internal.http.HttpResponse;
import com.naprock.hexudon.sdk.internal.mapper.MatchConfigMapper;
import com.naprock.hexudon.sdk.internal.mapper.MatchStateMapper;
import com.naprock.hexudon.sdk.internal.mapper.SubmitActionMapper;
import com.naprock.hexudon.sdk.internal.mapper.TeamRegisterMapper;
import com.naprock.hexudon.sdk.internal.serialization.JacksonMapper;
import com.naprock.hexudon.sdk.model.MatchConfig;
import com.naprock.hexudon.sdk.model.MatchState;
import com.naprock.hexudon.sdk.model.SubmitActions;
import com.naprock.hexudon.sdk.model.Team;
import com.naprock.hexudon.sdk.model.TeamRegistration;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link GameApi}.
 *
 * <p>
 * This class is the internal HTTP client layer of Hexudon SDK.
 * It is responsible for:
 * </p>
 *
 * <ul>
 *     <li>Building HTTP requests to Hexudon Game Server.</li>
 *     <li>Executing network calls through {@link HttpExecutor}.</li>
 *     <li>Deserializing server responses.</li>
 *     <li>Mapping internal DTO objects into public SDK domain models.</li>
 *     <li>Normalizing HTTP errors into SDK exceptions.</li>
 * </ul>
 *
 * <p>
 * This class is package-private because SDK consumers should only interact
 * with {@link GameApi} through {@code HexudonClient}.
 * </p>
 */
final class DefaultGameApi implements GameApi {


    private static final String CONTENT_TYPE = "application/json";
    private static final String USER_AGENT = "Hexudon/1.0";


    private final HttpExecutor httpExecutor;

    private final JacksonMapper mapper;

    private final HexudonConfig config;
    private final Map<String, Integer> mapWidthCache = new java.util.concurrent.ConcurrentHashMap<>();


    /**
     * Creates a new DefaultGameApi instance.
     *
     * @param httpExecutor HTTP execution engine
     * @param mapper JSON serializer/deserializer
     * @param config SDK configuration
     *
     * @throws NullPointerException if any argument is null
     */
    DefaultGameApi(
            HttpExecutor httpExecutor,
            JacksonMapper mapper,
            HexudonConfig config
    ) {
        this.httpExecutor = Objects.requireNonNull(
                httpExecutor,
                "httpExecutor must not be null"
        );

        this.mapper = Objects.requireNonNull(
                mapper,
                "mapper must not be null"
        );

        this.config = Objects.requireNonNull(
                config,
                "config must not be null"
        );
    }


    /**
     * Registers a team and its agents into the game server.
     *
     * @param registration team registration information
     *
     * @return registered team domain model
     *
     * @throws HexudonValidationException when input is invalid
     * @throws HexudonAuthenticationException when authentication fails
     * @throws HexudonNetworkException when network communication fails
     * @throws HexudonServerException when server returns error
     */
    @Override
    public void registerTeam(
            String gameId,
            TeamRegistration registration
    ) {

        Objects.requireNonNull(
                registration,
                "registration must not be null"
        );


        var requestDto =
                TeamRegisterMapper.toDto(registration);


        byte[] body = mapper.writeValueAsBytes(requestDto);


        HttpRequest request =
                HttpRequest.builder()
                        .method(HttpMethod.POST)
                        .path("/api/game/agent-types")
                        .headers(defaultHeaders())
                        .body(body)
                        .build();


        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }


        checkResponse(response);
    }


    /**
     * Retrieves current match configuration.
     *
     * @param gameId game identifier
     *
     * @return match configuration domain model
     */
    @Override
    public MatchConfig getMatchConfig(
            String gameId
    ) {

        validateGameId(gameId);


        HttpRequest request =
                HttpRequest.builder()
                        .method(HttpMethod.GET)
                        .path("/api/game/config")
                        .headers(defaultHeaders())
                        .queryParam("game_id", gameId)
                        .build();


        MatchConfigResponse response =
                execute(
                        request,
                        MatchConfigResponse.class
                );

        mapWidthCache.put(gameId, response.mapWidth());

        return MatchConfigMapper.toDomain(response);
    }


    /**
     * Retrieves current match state.
     *
     * @param gameId game identifier
     *
     * @return match state domain model
     */
    @Override
    public MatchState getMatchState(
            String gameId
    ) {

        validateGameId(gameId);


        HttpRequest request =
                HttpRequest.builder()
                        .method(HttpMethod.GET)
                        .path("/api/game/state")
                        .headers(defaultHeaders())
                        .queryParam("game_id", gameId)
                        .build();


        MatchStateResponse response =
                execute(
                        request,
                        MatchStateResponse.class
                );

        int mapWidth = mapWidthCache.computeIfAbsent(gameId, id -> {
            return getMatchConfig(id).mapWidth();
        });

        return MatchStateMapper.toDomain(
                response,
                mapWidth
        );
    }


    /**
     * Submits agent actions to server.
     *
     * @param actions agent actions
     */
    @Override
    public void submitActions(
            String gameId,
            SubmitActions actions
    ) {

        validateGameId(gameId);

        Objects.requireNonNull(
                actions,
                "actions must not be null"
        );


        SubmitActionRequest requestDto =
                SubmitActionMapper.toDto(actions);


        byte[] body =
                mapper.writeValueAsBytes(requestDto);


        HttpRequest request =
                HttpRequest.builder()
                        .method(HttpMethod.POST)
                        .path("/api/game/actions")
                        .headers(defaultHeaders())
                        .queryParam("game_id", gameId)
                        .body(body)
                        .build();


        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }


        checkResponse(response);
    }



    /**
     * Creates default HTTP headers.
     *
     * @return immutable HTTP header map
     */
    private Map<String, String> defaultHeaders() {

        return Map.of(
                "Content-Type",
                CONTENT_TYPE,

                "Authorization",
                "Bearer " + config.token(),

                "User-Agent",
                USER_AGENT
        );
    }



    /**
     * Validates HTTP response status.
     *
     * @param response HTTP response
     */
    private void checkResponse(
            HttpResponse response
    ) {

        Objects.requireNonNull(
                response,
                "response must not be null"
        );


        int status = response.statusCode();


        if (status < 400) {
            return;
        }


        String message =
                new String(
                        response.body(),
                        StandardCharsets.UTF_8
                );


        if (status == 401 || status == 403) {

            throw new HexudonAuthenticationException(
                    message
            );
        }


        if (status == 400 || status == 422) {

            HexudonValidationException.ErrorResponse error =
                    mapper.readValue(
                            response.body(),
                            HexudonValidationException.ErrorResponse.class
                    );


            throw new HexudonValidationException(
                    message,
                    error
            );
        }


        throw new HexudonServerException(
                message,
                status
        );
    }



    /**
     * Executes HTTP request and converts response into object.
     *
     * @param request HTTP request
     * @param responseType response class
     *
     * @param <T> response type
     *
     * @return deserialized response object
     */
    private <T> T execute(
            HttpRequest request,
            Class<T> responseType
    ) {

        Objects.requireNonNull(
                request,
                "request must not be null"
        );

        Objects.requireNonNull(
                responseType,
                "responseType must not be null"
        );


        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }


        checkResponse(response);


        return mapper.readValue(
                response.body(),
                responseType
        );
    }



    /**
     * Validates game identifier.
     *
     * @param gameId game identifier
     */
    private void validateGameId(
            String gameId
    ) {

        if (gameId == null || gameId.isBlank()) {

            throw new HexudonValidationException(
                    "gameId must not be blank"
            );
        }
    }
}