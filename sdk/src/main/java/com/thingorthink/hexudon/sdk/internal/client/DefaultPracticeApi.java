package com.thingorthink.hexudon.sdk.internal.client;

import com.thingorthink.hexudon.sdk.api.PracticeApi;
import com.thingorthink.hexudon.sdk.config.HexudonConfig;
import com.thingorthink.hexudon.sdk.exception.HexudonAuthenticationException;
import com.thingorthink.hexudon.sdk.exception.HexudonNetworkException;
import com.thingorthink.hexudon.sdk.exception.HexudonServerException;
import com.thingorthink.hexudon.sdk.exception.HexudonValidationException;
import com.thingorthink.hexudon.sdk.internal.http.HttpExecutor;
import com.thingorthink.hexudon.sdk.internal.http.HttpRequest;
import com.thingorthink.hexudon.sdk.internal.http.HttpResponse;
import com.thingorthink.hexudon.sdk.internal.dto.request.PracticeCopyRequest;
import com.thingorthink.hexudon.sdk.internal.dto.request.PracticeSubmitRequest;
import com.thingorthink.hexudon.sdk.internal.mapper.SubmitActionMapper;
import com.thingorthink.hexudon.sdk.internal.serialization.JacksonMapper;
import com.thingorthink.hexudon.sdk.model.SubmitActions;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link PracticeApi}.
 */
final class DefaultPracticeApi implements PracticeApi {

    private static final String CONTENT_TYPE = "application/json";
    private static final String USER_AGENT = "Hexudon/1.0";

    private static final String PRACTICE_ACTION_PATH = "/api/game/practice/actions";
    private static final String PRACTICE_PEER_PATH = "/api/game/practice/peer";
    private static final String PRACTICE_COPY_PATH = "/api/game/practice/copy";
    private static final String PRACTICE_RESET_PATH = "/api/game/practice/reset";

    private final HttpExecutor httpExecutor;
    private final JacksonMapper mapper;
    private final HexudonConfig config;

    DefaultPracticeApi(
            HttpExecutor httpExecutor,
            JacksonMapper mapper,
            HexudonConfig config
    ) {
        this.httpExecutor = Objects.requireNonNull(httpExecutor, "httpExecutor must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    @Override
    public void submitPracticeActions(
            String gameId,
            SubmitActions actions
    ) {
        validateGameId(gameId);
        Objects.requireNonNull(actions, "actions must not be null");

        var actionDto = SubmitActionMapper.toDto(gameId, actions);

        PracticeSubmitRequest requestDto = new PracticeSubmitRequest(
                gameId,
                actions.day(),
                actionDto.actions()
        );

        byte[] body = mapper.writeValueAsBytes(requestDto);

        HttpRequest request = HttpRequest.post(
                PRACTICE_ACTION_PATH,
                defaultHeaders(),
                body
        );

        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }

        checkResponse(response);
    }

    @Override
    public String getPracticePeerState(
            String gameId
    ) {
        validateGameId(gameId);

        HttpRequest request = HttpRequest.get(
                PRACTICE_PEER_PATH + "?game_id=" + gameId,
                defaultHeaders()
        );

        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }

        checkResponse(response);

        return new String(response.body(), StandardCharsets.UTF_8);
    }

    @Override
    public void copyPracticeState(
            String gameId,
            String fromGameId,
            String fromTeamId,
            int uptoDay
    ) {
        validateGameId(gameId);
        validateNotBlank(fromGameId, "fromGameId");
        validateNotBlank(fromTeamId, "fromTeamId");

        if (uptoDay < 0) {
            throw new HexudonValidationException("uptoDay must be >= 0");
        }

        PracticeCopyRequest requestDto = new PracticeCopyRequest(
                gameId,
                fromGameId,
                fromTeamId,
                uptoDay
        );

        byte[] body = mapper.writeValueAsBytes(requestDto);

        HttpRequest request = HttpRequest.post(
                PRACTICE_COPY_PATH,
                defaultHeaders(),
                body
        );

        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }

        checkResponse(response);
    }

    @Override
    public void resetPractice(
            String gameId
    ) {
        validateGameId(gameId);

        byte[] body = mapper.writeValueAsBytes(
                Map.of("game_id", gameId)
        );

        HttpRequest request = HttpRequest.post(
                PRACTICE_RESET_PATH,
                defaultHeaders(),
                body
        );

        HttpResponse response;
        try {
            response = httpExecutor.execute(request);
        } catch (java.io.IOException e) {
            throw new HexudonNetworkException("Network communication failed", e);
        }

        checkResponse(response);
    }

    private Map<String, String> defaultHeaders() {
        return Map.of(
                "Content-Type", CONTENT_TYPE,
                "Authorization", "Bearer " + config.token(),
                "User-Agent", USER_AGENT,
                "X-Team-Name", config.teamId()
        );
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
        validateNotBlank(gameId, "gameId");
    }

    private void validateNotBlank(
            String value,
            String name
    ) {
        if (value == null || value.isBlank()) {
            throw new HexudonValidationException(name + " must not be blank");
        }
    }
}
