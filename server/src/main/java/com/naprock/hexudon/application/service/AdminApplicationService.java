package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.admin.*;
import com.naprock.hexudon.application.port.in.AddTeamUseCase;
import com.naprock.hexudon.application.port.in.DeleteGameUseCase;
import com.naprock.hexudon.application.port.in.GenerateMapUseCase;
import com.naprock.hexudon.application.port.in.InitializeGameUseCase;
import com.naprock.hexudon.application.port.out.file.MatchConfigRepository;
import com.naprock.hexudon.application.port.out.match.MatchRepository;
import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.board.MapGenerationConfig;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.service.MapGeneratorService;
import com.naprock.hexudon.infrastructure.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import static com.naprock.hexudon.application.mapper.AdminMapper.*;

@Service
public class AdminApplicationService implements
        GenerateMapUseCase,
        InitializeGameUseCase,
        DeleteGameUseCase,
        AddTeamUseCase
{

    private final MatchConfigRepository matchConfigRepository;
    private final MapGeneratorService mapGeneratorService;
    private final MatchRepository matchRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminApplicationService(
            MapGeneratorService mapGeneratorService,
            MatchConfigRepository matchConfigRepository,
            MatchRepository matchRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.mapGeneratorService = mapGeneratorService;
        this.matchConfigRepository = matchConfigRepository;
        this.matchRepository = matchRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public GenerateMapResponse generate(GenerateMapRequest request) {
        MapGenerationConfig mapGenerationConfig = toMapGenerationConfig(request);
        BoardConfig boardConfig = mapGeneratorService.generate(mapGenerationConfig);
        return toGenerateMapResponse(boardConfig);
    }

    @Override
    public void initialize(InitGameRequest request) {
        MatchConfig config = toMatchConfig(request);
        matchConfigRepository.save(config);
    }

    @Override
    public void deleteGame(String gameId) {
        matchConfigRepository.deleteByGameId(gameId);
    }

    @Override
    public AddTeamResponse addTeam(AddTeamRequest request) {
        return toAddTeamResponse(request.teamId(), jwtTokenProvider.generateTeamToken(request.teamId()));
    }
}
