package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.application.dto.match.MatchConfigResponse;
import com.naprock.hexudon.application.dto.match.MatchStateResponse;
import com.naprock.hexudon.application.dto.match.SubmitActionRequest;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import com.naprock.hexudon.application.dto.team.TeamResponse;
import com.naprock.hexudon.application.port.in.GetMatchConfigUseCase;
import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@Validated
public class MatchController {

    private final RegisterTeamUseCase registerTeamUseCase;
    private final SubmitActionsUseCase submitActionsUseCase;
    private final GetMatchStateUseCase getMatchStateUseCase;
    private final GetMatchConfigUseCase getMatchConfigUseCase;

    public MatchController(
            RegisterTeamUseCase registerTeamUseCase,
            SubmitActionsUseCase submitActionsUseCase,
            GetMatchStateUseCase getMatchStateUseCase,
            GetMatchConfigUseCase getMatchConfigUseCase
    ) {
        this.registerTeamUseCase = registerTeamUseCase;
        this.submitActionsUseCase = submitActionsUseCase;
        this.getMatchStateUseCase = getMatchStateUseCase;
        this.getMatchConfigUseCase = getMatchConfigUseCase;
    }


    /**
     * Register team and select agent type.
     */
    @PostMapping("/agent-types")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAgentType(
            @RequestHeader("X-Team-Id")
            @NotBlank String teamId,

            @Valid
            @RequestBody TeamRegisterRequest request
    ) {
        registerTeamUseCase.registerTeam(teamId, request);
    }


    /**
     * Get public match configuration.
     */
    @GetMapping("/config")
    public MatchConfigResponse getMatchConfig() {
        return getMatchConfigUseCase.getMatchConfig();
    }


    /**
     * Get current match state for a team.
     */
    @GetMapping("/state")
    public MatchStateResponse getMatchState(
            @RequestHeader("X-Team-Id")
            @NotBlank String teamId
    ) {
        return getMatchStateUseCase.getMatchState(teamId);
    }


    /**
     * Submit actions for current day.
     */
    @PostMapping("/actions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void submitActions(
            @RequestHeader("X-Team-Id")
            @NotBlank String teamId,

            @Valid
            @RequestBody SubmitActionRequest request
    ) {
        submitActionsUseCase.submitActions(teamId, request);
    }
}