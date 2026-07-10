package com.naprock.hexudon.controller;

import com.naprock.hexudon.dto.*;
import com.naprock.hexudon.model.Action;
import com.naprock.hexudon.model.AgentExecutionResult;
import com.naprock.hexudon.model.TurnSimulationResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ActionMapper {

    public ActionMapper() {
    }

    public static Action toAction(ActionRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        return new Action(
                request.order(),
                request.actionType(),
                request.targetX(),
                request.targetY(),
                System.currentTimeMillis()
        );
    }

    public static Map<String, List<Action>> toAgentPlans(
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

    public static ActionResponse toActionResponse(
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

    public static AgentActionPlanResponse toAgentActionPlanResponse(
            AgentExecutionResult executionResult
    ) {
        Objects.requireNonNull(executionResult, "executionResult must not be null");

        List<ActionResponse> actions = executionResult.actions()
                .stream()
                .map(ActionMapper::toActionResponse)
                .toList();

        return new AgentActionPlanResponse(
                executionResult.agentId(),
                actions
        );
    }

    public static DayActionResponse toDayActionResponse(
            TurnSimulationResult simulationResult
    ) {
        Objects.requireNonNull(simulationResult, "simulationResult must not be null");

        List<AgentActionPlanResponse> plans = simulationResult.agentExecutionResults()
                .stream()
                .map(ActionMapper::toAgentActionPlanResponse)
                .toList();

        return new DayActionResponse(
                simulationResult.day(),
                plans
        );
    }

}
