package com.naprock.hexudon.controller;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.application.mapper.ActionMapper;
import com.naprock.hexudon.manager.MatchManager;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchManager matchManager;

    public MatchController(MatchManager matchManager) {
        this.matchManager = Objects.requireNonNull(matchManager, "matchManager must not be null");
    }


    /**
     * POST /api/match/actions
     */

    @PostMapping("/actions")
    public DayActionResponse submitActions(
            @Valid @RequestBody DayActionRequest request,
            @RequestHeader("X-Team-Name") String teamName
    ) {
        return ActionMapper.toDayActionResponse(
                matchManager.submitActions(
                        teamName,
                        request.day(),
                        ActionMapper.toAgentPlans(request)
                )
        );
    }

    /**
     * GET /api/match/state
     */
    @GetMapping("/state")
    public MatchStateResponse getMatchState() {
        return new MatchStateResponse(matchManager.getMatchState());
    }

    /**
     * POST /api/match/register
     */
    @PostMapping("/register")
    public TeamResponse registerTeam(@Valid @RequestBody TeamRegisterRequest request) {
        return new TeamResponse(matchManager.registerTeam(request.teamName()));
    }

    /**
     * POST /api/match/start
     */
    @PostMapping("/start")
    public MatchStateResponse startMatch() {
        matchManager.startMatch();
        return new MatchStateResponse(matchManager.getMatchState());
    }

}