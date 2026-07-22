package com.naprock.hexudon.domain.service;

import com.naprock.hexudon.domain.exception.business.GameRuleViolationException;
import com.naprock.hexudon.domain.exception.code.ErrorCode;
import com.naprock.hexudon.domain.model.agent.Agent;
import com.naprock.hexudon.domain.model.agent.PatrolAgent;
import com.naprock.hexudon.domain.model.agent.RefuelAgent;
import com.naprock.hexudon.domain.model.board.Cell;
import com.naprock.hexudon.domain.model.board.GameBoard;
import com.naprock.hexudon.domain.model.board.TerrainType;
import com.naprock.hexudon.domain.model.dto.SubmitActionsDto;
import com.naprock.hexudon.domain.model.geometry.Coordinate;
import com.naprock.hexudon.domain.model.movement.Action;
import com.naprock.hexudon.domain.model.movement.ActionPlan;
import com.naprock.hexudon.domain.model.movement.ActionType;
import com.naprock.hexudon.domain.model.movement.MovementCost;
import com.naprock.hexudon.domain.model.submission.ActionSubmission;
import com.naprock.hexudon.domain.model.team.CollectResult;
import com.naprock.hexudon.domain.model.team.Team;
import com.naprock.hexudon.domain.model.traffic.TrafficTracker;

import java.util.ArrayList;
import java.util.List;

public class TurnActionService {

    public ActionSubmission simulate(
            GameBoard board,
            TrafficTracker traffic,
            Team team,
            SubmitActionsDto submission,
            int availableSteps
    ) {
        List<Agent> simulatedAgents = copyAgentsForSimulation(
                team,
                availableSteps
        );

        List<ActionPlan> simulatedActionPlans = new ArrayList<>();

        for (int i = 0; i < simulatedAgents.size(); i++) {
            Agent agent = simulatedAgents.get(i);

            ActionPlan simulatedActionPlan = simulateAgent(
                    board,
                    traffic,
                    agent,
                    submission.actions().get(i)
            );

            simulatedActionPlans.add(simulatedActionPlan);
        }

        return new ActionSubmission(
                submission.day(),
                team.getTeamId(),
                simulatedActionPlans,
                submission.submittedAt()
        );
    }

    public List<CollectResult> execute(
            GameBoard board,
            Team team,
            ActionSubmission submission,
            TrafficTracker traffic,
            int availableSteps
    ) {
        List<Agent> agents = team.getAgents();
        List<List<Action>> actionPlans = copyActions(submission, agents);

        List<CollectResult> collectResults = new ArrayList<>();

        for (int step = availableSteps; step >= 0; step--) {
            executeStep(
                    board,
                    team,
                    agents,
                    actionPlans,
                    traffic,
                    step,
                    collectResults
            );
        }

        return collectResults;
    }

    private List<Agent> copyAgentsForSimulation(
            Team team,
            int availableSteps
    ) {
        return team.getAgents()
                .stream()
                .map(agent -> agent.copy(availableSteps))
                .toList();
    }

    private ActionPlan simulateAgent(
            GameBoard board,
            TrafficTracker traffic,
            Agent agent,
            List<Action> submittedActions
    ) {
        List<Action> executedActions = new ArrayList<>();

        while (agent.getRemainingSteps() != 0) {
            Action action = pollNextAction(submittedActions);

            executeAction(
                    board,
                    traffic,
                    agent,
                    action
            );

            executedActions.add(action);
        }

        validateNoRemainingActions(submittedActions);

        return new ActionPlan(executedActions);
    }

    private void executeStep(
            GameBoard board,
            Team team,
            List<Agent> agents,
            List<List<Action>> actionPlans,
            TrafficTracker traffic,
            int step,
            List<CollectResult> collectResults
    ) {
        for (int agentIndex = 0; agentIndex < agents.size(); agentIndex++) {
            Agent agent = agents.get(agentIndex);

            if (!agent.hasRemainingSteps(step)) {
                continue;
            }

            Action action = pollNextAction(
                    actionPlans.get(agentIndex)
            );

            executeAction(
                    board,
                    traffic,
                    agent,
                    action
            );

            Coordinate position = agent.getPosition();

            traffic.recordMovements(position);

            processAgentAction(
                    agent,
                    team,
                    board,
                    collectResults
            );
        }
    }

    private void executeAction(
            GameBoard board,
            TrafficTracker traffic,
            Agent agent,
            Action action
    ) {
        if (action.actionType() == ActionType.MOVE) {
            moveAgent(
                    board,
                    traffic,
                    agent,
                    action
            );
            return;
        }

        agent.waitAction();
    }

    private void moveAgent(
            GameBoard board,
            TrafficTracker traffic,
            Agent agent,
            Action action
    ) {
        Coordinate nextPosition = getNextPosition(
                agent,
                action
        );

        if (!board.getCell(nextPosition).isWalkable()) {
            throw new GameRuleViolationException(
                    ErrorCode.INVALID_TARGET_TERRAIN,
                    "Agent cannot move to an unwalkable terrain."
            );
        }


        MovementCost movementCost = calculateMovementCost(
                board,
                traffic,
                nextPosition
        );

        agent.moveTo(
                nextPosition,
                movementCost
        );
    }

    private Coordinate getNextPosition(
            Agent agent,
            Action action
    ) {
        if (action.actionType() != ActionType.MOVE) {
            return agent.getPosition();
        }

        return agent
                .getPosition()
                .getNeighbor(action.direction());
    }

    private MovementCost calculateMovementCost(
            GameBoard board,
            TrafficTracker traffic,
            Coordinate position
    ) {
        Cell destinationCell = board.getCell(position);
        TerrainType terrainType = destinationCell.terrainType();

        int fuelCost = terrainType.getFuelCost();
        int stepCost = calculateStepCost(
                destinationCell,
                traffic
        );

        return new MovementCost(
                fuelCost,
                stepCost
        );
    }

    private int calculateStepCost(
            Cell destinationCell,
            TrafficTracker traffic
    ) {
        if (destinationCell.terrainType() != TerrainType.ROAD) {
            return destinationCell
                    .terrainType()
                    .getStepCost();
        }

        return traffic
                .stateAt(destinationCell.coordinate())
                .getTrafficLevel()
                .cost();
    }

    private void processAgentAction(
            Agent agent,
            Team team,
            GameBoard board,
            List<CollectResult> collectResults
    ) {
        if (agent instanceof PatrolAgent patrolAgent) {
            collectResults.add(
                    patrolAgent.collectUdon(
                            team.getTeamId(),
                            board.getSpotIndex()
                    )
            );
            return;
        }

        if (agent instanceof RefuelAgent refuelAgent) {
            refuelAgent.refuel(
                    team.getAgents()
            );
        }
    }

    private List<List<Action>> copyActions(
            ActionSubmission submission,
            List<Agent> agents
    ) {
        List<List<Action>> actionPlans = new ArrayList<>();

        for (int i = 0; i < agents.size(); i++) {
            if (i < submission.getPlans().size()) {
                actionPlans.add(
                        submission.getPlans()
                                .get(i)
                                .copy()
                );
            } else {
                actionPlans.add(
                        new ArrayList<>()
                );
            }
        }

        return actionPlans;
    }

    private Action pollNextAction(
            List<Action> actions
    ) {
        return actions.isEmpty()
                ? Action.stay()
                : actions.removeFirst();
    }

    private void validateNoRemainingActions(
            List<Action> submittedActions
    ) {
        if (!submittedActions.isEmpty()) {
            throw new GameRuleViolationException(
                    ErrorCode.STEPS_LIMIT_EXCEEDED,
                    "Agent submitted more actions than the available steps."
            );
        }
    }
}