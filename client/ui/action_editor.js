import { gameState } from "../state/game_state.js";
import { Icons } from "../assets/icons.js";
import { isAdjacent } from "../utils/hex_utils.js";
import { logger } from "../logger/logger.js";

export class ActionEditor {
  constructor(containerElement) {
    this.container = containerElement;
    
    // Subscribe to state changes
    gameState.subscribe(() => this.render());
  }

  render() {
    this.container.innerHTML = "";

    const selectedAgent = gameState.getSelectedAgent();
    const myTeam = gameState.getMyTeam();

    // 1. Render Header controls (Undo, Redo, Submit)
    const toolbar = document.createElement("div");
    toolbar.className = "editor-toolbar";
    toolbar.innerHTML = `
      <div class="toolbar-left">
        <button class="btn btn-sm btn-secondary btn-icon undo-btn" title="Undo Action" ${gameState.undoStack.length === 0 ? "disabled" : ""}>
          ${Icons.UNDO("#fff", 14)} Undo
        </button>
        <button class="btn btn-sm btn-secondary btn-icon redo-btn" title="Redo Action" ${gameState.redoStack.length === 0 ? "disabled" : ""}>
          ${Icons.REDO("#fff", 14)} Redo
        </button>
      </div>
      <div class="toolbar-right">
        <button class="btn btn-sm btn-danger clear-all-btn" ${!selectedAgent ? "disabled" : ""}>
          ${Icons.TRASH("#fff", 14)} Clear Agent
        </button>
      </div>
    `;

    // Hook toolbar event listeners
    toolbar.querySelector(".undo-btn").addEventListener("click", () => gameState.undo());
    toolbar.querySelector(".redo-btn").addEventListener("click", () => gameState.redo());
    
    if (selectedAgent) {
      toolbar.querySelector(".clear-all-btn").addEventListener("click", () => {
        if (confirm(`Clear all planned actions for Agent ${selectedAgent.id}?`)) {
          gameState.clearActionQueue(selectedAgent.id);
        }
      });
    }

    this.container.appendChild(toolbar);

    if (!myTeam) {
      const emptyMsg = document.createElement("div");
      emptyMsg.className = "empty-state";
      emptyMsg.textContent = "Register a team to begin planning actions.";
      this.container.appendChild(emptyMsg);
      return;
    }

    if (!selectedAgent) {
      const emptyMsg = document.createElement("div");
      emptyMsg.className = "empty-state";
      emptyMsg.textContent = "Select an Agent from the Dashboard or Map to edit actions.";
      this.container.appendChild(emptyMsg);
      return;
    }

    // 2. Render Agent Header Info
    const agentHeader = document.createElement("div");
    agentHeader.className = "editor-agent-header";
    agentHeader.innerHTML = `
      <h3>Planning for ${selectedAgent.id} (${selectedAgent.type})</h3>
      <p class="subtitle">Click on adjacent cells on the map to add Move paths, or add Wait turns below.</p>
    `;
    this.container.appendChild(agentHeader);

    // 3. Render List of Actions
    const actionsList = document.createElement("div");
    actionsList.className = "actions-list-container";

    const queue = gameState.localActionQueue[selectedAgent.id] || [];
    
    // Validate to get specific errors for this agent
    const validationReport = gameState.runValidations();
    const agentErrors = validationReport.errors[selectedAgent.id] || [];
    const agentWarnings = validationReport.warnings[selectedAgent.id] || [];

    if (queue.length === 0) {
      actionsList.innerHTML = `
        <div class="empty-actions">
          No actions planned. Agent will WAITING by default.
        </div>
      `;
    } else {
      queue.forEach((action, index) => {
        const actionItem = document.createElement("div");
        
        // Check if this action has an error
        const hasError = agentErrors.some(e => e.order === action.order);
        const errorDetail = agentErrors.find(e => e.order === action.order);

        actionItem.className = `action-item-card ${hasError ? "has-error" : ""}`;
        
        let actionDesc = "";
        let detailsDesc = "";
        
        if (action.actionType === "WAIT") {
          actionDesc = `<strong>WAIT</strong>`;
          detailsDesc = `Consumes 1 Step`;
        } else if (action.actionType === "MOVE") {
          const cell = gameState.cellMap[`${action.targetX}_${action.targetY}`];
          const terrain = cell ? cell.terrainType : "UNKNOWN";
          const costs = getTerrainCosts(terrain);
          
          actionDesc = `<strong>MOVE</strong> to (${action.targetX}, ${action.targetY})`;
          detailsDesc = `<span class="terrain-badge terrain-${terrain.toLowerCase()}">${terrain}</span> [Cost: ${costs.step} steps, ${selectedAgent.type === "PATROL" ? `${costs.fuel} fuel` : "0 fuel"}]`;
        }

        actionItem.innerHTML = `
          <div class="action-num">${action.order}</div>
          <div class="action-body">
            <div class="action-desc">${actionDesc}</div>
            <div class="action-sub-details">${detailsDesc}</div>
            ${hasError ? `<div class="action-error-msg">${errorDetail.message}</div>` : ""}
          </div>
          <div class="action-controls">
            <button class="btn-icon-sm move-up-btn" title="Move Up" ${index === 0 ? "disabled" : ""}>▲</button>
            <button class="btn-icon-sm move-down-btn" title="Move Down" ${index === queue.length - 1 ? "disabled" : ""}>▼</button>
            <button class="btn-icon-sm duplicate-btn" title="Duplicate">${Icons.ADD("#10B981", 12)}</button>
            <button class="btn-icon-sm delete-btn" title="Delete">${Icons.TRASH("#EF4444", 12)}</button>
          </div>
        `;

        // Action Item Listeners
        actionItem.querySelector(".delete-btn").addEventListener("click", () => {
          gameState.removeAction(selectedAgent.id, index);
        });

        actionItem.querySelector(".duplicate-btn").addEventListener("click", () => {
          // Check limits
          const maxSteps = gameState.serverState.maxStepsPerTurn || 5;
          if (queue.length >= maxSteps) {
            logger.warn(`Action queue is full (${maxSteps} steps max).`);
            return;
          }
          gameState.duplicateAction(selectedAgent.id, index);
        });

        actionItem.querySelector(".move-up-btn").addEventListener("click", () => {
          gameState.reorderAction(selectedAgent.id, index, index - 1);
        });

        actionItem.querySelector(".move-down-btn").addEventListener("click", () => {
          gameState.reorderAction(selectedAgent.id, index, index + 1);
        });

        actionsList.appendChild(actionItem);
      });
    }

    this.container.appendChild(actionsList);

    // 4. Render validation summary errors/warnings
    if (agentWarnings.length > 0) {
      const warnContainer = document.createElement("div");
      warnContainer.className = "alert alert-warning";
      warnContainer.innerHTML = agentWarnings.map(w => `<div>⚠ ${w}</div>`).join("");
      this.container.appendChild(warnContainer);
    }

    // 5. Render Action Creators
    const creators = document.createElement("div");
    creators.className = "action-creator-buttons";
    
    const maxSteps = gameState.serverState.maxStepsPerTurn || 5;
    const isQueueFull = queue.length >= maxSteps;

    creators.innerHTML = `
      <button class="btn btn-secondary add-wait-btn" ${isQueueFull ? "disabled" : ""}>
        ${Icons.PLAY("#fff", 12)} Add WAIT Action
      </button>
    `;

    creators.querySelector(".add-wait-btn").addEventListener("click", () => {
      gameState.addAction(selectedAgent.id, "WAIT");
    });

    this.container.appendChild(creators);
  }
}

// Helpers
function getTerrainCosts(terrain) {
  const config = gameState.serverState;
  switch (terrain) {
    case "ROAD":
      return { step: config.roadStepCost || 1, fuel: config.roadFuelCost || 2 };
    case "PLAIN":
      return { step: config.plainStepCost || 2, fuel: config.plainFuelCost || 1 };
    case "MOUNTAIN":
      return { step: config.mountainStepCost || 3, fuel: config.mountainFuelCost || 2 };
    default:
      return { step: 999, fuel: 999 };
  }
}
