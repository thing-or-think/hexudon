package com.naprock.hexudon.application.mapper;

import com.naprock.hexudon.application.dto.*;
import com.naprock.hexudon.domain.model.Team;
import com.naprock.hexudon.domain.valueobject.Action;
import com.naprock.hexudon.domain.valueobject.AgentExecutionResult;
import com.naprock.hexudon.domain.valueobject.MatchState;
import com.naprock.hexudon.domain.valueobject.TurnSimulationResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ActionMapper {

    public ActionMapper() {
    }

    public MatchStateResponse toMatchStateResponse(MatchState matchState) {
        return new MatchStateResponse(matchState);
    }

    public TeamResponse toTeamResponse(Team team) {
        return new TeamResponse(team);
    }

    public Action toAction(ActionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        return new Action(
                request.order(),
                request.actionType(),
                request.targetX(),
                request.targetY(),
                System.currentTimeMillis()
        );
    }

    public Map<String, List<Action>> toDomainActionPlanMap(
            DayActionRequest dayRequest
    ) {
        Objects.requireNonNull(dayRequest, "dayRequest must not be null");

        return dayRequest.agentPlans()
                .stream()
                .collect(Collectors.toMap(
                        AgentActionPlanRequest::agentId,
                        plan -> plan.actions()
                                .stream()
                                .map(action -> toAction(action))
                                .toList()
                ));
    }

    public ActionResponse toActionResponse(
            Action action
    ) {
        Objects.requireNonNull(action, "action must not be null");

        return new ActionResponse(
                action.getOrder(),
                action.getActionType(),
                action.getTargetX(),
                action.getTargetY(),
                action.getTimestamp()
        );
    }

    public AgentActionPlanResponse toAgentActionPlanResponse(
            AgentExecutionResult executionResult
    ) {
        Objects.requireNonNull(executionResult, "executionResult must not be null");

        List<ActionResponse> actions = executionResult.actions()
                .stream()
                .map(this::toActionResponse)
                .toList();

        return new AgentActionPlanResponse(
                executionResult.agentId(),
                actions
        );
    }

    public DayActionResponse toDayActionResponse(
            TurnSimulationResult simulationResult
    ) {
        Objects.requireNonNull(simulationResult, "simulationResult must not be null");

        List<AgentActionPlanResponse> plans = simulationResult.agentExecutionResults()
                .stream()
                .map(this::toAgentActionPlanResponse)
                .toList();

        return new DayActionResponse(
                simulationResult.day(),
                plans
        );
    }

}
