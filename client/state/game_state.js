import { apiClient } from "../network/api_client.js";
import { logger } from "../logger/logger.js";
import { validateActions } from "../validation/action_validator.js";

class GameState {
  constructor() {
    this.serverState = {
      status: "WAITING",
      currentTurn: 0,
      teams: [],
      cells: [],
      currentTurnActions: {},
      spots: []
    };
    this.cellMap = {}; // "x_y" -> Cell
    
    // Client specific state
    this.isRegistered = false;
    this.registeredTeamName = "";
    this.selectedAgentId = null;
    this.lastLatency = 0;
    this.connectionStatus = "DISCONNECTED"; // DISCONNECTED, CONNECTED
    
    // Action plans for the current day
    // agentId -> Array of Action { order: 1, actionType: "MOVE"|"WAIT", targetX: null|number, targetY: null|number }
    this.localActionQueue = {};
    
    // Undo/Redo stacks for local action changes
    // Each stack entry is a deep copy of localActionQueue
    this.undoStack = [];
    this.redoStack = [];

    this.listeners = [];

    // Track active team's agent plans response
    this.lastSimulationResult = null;
  }

  subscribe(listener) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  notify() {
    for (const listener of this.listeners) {
      try {
        listener(this);
      } catch (e) {
        console.error("State listener error", e);
      }
    }
  }

  /**
   * Save the current action queue to the undo stack.
   * Clears the redo stack.
   */
  saveToHistory() {
    const stateCopy = JSON.parse(JSON.stringify(this.localActionQueue));
    this.undoStack.push(stateCopy);
    this.redoStack = []; // Clear redo stack on new actions
    if (this.undoStack.length > 50) {
      this.undoStack.shift(); // Limit undo depth
    }
  }

  undo() {
    if (this.undoStack.length === 0) {
      logger.warn("Nothing to undo.");
      return;
    }
    const currentCopy = JSON.parse(JSON.stringify(this.localActionQueue));
    this.redoStack.push(currentCopy);

    const previousState = this.undoStack.pop();
    this.localActionQueue = previousState;
    logger.info("Action queue undo applied.");
    this.notify();
  }

  redo() {
    if (this.redoStack.length === 0) {
      logger.warn("Nothing to redo.");
      return;
    }
    const currentCopy = JSON.parse(JSON.stringify(this.localActionQueue));
    this.undoStack.push(currentCopy);

    const nextState = this.redoStack.pop();
    this.localActionQueue = nextState;
    logger.info("Action queue redo applied.");
    this.notify();
  }

  /**
   * Initialize or update from server state DTO
   */
  updateFromServerState(stateData, latency = 0) {
    this.serverState = stateData;
    this.lastLatency = latency;
    this.connectionStatus = "CONNECTED";

    // Build fast cell lookup map
    this.cellMap = {};
    if (stateData.cells) {
      for (const cell of stateData.cells) {
        this.cellMap[`${cell.x}_${cell.y}`] = cell;
      }
    }

    // Check registration status
    if (this.registeredTeamName) {
      const team = stateData.teams?.find(t => t.teamName === this.registeredTeamName);
      if (team) {
        this.isRegistered = true;
        
        // Auto initialize action queues for all agents in the team
        let queuesChanged = false;
        for (const agent of team.agents) {
          if (!this.localActionQueue[agent.id]) {
            this.localActionQueue[agent.id] = [];
            queuesChanged = true;
          }
        }
        if (queuesChanged) {
          this.undoStack = [];
          this.redoStack = [];
        }
      } else {
        this.isRegistered = false;
      }
    }

    this.notify();
  }

  setDisconnected() {
    this.connectionStatus = "DISCONNECTED";
    this.notify();
  }

  // Getters
  getMyTeam() {
    if (!this.registeredTeamName) return null;
    return this.serverState.teams?.find(t => t.teamName === this.registeredTeamName) || null;
  }

  getAgents() {
    const team = this.getMyTeam();
    return team ? team.agents : [];
  }

  getSelectedAgent() {
    if (!this.selectedAgentId) return null;
    const agents = this.getAgents();
    return agents.find(a => a.id === this.selectedAgentId) || null;
  }

  // Setters/Operations
  setSelectedAgentId(id) {
    this.selectedAgentId = id;
    this.notify();
  }

  /**
   * Set server configuration (url, team name)
   */
  configure(baseUrl, teamName) {
    apiClient.setBaseUrl(baseUrl);
    apiClient.setTeamName(teamName);
    this.registeredTeamName = teamName;
    this.localActionQueue = {};
    this.undoStack = [];
    this.redoStack = [];
    this.selectedAgentId = null;
    this.lastSimulationResult = null;
    this.isRegistered = false;
    this.notify();
  }

  /**
   * Action Queue Editing functions
   */
  addAction(agentId, type, tx = null, ty = null) {
    this.saveToHistory();
    if (!this.localActionQueue[agentId]) {
      this.localActionQueue[agentId] = [];
    }
    const currentQueue = this.localActionQueue[agentId];
    
    // Next order
    const nextOrder = currentQueue.length + 1;
    currentQueue.push({
      order: nextOrder,
      actionType: type,
      targetX: tx,
      targetY: ty
    });

    logger.info(`Added action ${type} to Agent ${agentId} at order ${nextOrder}`);
    this.notify();
  }

  removeAction(agentId, index) {
    this.saveToHistory();
    const currentQueue = this.localActionQueue[agentId];
    if (currentQueue && index >= 0 && index < currentQueue.length) {
      const removed = currentQueue.splice(index, 1);
      // Re-index order
      currentQueue.forEach((action, idx) => {
        action.order = idx + 1;
      });
      logger.info(`Removed action at index ${index} for Agent ${agentId}`);
      this.notify();
    }
  }

  clearActionQueue(agentId) {
    this.saveToHistory();
    this.localActionQueue[agentId] = [];
    logger.info(`Cleared action queue for Agent ${agentId}`);
    this.notify();
  }

  duplicateAction(agentId, index) {
    this.saveToHistory();
    const currentQueue = this.localActionQueue[agentId];
    if (currentQueue && index >= 0 && index < currentQueue.length) {
      const act = currentQueue[index];
      const nextOrder = currentQueue.length + 1;
      currentQueue.push({
        order: nextOrder,
        actionType: act.actionType,
        targetX: act.targetX,
        targetY: act.targetY
      });
      logger.info(`Duplicated action ${act.actionType} for Agent ${agentId}`);
      this.notify();
    }
  }

  reorderAction(agentId, fromIndex, toIndex) {
    const queue = this.localActionQueue[agentId];
    if (!queue) return;
    if (fromIndex < 0 || fromIndex >= queue.length || toIndex < 0 || toIndex >= queue.length) return;
    
    this.saveToHistory();
    const [moved] = queue.splice(fromIndex, 1);
    queue.splice(toIndex, 0, moved);
    
    // Re-index
    queue.forEach((action, idx) => {
      action.order = idx + 1;
    });
    
    logger.info(`Reordered actions for Agent ${agentId} from ${fromIndex} to ${toIndex}`);
    this.notify();
  }

  /**
   * Run local validations and return report
   */
  runValidations() {
    const myTeam = this.getMyTeam();
    if (!myTeam) return { isValid: false, errors: {}, warnings: {} };
    
    const config = {
      maxFuel: this.serverState.maxFuel || 100,
      maxStepsPerTurn: this.serverState.maxStepsPerTurn || 5,
      roadFuelCost: this.serverState.roadFuelCost || 2,
      roadStepCost: this.serverState.roadStepCost || 1,
      plainFuelCost: this.serverState.plainFuelCost || 1,
      plainStepCost: this.serverState.plainStepCost || 2,
      mountainFuelCost: this.serverState.mountainFuelCost || 2,
      mountainStepCost: this.serverState.mountainStepCost || 3
    };

    // Merge default server config if missing
    // Wait, let's load matchConfig parameters dynamically from MatchStateResponse if server exposes it,
    // otherwise fallback to defaults in config.js / validation.
    // Let's use config properties.
    return validateActions(myTeam, this.cellMap, config, this.localActionQueue);
  }

  /**
   * API wrappers
   */
  async register() {
    try {
      const { data, latency } = await apiClient.registerTeam(this.registeredTeamName);
      logger.info(`Registered team successfully! Response latency: ${latency}ms`, "SERVER");
      this.isRegistered = true;
      this.localActionQueue = {};
      // Fetch state immediately to load coordinates
      await this.fetchState();
    } catch (e) {
      logger.error(`Registration failed: ${e.message}`, "CLIENT");
      throw e;
    }
  }

  async start() {
    try {
      const { data, latency } = await apiClient.startMatch();
      this.updateFromServerState(data, latency);
      logger.info("Match started!", "SERVER");
    } catch (e) {
      logger.error(`Failed to start match: ${e.message}`, "CLIENT");
      throw e;
    }
  }

  async fetchState() {
    try {
      const { data, latency } = await apiClient.getMatchState();
      this.updateFromServerState(data, latency);
    } catch (e) {
      this.setDisconnected();
      throw e;
    }
  }

  async submit() {
    const validationReport = this.runValidations();
    if (!validationReport.isValid) {
      logger.error("Submit aborted: Local validations found rule violations.", "CLIENT");
      throw new Error("Validation failed");
    }

    try {
      const currentDay = this.serverState.currentTurn;
      const { data, latency } = await apiClient.submitActions(currentDay, this.localActionQueue);
      
      this.lastSimulationResult = data;
      logger.info(`Plans submitted for day ${currentDay}. Latency: ${latency}ms`, "SERVER");
      
      // Clear actions local queues after submitting successfully
      // Wait, should we clear them? Yes, since a new day starts, the queue is cleared.
      // But wait! If we clear it, the user might want to see what they submitted, or they might wait for the new day.
      // In the game, the server runs the turn simulation immediately and moves the agents.
      // So once submitted successfully, the server has already executed the actions.
      // Let's clear the queue for the new day.
      for (const agentId of Object.keys(this.localActionQueue)) {
        this.localActionQueue[agentId] = [];
      }
      this.undoStack = [];
      this.redoStack = [];
      
      // Update state immediately to reflect new positions
      await this.fetchState();
    } catch (e) {
      logger.error(`Plan submission failed: ${e.message}`, "CLIENT");
      throw e;
    }
  }
}

export const gameState = new GameState();
export default gameState;
