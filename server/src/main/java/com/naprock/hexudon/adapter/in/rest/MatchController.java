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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    private final RegisterTeamUseCase registerTeamUseCase;
    private final SubmitActionsUseCase submitActionsUseCase;
    private final GetMatchStateUseCase getMatchStateUseCase;
    private final GetMatchConfigUseCase getMatchConfigUseCase;

    public MatchController(
            RegisterTeamUseCase registerTeamUseCase,
            GetMatchConfigUseCase getMatchConfigUseCase,
            SubmitActionsUseCase submitActionsUseCase,
            GetMatchStateUseCase getMatchStateUseCase
    ) {
        this.registerTeamUseCase = registerTeamUseCase;
        this.getMatchConfigUseCase = getMatchConfigUseCase;
        this.submitActionsUseCase = submitActionsUseCase;
        this.getMatchStateUseCase = getMatchStateUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse registerTeam(
            @Valid @RequestBody TeamRegisterRequest request
    ) {
        return registerTeamUseCase.registerTeam(request);
    }

    @GetMapping("/config")
    public MatchConfigResponse getMatchConfig() {
        return getMatchConfigUseCase.getMatchConfig();
    }

    @GetMapping("/state")
    public MatchStateResponse getMatchState(
            @NotBlank
            @RequestHeader("X-Team-Name") String teamName
    ) {
        return getMatchStateUseCase.getMatchState(teamName);
    }

    @PostMapping("/actions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void submitActions(
            @NotBlank
            @RequestHeader("X-Team-Name") String teamName,
            @Valid @RequestBody SubmitActionRequest request
    ) {
        submitActionsUseCase.submitActions(teamName, request);
    }
}