package com.naprock.hexudon.adapter.in.rest;

import com.naprock.hexudon.application.dto.admin.*;
import com.naprock.hexudon.application.port.in.AddTeamUseCase;
import com.naprock.hexudon.application.port.in.DeleteGameUseCase;
import com.naprock.hexudon.application.port.in.GenerateMapUseCase;
import com.naprock.hexudon.application.port.in.InitializeGameUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@Validated
public class AdminController {

    private final GenerateMapUseCase generateMapUseCase;
    private final InitializeGameUseCase initializeGameUseCase;
    private final DeleteGameUseCase deleteGameUseCase;
    private final AddTeamUseCase addTeamUseCase;

    public AdminController(
            GenerateMapUseCase generateMapUseCase,
            InitializeGameUseCase initializeGameUseCase,
            DeleteGameUseCase deleteGameUseCase,
            AddTeamUseCase addTeamUseCase
    ) {
        this.generateMapUseCase = generateMapUseCase;
        this.initializeGameUseCase = initializeGameUseCase;
        this.deleteGameUseCase = deleteGameUseCase;
        this.addTeamUseCase = addTeamUseCase;
    }

    @PostMapping("/generate")
    public GenerateMapResponse generateBoard(
            @Valid @RequestBody GenerateMapRequest request
    ) {
        return generateMapUseCase.generate(request);
    }

    @PostMapping("/init")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void initializeGame(
            @Valid @RequestBody InitGameRequest request
    ) {
        initializeGameUseCase.initialize(request);
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(
            @PathVariable String gameId
    ) {
        deleteGameUseCase.deleteGame(gameId);
    }

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public AddTeamResponse addTeam(
            @Valid @RequestBody AddTeamRequest request
    ) {
        return addTeamUseCase.addTeam(request);
    }
}