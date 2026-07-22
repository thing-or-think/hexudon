package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.traffic.TrafficLevel;
import com.naprock.hexudon.domain.model.traffic.TrafficState;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TrafficCalculationService Unit Tests")
class TrafficCalculationServiceTest {

    private TrafficCalculationService service;

    @BeforeEach
    void setUp() {
        service = new TrafficCalculationService();
    }

    @Nested
    @DisplayName("Traffic Level Determination Tests")
    class TrafficLevelDeterminationTests {

        @Test
        @DisplayName("Should return NORMAL when average stay steps per team is strictly less than busyThreshold")
        void calculate_ShouldReturnNormal_WhenAverageLessThanBusyThreshold() {
            Coordinate coord = new Coordinate(1, 1);
            TrafficState state = new TrafficState(coord, 1, 1, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(TrafficLevel.NORMAL, result.stateAt(coord).getTrafficLevel());
        }

        @Test
        @DisplayName("Should return BUSY when average stay steps per team is between busyThreshold and jammedThreshold")
        void calculate_ShouldReturnBusy_WhenAverageBetweenBusyAndJammedThreshold() {
            Coordinate coord = new Coordinate(1, 1);
            TrafficState state = new TrafficState(coord, 3, 3, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(TrafficLevel.BUSY, result.stateAt(coord).getTrafficLevel());
        }

        @Test
        @DisplayName("Should return CONGESTED when average stay steps per team is greater than jammedThreshold")
        void calculate_ShouldReturnCongested_WhenAverageGreaterThanJammedThreshold() {
            Coordinate coord = new Coordinate(1, 1);
            TrafficState state = new TrafficState(coord, 5, 5, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(TrafficLevel.CONGESTED, result.stateAt(coord).getTrafficLevel());
        }
    }

    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @Test
        @DisplayName("Should return BUSY when average stay steps per team exactly equals busyThreshold")
        void calculate_ShouldReturnBusy_WhenAverageEqualsBusyThreshold() {
            Coordinate coord = new Coordinate(1, 1);
            TrafficState state = new TrafficState(coord, 2, 2, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(TrafficLevel.BUSY, result.stateAt(coord).getTrafficLevel());
        }

        @Test
        @DisplayName("Should return CONGESTED when average stay steps per team exactly equals jammedThreshold")
        void calculate_ShouldReturnCongested_WhenAverageEqualsJammedThreshold() {
            Coordinate coord = new Coordinate(1, 1);
            TrafficState state = new TrafficState(coord, 4, 4, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(TrafficLevel.CONGESTED, result.stateAt(coord).getTrafficLevel());
        }
    }

    @Nested
    @DisplayName("Calculation Logic Tests")
    class CalculationLogicTests {

        @Test
        @DisplayName("Should sum previousDayStaySteps and currentDayStaySteps correctly")
        void calculate_ShouldSumPreviousAndCurrentStaySteps() {
            Coordinate c1 = new Coordinate(0, 0);
            Coordinate c2 = new Coordinate(1, 1);
            Map<Coordinate, TrafficState> states = Map.of(
                    c1, new TrafficState(c1, 3, 5, TrafficLevel.NORMAL),
                    c2, new TrafficState(c2, 0, 8, TrafficLevel.NORMAL)
            );
            TrafficTracker tracker = new TrafficTracker(1, states);

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 4);

            assertEquals(TrafficLevel.BUSY, result.stateAt(c1).getTrafficLevel());
            assertEquals(TrafficLevel.BUSY, result.stateAt(c2).getTrafficLevel());
        }

        @Test
        @DisplayName("Should divide total stay steps by teamCount")
        void calculate_ShouldDivideTotalStepsByTeamCount() {
            Coordinate coord = new Coordinate(0, 0);
            TrafficState state = new TrafficState(coord, 2, 4, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result3Teams = service.calculate(tracker, 2.0, 4.0, 3);
            TrafficTracker result1Team = service.calculate(tracker, 2.0, 4.0, 1);

            assertEquals(TrafficLevel.BUSY, result3Teams.stateAt(coord).getTrafficLevel());
            assertEquals(TrafficLevel.CONGESTED, result1Team.stateAt(coord).getTrafficLevel());
        }

        @Test
        @DisplayName("Should perform floating point division for average calculation")
        void calculate_ShouldUseFloatingPointDivision() {
            Coordinate coord = new Coordinate(0, 0);
            TrafficState state = new TrafficState(coord, 2, 3, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.5, 5.0, 2);

            assertEquals(TrafficLevel.BUSY, result.stateAt(coord).getTrafficLevel());
        }

        @Test
        @DisplayName("Should calculate multiple coordinates independently")
        void calculate_ShouldCalculateMultipleCoordinatesIndependently() {
            Coordinate c1 = new Coordinate(0, 0);
            Coordinate c2 = new Coordinate(1, 0);
            Coordinate c3 = new Coordinate(2, 0);

            Map<Coordinate, TrafficState> states = Map.of(
                    c1, new TrafficState(c1, 0, 0, TrafficLevel.NORMAL),
                    c2, new TrafficState(c2, 2, 2, TrafficLevel.NORMAL),
                    c3, new TrafficState(c3, 4, 4, TrafficLevel.NORMAL)
            );
            TrafficTracker tracker = new TrafficTracker(1, states);

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(TrafficLevel.NORMAL, result.stateAt(c1).getTrafficLevel());
            assertEquals(TrafficLevel.BUSY, result.stateAt(c2).getTrafficLevel());
            assertEquals(TrafficLevel.CONGESTED, result.stateAt(c3).getTrafficLevel());
        }
    }

    @Nested
    @DisplayName("Result TrafficTracker Structure Tests")
    class ResultTrackerStructureTests {

        @Test
        @DisplayName("Should increment tracker day by 1")
        void calculate_ShouldIncrementDayByOne() {
            Coordinate coord = new Coordinate(0, 0);
            TrafficState state = new TrafficState(coord, 1, 1, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(5, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(6, result.getDay());
        }

        @Test
        @DisplayName("Should preserve all original coordinates in result tracker")
        void calculate_ShouldPreserveAllCoordinates() {
            Coordinate c1 = new Coordinate(0, 0);
            Coordinate c2 = new Coordinate(1, 1);
            Map<Coordinate, TrafficState> states = Map.of(
                    c1, new TrafficState(c1),
                    c2, new TrafficState(c2)
            );
            TrafficTracker tracker = new TrafficTracker(1, states);

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(2, result.trafficStatesIndex().size());
            assertTrue(result.trafficStatesIndex().containsKey(c1));
            assertTrue(result.trafficStatesIndex().containsKey(c2));
        }

        @Test
        @DisplayName("Should preserve currentStaySteps and reset previousStaySteps to 0 in new TrafficState")
        void calculate_ShouldPreserveCurrentStayStepsAndResetPreviousToZero() {
            Coordinate coord = new Coordinate(0, 0);
            TrafficState state = new TrafficState(coord, 3, 7, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            TrafficState newState = result.stateAt(coord);
            assertNotNull(newState);
            assertEquals(0, newState.getPreviousStaySteps(), "previousStaySteps of new TrafficState should be reset to 0");
            assertEquals(7, newState.getCurrentStaySteps(), "currentStaySteps should be preserved in new TrafficState");
        }

        @Test
        @DisplayName("Should attach calculated TrafficLevel to the new TrafficState")
        void calculate_ShouldSetCalculatedTrafficLevelInNewState() {
            Coordinate coord = new Coordinate(0, 0);
            TrafficState state = new TrafficState(coord, 4, 4, TrafficLevel.NORMAL);
            TrafficTracker tracker = new TrafficTracker(1, Map.of(coord, state));

            TrafficTracker result = service.calculate(tracker, 2.0, 4.0, 2);

            assertEquals(TrafficLevel.CONGESTED, result.stateAt(coord).getTrafficLevel());
        }
    }

    @Nested
    @DisplayName("Empty Tracker Tests")
    class EmptyTrackerTests {

        @Test
        @DisplayName("Should handle empty tracker successfully and increment day")
        void calculate_ShouldHandleEmptyTracker() {
            TrafficTracker emptyTracker = new TrafficTracker(0, Map.of());

            TrafficTracker result = service.calculate(emptyTracker, 2.0, 4.0, 2);

            assertNotNull(result);
            assertEquals(1, result.getDay());
            assertTrue(result.trafficStatesIndex().isEmpty());
        }
    }
}
