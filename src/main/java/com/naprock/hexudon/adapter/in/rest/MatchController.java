package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.application.mapper.MatchMapper;
import com.naprock.hexudon.application.port.in.GetMatchStateUseCase;
import com.naprock.hexudon.application.port.in.RegisterTeamUseCase;
import com.naprock.hexudon.application.port.in.StartMatchUseCase;
import com.naprock.hexudon.application.port.in.SubmitActionsUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    private final RegisterTeamUseCase registerTeamUseCase;
    private final StartMatchUseCase startMatchUseCase;
    private final SubmitActionsUseCase submitActionsUseCase;
    private final GetMatchStateUseCase getMatchStateUseCase;
    private final MatchMapper matchMapper;

    public MatchController(
            RegisterTeamUseCase registerTeamUseCase,
            StartMatchUseCase startMatchUseCase,
            SubmitActionsUseCase submitActionsUseCase,
            GetMatchStateUseCase getMatchStateUseCase,
            MatchMapper actionMapper
    ) {
        this.registerTeamUseCase = registerTeamUseCase;
        this.startMatchUseCase = startMatchUseCase;
        this.submitActionsUseCase = submitActionsUseCase;
        this.getMatchStateUseCase = getMatchStateUseCase;
        this.matchMapper = actionMapper;
    }

    /**
     * Register a new team.
     *
     * @param request team registration request
     * @return registered team information
     */
    @PostMapping("/register")
    public TeamResponse registerTeam(
            @Valid @RequestBody TeamRegisterRequest request
    ) {
        var result = registerTeamUseCase.registerTeam(
                request.teamName()
        );
        return matchMapper.toTeamResponse(result);
    }

    /**
     * Start the match.
     */
    @PostMapping("/start")
    public void startMatch() {
        startMatchUseCase.startMatch();
    }

    /**
     * Get current match state.
     *
     * @return current match state
     */
    @GetMapping("/state")
    public MatchStateResponse getMatchState() {

        var state = getMatchStateUseCase.getMatchState();

        return matchMapper.toMatchStateResponse(state);
    }

    /**
     * Submit day actions.
     *
     * @param teamName team name from request header
     * @param request submitted action plan
     * @return submission result
     */
    @PostMapping("/actions")
    public DayActionResponse submitActions(
            @RequestHeader("X-Team-Name") String teamName,
            @Valid @RequestBody DayActionRequest request
    ) {

        var plans = matchMapper.toDomainActionPlanMap(request);

        var result = submitActionsUseCase.submitActions(
                teamName,
                request.day(),
                plans
        );

        return matchMapper.toDayActionResponse(result);
    }
}