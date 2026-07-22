package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.dto.config.MapResponse;
import com.naprock.hexudon.application.dto.game.GameDayResponse;
import com.naprock.hexudon.application.dto.game.GameListResponse;
import com.naprock.hexudon.application.dto.game.GameResultResponse;
import com.naprock.hexudon.application.dto.game.GameSummaryResponse;
import com.naprock.hexudon.application.dto.team.TeamDetailResponse;
import com.naprock.hexudon.application.dto.traffic.TrafficResponse;
import com.naprock.hexudon.application.dto.state.GameStateResponse;
import com.naprock.hexudon.application.port.out.file.MatchConfigRepository;
import com.naprock.hexudon.application.port.out.match.MatchRepository;
import com.naprock.hexudon.domain.model.match.MatchStatus;
import com.naprock.hexudon.domain.model.score.TeamScore;
import java.time.Instant;
import org.mockito.MockedStatic;
import com.naprock.hexudon.domain.service.TeamRankingService;
import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.business.ResourceNotFoundException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.match.Match;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.traffic.TrafficState;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameApplicationService Unit Tests")
class GameApplicationServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchConfigRepository matchConfigRepository;

    @Spy
    private TeamRankingService teamRankingService = new TeamRankingService();

    @InjectMocks
    private GameApplicationService gameApplicationService;

    private MatchConfig matchConfig;
    private Match match;
    private Team myTeam;
    private Team otherTeam;
    private Agent myPatrolAgent;
    private Agent myRefuelAgent;
    private Agent otherPatrolAgent;

    @BeforeEach
    void setUp() {
        BoardConfig boardConfig = new BoardConfig(
                2, 2,
                List.of(
                        List.of(1, 0),
                        List.of(0, 1)
                ),
                List.of()
        );

        matchConfig = new MatchConfig(
                "game-123",
                1000L,
                List.of(10.0, 20.0),
                List.of(5, 5),
                boardConfig,
                List.of(0, 1),
                100,
                2,
                0.5,
                0.8,
                5.0
        );

        match = new Match(matchConfig);

        myPatrolAgent = new PatrolAgent(new Coordinate(0, 0), 100);
        myRefuelAgent = new RefuelAgent(new Coordinate(1, 0));
        otherPatrolAgent = new PatrolAgent(new Coordinate(0, 1), 100);

        myTeam = new Team("my-team", List.of(myPatrolAgent, myRefuelAgent));
        otherTeam = new Team("other-team", List.of(otherPatrolAgent));
    }

    @Test
    @DisplayName("getGameDay - Success - Returns 200 equivalent and correctly mapped data")
    void getGameDay_success() {
        // Arrange
        long now = 1000L;
        match.openRegistration(now);
        match.registerTeam(myTeam);
        match.registerTeam(otherTeam);
        match.start(now + 10L); // starts match at 1010L

        // Advance to Day 1
        match.finishDay(1020L);

        // Add a custom traffic tracker for Day 1
        TrafficState trafficState00 = new TrafficState(new Coordinate(0, 0), 0, 0, TrafficLevel.CONGESTED);
        TrafficState trafficState11 = new TrafficState(new Coordinate(1, 1), 0, 0, TrafficLevel.BUSY);
        TrafficTracker customTracker = new TrafficTracker(1, Map.of(
                new Coordinate(0, 0), trafficState00,
                new Coordinate(1, 1), trafficState11
        ));
        match.getTrafficHistory().add(customTracker);

        when(matchRepository.findById("game-123")).thenReturn(match);

        // Act
        GameDayResponse response = gameApplicationService.getGameDay("game-123", "my-team");

        // Assert
        assertNotNull(response);
        assertEquals(1040.0, response.endsAt());
        assertEquals(1, response.day());

        // Verify agents mapping (only current team agents, mapped correctly)
        assertEquals(2, response.agents().size());
        
        // Agent 1: Patrol agent at (0,0)
        assertEquals(0, response.agents().get(0).kind());
        assertEquals(0, response.agents().get(0).pos()); // index of (0,0) on 2x2 board is 0
        assertEquals(100, response.agents().get(0).fuel());

        // Agent 2: Refuel agent at (1,0)
        assertEquals(1, response.agents().get(1).kind());
        assertEquals(1, response.agents().get(1).pos()); // index of (1,0) on 2x2 board is 1
        assertEquals(0, response.agents().get(1).fuel());

        // Verify others mapping (does not contain current team, contains other team with correct mapping)
        assertEquals(1, response.others().size());
        assertEquals("other-team", response.others().get(0).id());
        assertEquals(1, response.others().get(0).agents().size());
        assertEquals(0, response.others().get(0).agents().get(0).kind());
        assertEquals(2, response.others().get(0).agents().get(0).pos()); // index of (0,1) is 2
        assertEquals(100, response.others().get(0).agents().get(0).fuel());

        // Verify traffic mapping
        assertEquals(2, response.traffics().size());
        TrafficResponse t0 = response.traffics().stream().filter(t -> t.pos() == 0).findFirst().orElse(null);
        TrafficResponse t1 = response.traffics().stream().filter(t -> t.pos() == 3).findFirst().orElse(null);
        
        assertNotNull(t0);
        assertEquals(2, t0.status()); // CONGESTED order is 2

        assertNotNull(t1);
        assertEquals(1, t1.status()); // BUSY order is 1
    }

    @Test
    @DisplayName("getGameDay - Game not found - Throws ResourceNotFoundException")
    void getGameDay_gameNotFound() {
        // Arrange
        when(matchRepository.findById("non-existent-game")).thenThrow(
                new com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException("Match", "non-existent-game")
        );

        // Act & Assert
        assertThrows(com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException.class, () ->
                gameApplicationService.getGameDay("non-existent-game", "my-team")
        );
    }

    @Test
    @DisplayName("getGameDay - Game is not playing (still in registering state) - Throws GameRuleViolationException")
    void getGameDay_gameNotPlaying() {
        // Arrange
        long now = 1000L;
        match.openRegistration(now);
        match.registerTeam(myTeam);

        when(matchRepository.findById("game-123")).thenReturn(match);

        // Act & Assert
        GameRuleViolationException exception = assertThrows(GameRuleViolationException.class, () ->
                gameApplicationService.getGameDay("game-123", "my-team")
        );
        assertEquals(ErrorCode.MATCH_NOT_PLAYING, exception.getErrorCode());
    }

    @Test
    @DisplayName("getGameDay - Team not registered in the game - Throws ResourceNotFoundException")
    void getGameDay_teamNotRegistered() {
        // Arrange
        long now = 1000L;
        match.openRegistration(now);
        match.registerTeam(otherTeam); // Register only otherTeam
        match.start(now + 10L);

        when(matchRepository.findById("game-123")).thenReturn(match);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                gameApplicationService.getGameDay("game-123", "my-team")
        );
        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("getGameResult - Success - Returns correctly ranked and mapped GameResultResponse")
    void getGameResult_success() {
        // Arrange
        long now = 1000L;
        match.openRegistration(now);
        match.registerTeam(myTeam);
        match.registerTeam(otherTeam);
        match.start(now + 10L); // starts match at 1010L

        // Day 0: myTeam has 1 serving, 2 unique types, 2 daily types, average response time 50ms
        // otherTeam has 0 servings, 1 unique type, 1 daily type, average response time 100ms
        TeamScore myScore = match.getScoreBoard().getTeamScore("my-team");
        myScore.addUdonCollection(1, 10);
        myScore.addUdonCollection(1, 11);
        myScore.incrementServings();
        myScore.addResponseTime(50);

        TeamScore otherScore = match.getScoreBoard().getTeamScore("other-team");
        otherScore.addUdonCollection(1, 20);
        otherScore.addResponseTime(100);

        when(matchRepository.findById("game-123")).thenReturn(match);

        // Act
        GameResultResponse response = gameApplicationService.getGameResult("game-123");

        // Assert
        assertNotNull(response);

        // Verify ranking order: my-team has 2 unique types vs other-team's 1, so my-team should rank 1st
        assertEquals(2, response.ranking().size());
        assertEquals("my-team", response.ranking().get(0));
        assertEquals("other-team", response.ranking().get(1));

        // Verify detail mapping
        TeamDetailResponse myDetail = response.detail().get("my-team");
        assertNotNull(myDetail);
        assertEquals(2, myDetail.distinctTypes());
        assertEquals(2, myDetail.cumulativeDailyTypes());
        assertEquals(1, myDetail.totalServings());
        assertEquals(50.0, myDetail.cumulativeResponseTime());

        TeamDetailResponse otherDetail = response.detail().get("other-team");
        assertNotNull(otherDetail);
        assertEquals(1, otherDetail.distinctTypes());
        assertEquals(1, otherDetail.cumulativeDailyTypes());
        assertEquals(0, otherDetail.totalServings());
        assertEquals(100.0, otherDetail.cumulativeResponseTime());
    }

    @Test
    @DisplayName("getGameResult - Game not found - Throws ResourceNotFoundException")
    void getGameResult_gameNotFound() {
        // Arrange
        when(matchRepository.findById("non-existent-game")).thenThrow(
                new com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException("Match", "non-existent-game")
        );

        // Act & Assert
        assertThrows(com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException.class, () ->
                gameApplicationService.getGameResult("non-existent-game")
        );
    }

    @Test
    @DisplayName("getGameResult - Game is not playing (still in registering state) - Throws GameRuleViolationException")
    void getGameResult_gameNotPlaying() {
        // Arrange
        long now = 1000L;
        match.openRegistration(now);
        match.registerTeam(myTeam);

        when(matchRepository.findById("game-123")).thenReturn(match);

        // Act & Assert
        GameRuleViolationException exception = assertThrows(GameRuleViolationException.class, () ->
                gameApplicationService.getGameResult("game-123")
        );
        assertEquals(ErrorCode.MATCH_NOT_PLAYING, exception.getErrorCode());
    }

    @Test
    @DisplayName("getGameList - Success - Returns GameListResponse matching repository data")
    void getGameList_success() {
        // Arrange
        when(matchConfigRepository.findAll()).thenReturn(List.of(matchConfig));

        // Act
        GameListResponse response = gameApplicationService.getGameList();

        // Assert
        assertNotNull(response);
        assertEquals(1, response.total());
        assertEquals(1, response.games().size());

        GameSummaryResponse summary = response.games().get(0);
        assertEquals("game-123", summary.gameId());
        assertEquals(1000L, summary.startsAt());
        assertEquals(2, summary.players());
        assertEquals(100, summary.fuelLimits());
        assertEquals(5.0, summary.agentSelectionTimeLimit());
        assertEquals(0.5, summary.busyThreshold());
        assertEquals(0.8, summary.jammedThreshold());
        assertEquals(2, summary.totalDays()); // matches daySeconds.size() which is 2

        assertNotNull(summary.map());
        assertEquals(2, summary.map().width());
        assertEquals(2, summary.map().height());
        assertEquals(List.of(List.of(1, 0), List.of(0, 1)), summary.map().cells());
    }

    @Test
    @DisplayName("getGameList - Empty - Returns empty list and total 0")
    void getGameList_empty() {
        // Arrange
        when(matchConfigRepository.findAll()).thenReturn(List.of());

        // Act
        GameListResponse response = gameApplicationService.getGameList();

        // Assert
        assertNotNull(response);
        assertEquals(0, response.total());
        assertTrue(response.games().isEmpty());
    }

    @Test
    @DisplayName("getGameList - Repository throws exception - Exception is propagated")
    void getGameList_repositoryException() {
        // Arrange
        when(matchConfigRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                gameApplicationService.getGameList()
        );
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("getGameState - Success - Returns correctly mapped GameStateResponse and calculates remainingTime")
    void getGameState_success() {
        // Arrange
        long now = 1000L;
        match.openRegistration(now);
        match.registerTeam(myTeam);
        match.registerTeam(otherTeam);
        match.start(now + 10L); // starts match at 1010L

        // Advance to Day 1
        match.finishDay(1020L); // dayEndTime becomes 1040L

        // Add a custom traffic tracker for Day 1
        TrafficState trafficState00 = new TrafficState(new Coordinate(0, 0), 0, 0, TrafficLevel.CONGESTED);
        TrafficState trafficState11 = new TrafficState(new Coordinate(1, 1), 0, 0, TrafficLevel.BUSY);
        TrafficTracker customTracker = new TrafficTracker(1, Map.of(
                new Coordinate(0, 0), trafficState00,
                new Coordinate(1, 1), trafficState11
        ));
        match.getTrafficHistory().add(customTracker);

        // Record some scores
        TeamScore myScore = match.getScoreBoard().getTeamScore("my-team");
        myScore.addUdonCollection(1, 10);
        myScore.incrementServings();
        myScore.addResponseTime(45);

        when(matchRepository.findById("game-123")).thenReturn(match);

        // Act
        Instant fixedInstant = Instant.ofEpochSecond(1035L);
        GameStateResponse response;
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
            // Mock Instant.now() to return a time during Day 1 (starts at 1020L, ends at 1040L)
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

            response = gameApplicationService.getGameState("game-123");
        }

        // Assert
        assertNotNull(response);
        assertEquals(MatchStatus.PLAYING, response.status());
        assertEquals(1, response.currentDay());
        // remainingTime = dayEndTime (1040L) - current time (1035L) = 5L
        assertEquals(5L, response.remainingTime());

        // Verify mapStatus traffic states mapping
        assertEquals(2, response.mapStatus().size());
        TrafficResponse t0 = response.mapStatus().stream().filter(t -> t.pos() == 0).findFirst().orElse(null);
        TrafficResponse t1 = response.mapStatus().stream().filter(t -> t.pos() == 3).findFirst().orElse(null);
        assertNotNull(t0);
        assertEquals(2, t0.status()); // CONGESTED is 2
        assertNotNull(t1);
        assertEquals(1, t1.status()); // BUSY is 1

        // Verify teams mapping
        assertEquals(2, response.teams().size());
        var myTeamState = response.teams().stream().filter(t -> t.teamId().equals("my-team")).findFirst().orElse(null);
        var otherTeamState = response.teams().stream().filter(t -> t.teamId().equals("other-team")).findFirst().orElse(null);

        assertNotNull(myTeamState);
        assertEquals("my-team", myTeamState.teamId());
        assertEquals(1, myTeamState.score().distinctTypes());
        assertEquals(1, myTeamState.score().totalServings());
        assertEquals(45.0, myTeamState.score().cumulativeResponseTime());
        assertEquals(2, myTeamState.agents().size());

        assertNotNull(otherTeamState);
        assertEquals("other-team", otherTeamState.teamId());
        assertEquals(0, otherTeamState.score().distinctTypes());
        assertEquals(0, otherTeamState.score().totalServings());
        assertEquals(1, otherTeamState.agents().size());
    }

    @Test
    @DisplayName("getGameState - Game not found - Throws ResourceNotFoundException")
    void getGameState_gameNotFound() {
        // Arrange
        when(matchRepository.findById("non-existent-game")).thenThrow(
                new com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException("Match", "non-existent-game")
        );

        // Act & Assert
        assertThrows(com.naprock.hexudon.domain.exception.repository.ResourceNotFoundException.class, () ->
                gameApplicationService.getGameState("non-existent-game")
        );
    }
}
