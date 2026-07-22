package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.adapter.in.rest.auth.RequestContext;
import com.naprock.hexudon.adapter.in.rest.interceptor.RequestAttributes;
import com.naprock.hexudon.application.dto.config.GameConfigResponse;
import com.naprock.hexudon.application.dto.game.SelectAgentTypesRequest;
import com.naprock.hexudon.application.port.in.GetGameConfigUseCase;
import com.naprock.hexudon.application.port.in.SelectAgentTypesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class TeamController {

    private final GetGameConfigUseCase getGameConfigUseCase;
    private final SelectAgentTypesUseCase selectAgentTypesUseCase;

    public TeamController(
            GetGameConfigUseCase getGameConfigUseCase,
            SelectAgentTypesUseCase selectAgentTypesUseCase
    ) {
        this.getGameConfigUseCase = getGameConfigUseCase;
        this.selectAgentTypesUseCase = selectAgentTypesUseCase;
    }

    @GetMapping("/config")
    public GameConfigResponse getConfig(
            @RequestAttribute(RequestAttributes.REQUEST_CONTEXT) RequestContext context,
            @RequestParam("game_id") String gameId
    ) {
        return getGameConfigUseCase.getConfig(
                context.teamId(),
                gameId
        );
    }

    @PostMapping("/agent-types")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void selectAgentTypes(
            @RequestAttribute(RequestAttributes.REQUEST_CONTEXT) RequestContext context,
            @Valid @RequestBody SelectAgentTypesRequest request
    ) {
        selectAgentTypesUseCase.selectAgentTypes(context.teamId(), request);
    }
}
