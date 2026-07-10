import { isAdjacent } from "../utils/hex_utils.js";

/**
 * Validates a team's planned actions by simulating the turn locally.
 *
 * @param {Object} teamState - Current team state from server { teamName, agents }
 * @param {Object} mapState - Map cells as key-value map: "x_y" -> Cell object { x, y, terrainType }
 * @param {Object} matchConfig - Game configurations { maxFuel, maxStepsPerTurn, plainFuelCost, plainStepCost, etc. }
 * @param {Object} agentActions - Map of agentId -> Array of Action objects
 * @returns {Object} { isValid: boolean, errors: Object, warnings: Object }
 *          errors is a map: agentId -> array of error objects { order, message }
 *          warnings is a map: agentId -> array of warning messages
 */
export function validateActions(teamState, mapState, matchConfig, agentActions) {
  const result = {
    isValid: true,
    errors: {}, // agentId -> [{ order: number, message: string }]
    warnings: {} // agentId -> [string]
  };

  const maxSteps = matchConfig.maxStepsPerTurn || 5;
  const maxFuel = matchConfig.maxFuel || 100;

  // Clone agents' state for local simulation
  const simulatedAgents = teamState.agents.map(a => ({
    id: a.id,
    type: a.type,
    posX: a.posX,
    posY: a.posY,
    fuel: a.fuel,
    remainingSteps: maxSteps, // Reset to max steps for the day, since each turn starts fresh
    actionsQueue: [...(agentActions[a.id] || [])].sort((a, b) => a.order - b.order),
    executedCount: 0
  }));

  const agentsMap = new Map(simulatedAgents.map(a => [a.id, a]));

  // 1. Basic format validations
  for (const agent of simulatedAgents) {
    result.errors[agent.id] = [];
    result.warnings[agent.id] = [];

    // Check action orders are consecutive starting from 1
    const queue = agent.actionsQueue;
    for (let i = 0; i < queue.length; i++) {
      if (queue[i].order !== i + 1) {
        result.errors[agent.id].push({
          order: queue[i].order,
          message: `Order must be consecutive starting from 1. Found order ${queue[i].order} at position ${i + 1}.`
        });
      }
    }
  }

  // Helper to get terrain costs
  const getTerrainCosts = (cell) => {
    if (!cell) return { step: 999, fuel: 999 };
    switch (cell.terrainType) {
      case "ROAD":
        return { step: matchConfig.roadStepCost || 1, fuel: matchConfig.roadFuelCost || 2 };
      case "PLAIN":
        return { step: matchConfig.plainStepCost || 2, fuel: matchConfig.plainFuelCost || 1 };
      case "MOUNTAIN":
        return { step: matchConfig.mountainStepCost || 3, fuel: matchConfig.mountainFuelCost || 2 };
      default:
        return { step: 999, fuel: 999 };
    }
  };

  // 2. Step-by-step simulation
  for (let step = maxSteps; step > 0; step--) {
    // A. Auto Refuel simulation:
    // When Refuel agent and Patrol agent stand in the same cell at the start of a step, refuel the Patrol agent
    const refuelAgents = simulatedAgents.filter(a => a.type === "REFUEL" && a.remainingSteps === step);
    const patrolAgents = simulatedAgents.filter(a => a.type === "PATROL" && a.remainingSteps === step);

    for (const refuel of refuelAgents) {
      for (const patrol of patrolAgents) {
        if (refuel.posX === patrol.posX && refuel.posY === patrol.posY) {
          patrol.fuel = maxFuel;
        }
      }
    }

    // B. Execute step for each agent
    for (const agent of simulatedAgents) {
      if (agent.remainingSteps !== step) {
        continue; // This agent doesn't act at this step
      }

      // If queue is empty, a WAIT action is automatically created (consumes 1 step)
      if (agent.actionsQueue.length === 0) {
        agent.remainingSteps -= 1;
        continue;
      }

      // Get next action
      const action = agent.actionsQueue.shift();
      agent.executedCount++;

      if (action.actionType === "WAIT") {
        agent.remainingSteps -= 1;
        continue;
      }

      if (action.actionType === "MOVE") {
        const tx = action.targetX;
        const ty = action.targetY;

        // Check if coordinates exist
        if (tx === null || tx === undefined || ty === null || ty === undefined) {
          result.errors[agent.id].push({
            order: action.order,
            message: "Move action must specify target coordinates (targetX, targetY)."
          });
          continue;
        }

        // Check if target is on map
        const targetCell = mapState[`${tx}_${ty}`];
        if (!targetCell) {
          result.errors[agent.id].push({
            order: action.order,
            message: `Target cell (${tx}, ${ty}) is out of map bounds.`
          });
          continue;
        }

        // Check if target is Pond
        if (targetCell.terrainType === "POND") {
          result.errors[agent.id].push({
            order: action.order,
            message: `Cannot move to (${tx}, ${ty}): terrain is a POND.`
          });
          continue;
        }

        // Check adjacency
        if (!isAdjacent(agent.posX, agent.posY, tx, ty)) {
          result.errors[agent.id].push({
            order: action.order,
            message: `Target cell (${tx}, ${ty}) is not adjacent to current cell (${agent.posX}, ${agent.posY}).`
          });
          continue;
        }

        const costs = getTerrainCosts(targetCell);

        // Check step cost
        if (agent.remainingSteps < costs.step) {
          result.errors[agent.id].push({
            order: action.order,
            message: `Not enough steps to move to ${targetCell.terrainType} cell. Cost: ${costs.step}, remaining: ${agent.remainingSteps}.`
          });
          continue;
        }

        // Check fuel cost (only for Patrol agents, Refuel has unlimited fuel in client pathing view)
        // Wait, does the server subtract fuel for REFUEL agents? Let's check FuelManager.java lines 55-60:
        // "if (agent.getType() == AgentType.REFUEL || action == null || action.getActionType() != ActionType.MOVE) { continue; }"
        // Yes, the server skips fuel subtraction for REFUEL agents! Refuel agents consume 0 fuel.
        if (agent.type === "PATROL" && agent.fuel < costs.fuel) {
          result.errors[agent.id].push({
            order: action.order,
            message: `Not enough fuel to move to ${targetCell.terrainType} cell. Cost: ${costs.fuel}, remaining: ${agent.fuel}.`
          });
          continue;
        }

        // Apply action effects
        agent.posX = tx;
        agent.posY = ty;
        agent.remainingSteps -= costs.step;
        if (agent.type === "PATROL") {
          agent.fuel -= costs.fuel;
        }
      }
    }
  }

  // 3. Post-simulation checks
  for (const agent of simulatedAgents) {
    // If there are still actions in the queue, warn that they will be ignored
    if (agent.actionsQueue.length > 0) {
      result.warnings[agent.id].push(
        `${agent.actionsQueue.length} action(s) remaining in the queue will not be executed because the agent has run out of steps.`
      );
    }

    if (result.errors[agent.id].length > 0) {
      result.isValid = false;
    }
  }

  return result;
}
