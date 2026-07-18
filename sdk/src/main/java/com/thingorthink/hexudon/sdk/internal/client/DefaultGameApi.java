package com.thingorthink.hexudon.sdk.internal.client;

import com.thingorthink.hexudon.sdk.api.GameApi;
import com.thingorthink.hexudon.sdk.config.HexudonConfig;
import com.thingorthink.hexudon.sdk.exception.HexudonAuthenticationException;
import com.thingorthink.hexudon.sdk.exception.HexudonNetworkException;
import com.thingorthink.hexudon.sdk.exception.HexudonServerException;
import com.thingorthink.hexudon.sdk.exception.HexudonValidationException;
import com.thingorthink.hexudon.sdk.internal.dto.request.SubmitActionRequest;
import com.thingorthink.hexudon.sdk.internal.dto.request.TeamRegisterRequest;
import com.thingorthink.hexudon.sdk.internal.dto.response.MatchConfigResponse;
import com.thingorthink.hexudon.sdk.internal.dto.response.MatchStateResponse;
import com.thingorthink.hexudon.sdk.internal.dto.response.DayInfoResponse;
import com.thingorthink.hexudon.sdk.internal.dto.response.GameResultResponse;
import com.thingorthink.hexudon.sdk.internal.http.HttpExecutor;
import com.thingorthink.hexudon.sdk.internal.http.HttpMethod;
import com.thingorthink.hexudon.sdk.internal.http.HttpRequest;
import com.thingorthink.hexudon.sdk.internal.http.HttpResponse;
import com.thingorthink.hexudon.sdk.internal.mapper.MatchConfigMapper;
import com.thingorthink.hexudon.sdk.internal.mapper.MatchStateMapper;
import com.thingorthink.hexudon.sdk.internal.mapper.SubmitActionMapper;
import com.thingorthink.hexudon.sdk.internal.mapper.TeamRegisterMapper;
import com.thingorthink.hexudon.sdk.internal.mapper.DayInfoMapper;
import com.thingorthink.hexudon.sdk.internal.mapper.GameResultMapper;
import com.thingorthink.hexudon.sdk.internal.serialization.JacksonMapper;
import com.thingorthink.hexudon.sdk.model.MatchConfig;
import com.thingorthink.hexudon.sdk.model.MatchState;
import com.thingorthink.hexudon.sdk.model.SubmitActions;
import com.thingorthink.hexudon.sdk.model.TeamRegistration;
import com.thingorthink.hexudon.sdk.model.DayInfo;
import com.thingorthink.hexudon.sdk.model.GameResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link GameApi}.
 */
final class DefaultGameApi implements GameApi {

    private static final String CONTENT_TYPE = "application/json";
    private static final String USER_AGENT = "Hexudon/1.0";

    private final HttpExecutor httpExecutor;
    private final JacksonMapper mapper;
    private final HexudonConfig config;
    private final Map<String, Integer> mapWidthCache = new ConcurrentHashMap<>();

    DefaultGameApi(
            HttpExecutor httpExecutor,
            JacksonMapper mapper,
            HexudonConfig config
    ) {
        this.httpExecutor = Objects.requireNonNull(httpExecutor, "httpExecutor must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    @Override
    public void registerAgentTypes(
            String gameId,
            TeamRegistration registration
    ) {
        validateGameId(gameId);
        Objects.requireNonNull(registration, "registration must not be null");

        TeamRegisterRequest requestDto = TeamRegisterMapper.toDto(gameId, registration);
        byte[] body = mapper.writeValueAsBytes(requestDto);

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/api/game/agent-types")
                .headers(defaultHeaders())
                .body(body)
                .build();

        execute(request);
    }

    @Override
    public MatchConfig getMatchConfig(
            String gameId
    ) {
        validateGameId(gameId);

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/game/config")
                .headers(defaultHeaders())
                .queryParam("game_id", gameId)
                .build();

        MatchConfigResponse response = execute(request, MatchConfigResponse.class);
        mapWidthCache.put(gameId, response.mapWidth());

        return MatchConfigMapper.toDomain(response);
    }

    @Override
    public MatchState getMatchState(
            String gameId
    ) {
        validateGameId(gameId);

        int mapWidth;
        if (mapWidthCache.containsKey(gameId)) {
            mapWidth = mapWidthCache.get(gameId);
        } else {
            mapWidth = getMatchConfig(gameId).mapWidth();
        }

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/game/state")
                .headers(defaultHeaders())
                .queryParam("game_id", gameId)
                .build();

        MatchStateResponse response = execute(request, MatchStateResponse.class);

        return MatchStateMapper.toDomain(response, mapWidth);
    }

    @Override
    public void submitActions(
            String gameId,
            SubmitActions actions
    ) {
        validateGameId(gameId);
        Objects.requireNonNull(actions, "actions must not be null");

        SubmitActionRequest requestDto = SubmitActionMapper.toDto(gameId, actions);
        byte[] body = mapper.writeValueAsBytes(requestDto);

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("/api/game/actions")
                .headers(defaultHeaders())
                .body(body)
                .build();

        execute(request);
    }

    @Override
    public DayInfo getDayInfo(
            String gameId
    ) {
        validateGameId(gameId);

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/game/day")
                .headers(defaultHeaders())
                .queryParam("game_id", gameId)
                .build();

        DayInfoResponse response = execute(request, DayInfoResponse.class);

        return DayInfoMapper.toDomain(response);
    }

    @Override
    public GameResult getGameResult(
            String gameId
    ) {
        validateGameId(gameId);

        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("/api/game/result")
                .headers(defaultHeaders())
                .queryParam("game_id", gameId)
                .build();

        GameResultResponse response = execute(request, GameResultResponse.class);

        return GameResultMapper.toDomain(response);
    }

    private Map<String, String> defaultHeaders() {
        return Map.of(
                "Content-Type", CONTENT_TYPE,
                "Authorization", "Bearer " + config.token(),
                "User-Agent", USER_AGENT
        );
    }

    private void execute(HttpRequest request) {
        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }
        checkResponse(response);
    }

    private <T> T execute(
            HttpRequest request,
            Class<T> responseType
    ) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");

        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }

        checkResponse(response);

        return mapper.readValue(response.body(), responseType);
    }

    private void checkResponse(
            HttpResponse response
    ) {
        Objects.requireNonNull(response, "response must not be null");
        int status = response.statusCode();

        if (status < 400) {
            return;
        }

        String message = new String(response.body(), StandardCharsets.UTF_8);

        if (status == 401 || status == 403) {
            throw new HexudonAuthenticationException(message);
        }

        if (status == 400 || status == 422) {
            HexudonValidationException.ErrorResponse error = null;
            try {
                error = mapper.readValue(response.body(), HexudonValidationException.ErrorResponse.class);
            } catch (Exception ignored) {
            }
            throw new HexudonValidationException(message, error);
        }

        throw new HexudonServerException(message, status);
    }

    private void validateGameId(
            String gameId
    ) {
        if (gameId == null || gameId.isBlank()) {
            throw new HexudonValidationException("gameId must not be blank");
        }
    }
}
