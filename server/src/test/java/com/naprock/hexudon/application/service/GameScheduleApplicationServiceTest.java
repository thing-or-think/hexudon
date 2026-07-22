package com.naprock.hexudon.application.service;

import com.naprock.hexudon.application.port.out.file.MatchConfigRepository;
import com.naprock.hexudon.application.port.out.match.MatchRepository;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.board.BoardConfig;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.match.Match;
import com.naprock.hexudon.domain.model.match.MatchConfig;
import com.naprock.hexudon.domain.model.match.MatchStatus;
import com.naprock.hexudon.domain.model.submission.ActionSubmission;
import com.naprock.hexudon.domain.model.team.CollectResult;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;
import com.naprock.hexudon.domain.service.TrafficCalculationService;
import com.naprock.hexudon.domain.service.TurnActionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameScheduleApplicationService Unit Tests")
class GameScheduleApplicationServiceTest {

    @Mock
    private MatchConfigRepository matchConfigRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TurnActionService turnActionService;

    @Mock
    private TrafficCalculationService trafficCalculationService;

    @InjectMocks
    private GameScheduleApplicationService gameScheduleApplicationService;

    // =========================================================================
    // Helper Factory Methods
    // =========================================================================

    private BoardConfig createBoardConfig() {
        return new BoardConfig(
                2, 2,
                List.of(
                        List.of(0, 0),
                        List.of(0, 0)
                ),
                List.of()
        );
    }

    private MatchConfig createMatchConfig(String gameId, long startsAt) {
        return new MatchConfig(
                gameId,
                startsAt,
                List.of(10.0, 20.0), // 2 days: day 0 = 10s, day 1 = 20s
                List.of(5, 5),       // daySteps: day 0 = 5 steps, day 1 = 5 steps
                createBoardConfig(),
                List.of(0, 1),
                100,
                2,
                2.0,
                5.0,
                5.0                  // 5 seconds selection limit
        );
    }

    private Team createTeam(String teamId) {
        Agent agent1 = new PatrolAgent(new Coordinate(0, 0), 100);
        Agent agent2 = new PatrolAgent(new Coordinate(1, 0), 100);
        return new Team(teamId, List.of(agent1, agent2));
    }

    // =========================================================================
    // 1. New Match Discovery Tests (discoverNewMatches)
    // =========================================================================

    @Nested
    @DisplayName("Discover New Matches Flow")
    class DiscoverNewMatchesTests {

        @Test
        @DisplayName("shouldSaveNewMatchWhenConfigExistsAndNotCreatedAndStartsAtInFuture")
        void shouldSaveNewMatchWhenConfigExistsAndNotCreatedAndStartsAtInFuture() {
            // Arrange
            long futureTime = Instant.now().getEpochSecond() + 1000;
            MatchConfig config = createMatchConfig("game-1", futureTime);

            when(matchConfigRepository.findAll()).thenReturn(List.of(config));
            when(matchRepository.existsById("game-1")).thenReturn(false);
            when(matchRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            gameScheduleApplicationService.process();

            // Assert & Verify
            ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
            verify(matchRepository, times(1)).save(matchCaptor.capture());

            Match savedMatch = matchCaptor.getValue();
            assertNotNull(savedMatch);
            assertEquals("game-1", savedMatch.getGameId());
            assertEquals(MatchStatus.NOT_STARTED, savedMatch.getState().getStatus());
        }

        @Test
        @DisplayName("shouldNotSaveMatchWhenMatchAlreadyExistsInRepository")
        void shouldNotSaveMatchWhenMatchAlreadyExistsInRepository() {
            // Arrange
            long futureTime = Instant.now().getEpochSecond() + 1000;
            MatchConfig config = createMatchConfig("game-1", futureTime);

            when(matchConfigRepository.findAll()).thenReturn(List.of(config));
            when(matchRepository.existsById("game-1")).thenReturn(true);
            when(matchRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            gameScheduleApplicationService.process();

            // Verify
            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldNotSaveMatchWhenConfigStartsAtInPastOrNow")
        void shouldNotSaveMatchWhenConfigStartsAtInPastOrNow() {
            // Arrange
            long pastTime = Instant.now().getEpochSecond() - 1000;
            MatchConfig config = createMatchConfig("game-past", pastTime);

            when(matchConfigRepository.findAll()).thenReturn(List.of(config));
            when(matchRepository.existsById("game-past")).thenReturn(false);
            when(matchRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            gameScheduleApplicationService.process();

            // Verify
            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldHandleEmptyConfigRepositoryWithoutErrors")
        void shouldHandleEmptyConfigRepositoryWithoutErrors() {
            // Arrange
            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenReturn(Collections.emptyList());

            // Act & Assert
            assertDoesNotThrow(() -> gameScheduleApplicationService.process());
            verify(matchRepository, never()).save(any());
        }

        @Test
        @DisplayName("shouldProcessMultipleConfigsCorrectly")
        void shouldProcessMultipleConfigsCorrectly() {
            // Arrange
            long now = Instant.now().getEpochSecond();
            MatchConfig newValidConfig = createMatchConfig("game-new", now + 1000);
            MatchConfig existingConfig = createMatchConfig("game-existing", now + 1000);
            MatchConfig pastConfig = createMatchConfig("game-past", now - 100);

            when(matchConfigRepository.findAll()).thenReturn(List.of(newValidConfig, existingConfig, pastConfig));
            when(matchRepository.existsById("game-new")).thenReturn(false);
            when(matchRepository.existsById("game-existing")).thenReturn(true);
            when(matchRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            gameScheduleApplicationService.process();

            // Verify
            ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
            verify(matchRepository, times(1)).save(matchCaptor.capture());
            assertEquals("game-new", matchCaptor.getValue().getGameId());
        }
    }

    // =========================================================================
    // 2. Match Phase Update Tests (updateMatchPhases)
    // =========================================================================

    @Nested
    @DisplayName("Update Match Phases Flow")
    class UpdateMatchPhasesTests {

        @Test
        @DisplayName("shouldDoNothingWhenMatchRepositoryIsEmpty")
        void shouldDoNothingWhenMatchRepositoryIsEmpty() {
            // Arrange
            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            gameScheduleApplicationService.process();

            // Verify
            verifyNoInteractions(turnActionService);
            verifyNoInteractions(trafficCalculationService);
        }

        @Test
        @DisplayName("shouldOpenRegistrationWhenStartsAtReached")
        void shouldOpenRegistrationWhenStartsAtReached() {
            // Arrange
            long now = Instant.now().getEpochSecond();
            long startsAt = now - 1; // startsAt reached
            MatchConfig config = createMatchConfig("game-1", startsAt);
            Match match = new Match(config);

            assertEquals(MatchStatus.NOT_STARTED, match.getState().getStatus());

            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenReturn(List.of(match));

            // Act
            gameScheduleApplicationService.process();

            // Verify: match status transitioned to REGISTERING
            assertEquals(MatchStatus.REGISTERING, match.getState().getStatus());
            verifyNoInteractions(turnActionService);
            verifyNoInteractions(trafficCalculationService);
        }

        @Test
        @DisplayName("shouldStartMatchWhenRegistrationEnds")
        void shouldStartMatchWhenRegistrationEnds() {
            // Arrange
            long now = Instant.now().getEpochSecond();
            long startsAt = now - 10; // registration opened 10s ago, selection limit is 5s
            MatchConfig config = createMatchConfig("game-1", startsAt);
            Match match = new Match(config);
            match.openRegistration(startsAt); // state -> REGISTERING

            assertEquals(MatchStatus.REGISTERING, match.getState().getStatus());

            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenReturn(List.of(match));

            // Act
            gameScheduleApplicationService.process();

            // Verify: match status transitioned from REGISTERING to PLAYING
            assertEquals(MatchStatus.PLAYING, match.getState().getStatus());
            verifyNoInteractions(turnActionService);
            verifyNoInteractions(trafficCalculationService);
        }

        @Test
        @DisplayName("shouldExecuteTurnActionsAndAdvanceDayWhenDayIsFinished")
        void shouldExecuteTurnActionsAndAdvanceDayWhenDayIsFinished() {
            // Arrange
            long now = Instant.now().getEpochSecond();
            long startsAt = now - 20; // 20s ago
            MatchConfig config = createMatchConfig("game-1", startsAt);
            Team team1 = createTeam("team-1");
            Match match = new Match(config);
            match.openRegistration(startsAt);
            match.registerTeam(team1);
            match.start(startsAt + 5); // PLAYING, day 0 ends at startsAt + 5 + 10 = startsAt + 15 (5s ago)

            assertTrue(match.isFinishDay(now));

            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenReturn(List.of(match));

            CollectResult collectResult = CollectResult.failed("team-1", new Coordinate(0, 0));
            when(turnActionService.execute(
                    eq(match.getBoard()),
                    eq(team1),
                    any(ActionSubmission.class),
                    any(TrafficTracker.class),
                    eq(5)
            )).thenReturn(List.of(collectResult));

            TrafficTracker updatedTracker = mock(TrafficTracker.class);
            when(updatedTracker.getDay()).thenReturn(1);
            when(trafficCalculationService.calculate(
                    any(TrafficTracker.class),
                    eq(2.0),
                    eq(5.0),
                    eq(2)
            )).thenReturn(updatedTracker);

            // Act
            gameScheduleApplicationService.process();

            // Verify
            verify(turnActionService, times(1)).execute(
                    eq(match.getBoard()),
                    eq(team1),
                    any(ActionSubmission.class),
                    any(TrafficTracker.class),
                    eq(5)
            );
            verify(trafficCalculationService, times(1)).calculate(any(), eq(2.0), eq(5.0), eq(2));
            verify(matchRepository, times(1)).save(match);

            // Match day advanced to 1
            assertEquals(1, match.getState().getCurrentDay());
            assertEquals(MatchStatus.PLAYING, match.getState().getStatus());
        }

        @Test
        @DisplayName("shouldProcessMultipleMatchesEvenIfFirstMatchDayIsNotFinished")
        void shouldProcessMultipleMatchesEvenIfFirstMatchDayIsNotFinished() {
            // Arrange
            long now = Instant.now().getEpochSecond();
            long startsAt = now - 20;

            // Match 1: Day NOT finished
            MatchConfig config1 = createMatchConfig("game-1", startsAt);
            Match match1 = new Match(config1);
            match1.openRegistration(startsAt);
            match1.start(now); // started now, day ends in now + 10s (not finished yet)

            // Match 2: Day IS finished
            MatchConfig config2 = createMatchConfig("game-2", startsAt);
            Team team2 = createTeam("team-2");
            Match match2 = new Match(config2);
            match2.openRegistration(startsAt);
            match2.registerTeam(team2);
            match2.start(startsAt + 5); // day ended 5s ago

            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenReturn(List.of(match1, match2));

            when(turnActionService.execute(any(), any(), any(), any(), anyInt()))
                    .thenReturn(Collections.emptyList());

            TrafficTracker updatedTracker = mock(TrafficTracker.class);
            when(updatedTracker.getDay()).thenReturn(1);
            when(trafficCalculationService.calculate(any(), anyDouble(), anyDouble(), anyInt()))
                    .thenReturn(updatedTracker);

            // Act
            gameScheduleApplicationService.process();

            // Verify: match2 was processed and saved despite match1 day not being finished (due to continue;)
            verify(turnActionService, times(1)).execute(eq(match2.getBoard()), eq(team2), any(), any(), eq(5));
            verify(matchRepository, times(1)).save(match2);
        }

        @Test
        @DisplayName("shouldFinishMatchOnLastDay")
        void shouldFinishMatchOnLastDay() {
            // Arrange
            long now = Instant.now().getEpochSecond();
            long startsAt = now - 50;
            MatchConfig config = createMatchConfig("game-1", startsAt);
            Team team = createTeam("team-1");
            Match match = new Match(config);
            match.openRegistration(startsAt);
            match.registerTeam(team);
            match.start(startsAt + 5); // day 0 starts at startsAt + 5, ends at startsAt + 15
            match.finishDay(startsAt + 20); // advances to day 1 (last day)

            assertEquals(1, match.getState().getCurrentDay());
            assertTrue(match.isFinishDay(now));

            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenReturn(List.of(match));

            when(turnActionService.execute(any(), any(), any(), any(), anyInt())).thenReturn(Collections.emptyList());
            TrafficTracker updatedTracker = mock(TrafficTracker.class);
            when(updatedTracker.getDay()).thenReturn(2);
            when(trafficCalculationService.calculate(any(), anyDouble(), anyDouble(), anyInt())).thenReturn(updatedTracker);

            // Act
            gameScheduleApplicationService.process();

            // Verify: Match transitioned to FINISHED
            assertEquals(MatchStatus.FINISHED, match.getState().getStatus());
            verify(matchRepository, times(1)).save(match);
        }
    }

    // =========================================================================
    // 3. Exception Cases Tests
    // =========================================================================

    @Nested
    @DisplayName("Exception Handling")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("shouldPropagateExceptionWhenMatchConfigRepositoryThrowsException")
        void shouldPropagateExceptionWhenMatchConfigRepositoryThrowsException() {
            // Arrange
            when(matchConfigRepository.findAll()).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> gameScheduleApplicationService.process()
            );

            assertEquals("Database error", ex.getMessage());
            verifyNoInteractions(matchRepository);
        }

        @Test
        @DisplayName("shouldPropagateExceptionWhenMatchRepositoryFindAllThrowsException")
        void shouldPropagateExceptionWhenMatchRepositoryFindAllThrowsException() {
            // Arrange
            when(matchConfigRepository.findAll()).thenReturn(Collections.emptyList());
            when(matchRepository.findAll()).thenThrow(new RuntimeException("Match repository read error"));

            // Act & Assert
            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> gameScheduleApplicationService.process()
            );

            assertEquals("Match repository read error", ex.getMessage());
            verifyNoInteractions(turnActionService);
        }
    }
}
