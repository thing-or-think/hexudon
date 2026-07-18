package com.naprock.hexudon.sdk.internal.mapper;

import com.naprock.hexudon.sdk.internal.dto.response.AgentResponse;
import com.naprock.hexudon.sdk.internal.dto.response.MatchStateResponse;
import com.naprock.hexudon.sdk.internal.dto.response.TeamResponse;
import com.naprock.hexudon.sdk.model.Coordinate;
import com.naprock.hexudon.sdk.model.AgentType;
import com.naprock.hexudon.sdk.model.MatchState;
import com.naprock.hexudon.sdk.model.MatchStatus;
import com.naprock.hexudon.sdk.model.TrafficLevel;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchStateMapperTest {

    @Test
    void shouldNotInstantiateUtilityClass() throws Exception {
        Constructor<MatchStateMapper> constructor = MatchStateMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldMapToDomainWithAllDetails() {
        // Arrange
        // Agent 1: String type ("refuel"), pos (3)
        AgentResponse agent1 = new AgentResponse("agent-1", 3, null, 80, "refuel", null);
        // Agent 2: Integer type/kind (0 = PATROL), cell (5)
        AgentResponse agent2 = new AgentResponse("agent-2", null, 5, 100, null, 0);
        // Agent 3: Fallback type, default pos (0)
        AgentResponse agent3 = new AgentResponse("agent-3", null, null, 120, null, null);

        TeamResponse teamDto = new TeamResponse(
                List.of(agent1, agent2, agent3),
                List.of("BrandA", "BrandB")
        );

        MatchStateResponse response = new MatchStateResponse(
                1600000000L,
                2,
                15,
                Map.of("10", 1, "20", 2),
                Map.of("team1", teamDto),
                "in_progress"
        );

        // Act
        MatchState domain = MatchStateMapper.toDomain(response, 10);

        // Assert
        assertThat(domain.endsAt()).isEqualTo(1600000000L);
        assertThat(domain.day()).isEqualTo(2);
        assertThat(domain.stepsToday()).isEqualTo(15);
        assertThat(domain.status()).isEqualTo(MatchStatus.PLAYING);

        // Verify road condition mapping
        assertThat(domain.roadCondition()).hasSize(2);
        assertThat(domain.roadCondition()).containsEntry(new Coordinate(10, 10), TrafficLevel.CONGESTED);
        assertThat(domain.roadCondition()).containsEntry(new Coordinate(20, 10), TrafficLevel.JAM);

        // Verify team mapping
        assertThat(domain.teams()).hasSize(1);
        assertThat(domain.teams()).containsKey("team1");
        var team = domain.teams().get("team1");
        assertThat(team.teamId()).isEqualTo("team1");
        assertThat(team.distinctBrands()).containsExactly("BrandA", "BrandB");

        // Verify agents
        assertThat(team.agents()).hasSize(3);
        
        // agent1
        assertThat(team.agents().get(0).agentId()).isEqualTo("agent-1");
        assertThat(team.agents().get(0).type()).isEqualTo(AgentType.REFUEL);
        assertThat(team.agents().get(0).coordinate().pos()).isEqualTo(3);
        assertThat(team.agents().get(0).fuel()).isEqualTo(80);

        // agent2
        assertThat(team.agents().get(1).agentId()).isEqualTo("agent-2");
        assertThat(team.agents().get(1).type()).isEqualTo(AgentType.PATROL);
        assertThat(team.agents().get(1).coordinate().pos()).isEqualTo(5);

        // agent3
        assertThat(team.agents().get(2).agentId()).isEqualTo("agent-3");
        assertThat(team.agents().get(2).type()).isEqualTo(AgentType.PATROL); // fallback
        assertThat(team.agents().get(2).coordinate().pos()).isEqualTo(0);    // fallback
    }

    @Test
    void shouldMapToDomainWithNullCollectionsAndEmptyDefaults() {
        // Arrange
        MatchStateResponse response = new MatchStateResponse(
                1000L, 0, 10,
                Collections.emptyMap(), Collections.emptyMap(), "waiting"
        );

        // Act
        MatchState domain = MatchStateMapper.toDomain(response, 4);

        // Assert
        assertThat(domain.roadCondition()).isEmpty();
        assertThat(domain.teams()).isEmpty();
        assertThat(domain.status()).isEqualTo(MatchStatus.WAITING);
    }

    @Test
    void shouldMapToDomainWhenRoadConditionAndTeamsAreNull() {
        // Arrange
        MatchStateResponse mockResponse = mock(MatchStateResponse.class);
        when(mockResponse.roadCondition()).thenReturn(null);
        when(mockResponse.teams()).thenReturn(null);
        when(mockResponse.status()).thenReturn("waiting");
        when(mockResponse.endsAt()).thenReturn(1000L);
        when(mockResponse.day()).thenReturn(0);
        when(mockResponse.stepsToday()).thenReturn(10);

        // Act
        MatchState domain = MatchStateMapper.toDomain(mockResponse, 4);

        // Assert
        assertThat(domain.roadCondition()).isEmpty();
        assertThat(domain.teams()).isEmpty();
    }

    @Test
    void shouldMapTeamWhenDistinctTypesAndAgentsAreNull() {
        // Arrange
        // We want to test toTeam mapper when agents list or distinctTypes is null in TeamResponse.
        // Again, TeamResponse constructor requires non-null lists, so we mock it.
        TeamResponse mockTeamResponse = mock(TeamResponse.class);
        when(mockTeamResponse.agents()).thenReturn(null);
        when(mockTeamResponse.distinctTypes()).thenReturn(null);

        // Act
        var team = MatchStateMapper.toTeam("team1", mockTeamResponse, 4);

        // Assert
        assertThat(team.agents()).isEmpty();
        assertThat(team.distinctBrands()).isEmpty();
    }

    @Test
    void shouldThrowWhenResponseIsNull() {
        assertThatThrownBy(() -> MatchStateMapper.toDomain(null, 5))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Match state response must not be null");
    }

    @Test
    void shouldThrowWhenWidthInvalid() {
        MatchStateResponse response = new MatchStateResponse(
                1000L, 0, 10, Collections.emptyMap(), Collections.emptyMap(), "waiting"
        );

        assertThatThrownBy(() -> MatchStateMapper.toDomain(response, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Map width must be positive");

        assertThatThrownBy(() -> MatchStateMapper.toDomain(response, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Map width must be positive");
    }
}
