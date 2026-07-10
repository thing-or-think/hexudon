import { gameState } from "../state/game_state.js";
import { Icons } from "../assets/icons.js";

export class AgentDashboard {
  constructor(containerElement, onCenterAgentCallback) {
    this.container = containerElement;
    this.onCenterAgent = onCenterAgentCallback; // Callback to mapRenderer.centerOn(x, y)
    
    // Subscribe to state changes
    gameState.subscribe(() => this.render());
  }

  render() {
    this.container.innerHTML = "";

    const team = gameState.getMyTeam();
    if (!team) {
      this.container.innerHTML = `
        <div class="empty-state">
          Register your team to view agents
        </div>
      `;
      return;
    }

    const agents = team.agents || [];
    if (agents.length === 0) {
      this.container.innerHTML = `
        <div class="empty-state">
          No agents found
        </div>
      `;
      return;
    }

    agents.forEach(agent => {
      const card = document.createElement("div");
      const isSelected = gameState.selectedAgentId === agent.id;
      
      card.className = `agent-card ${isSelected ? "selected" : ""}`;
      
      const isPatrol = agent.type === "PATROL";
      const iconColor = isPatrol ? "#60A5FA" : "#F59E0B";
      const iconHtml = isPatrol ? Icons.PATROL(iconColor, 20) : Icons.REFUEL(iconColor, 20);

      // Validation warnings for this agent
      const validationReport = gameState.runValidations();
      const agentWarnings = validationReport.warnings[agent.id] || [];
      const agentErrors = validationReport.errors[agent.id] || [];
      const hasErrors = agentErrors.length > 0;

      // Status indicator
      const currentQueue = gameState.localActionQueue[agent.id] || [];
      const actionCount = currentQueue.length;

      card.innerHTML = `
        <div class="agent-card-header">
          <div class="agent-id-section">
            ${iconHtml}
            <span class="agent-id">${agent.id}</span>
            <span class="agent-badge ${agent.type.toLowerCase()}">${agent.type}</span>
          </div>
          <div class="agent-status-indicator">
            ${actionCount > 0 ? `<span class="badge-active">${actionCount} Actions</span>` : `<span class="badge-idle">Idle</span>`}
            ${hasErrors ? `<span class="badge-error" title="Has errors">⚠</span>` : ""}
          </div>
        </div>

        <div class="agent-details">
          <div class="detail-row">
            <span class="detail-label">Location:</span>
            <span class="detail-val">(${agent.posX}, ${agent.posY})</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Steps Remaining:</span>
            <span class="detail-val">${agent.remainingSteps}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Fuel:</span>
            <span class="detail-val">
              ${isPatrol ? `${agent.fuel} / 100` : "Unlimited (Refuel)"}
            </span>
          </div>
        </div>

        ${isPatrol ? `
          <div class="fuel-progress-bar">
            <div class="fuel-progress-fill ${agent.fuel < 30 ? 'low' : ''}" style="width: ${Math.min(100, Math.max(0, agent.fuel))}%"></div>
          </div>
        ` : ""}

        <div class="agent-actions">
          <button class="btn btn-sm btn-primary select-btn">
            ${isSelected ? "Selected" : "Select"}
          </button>
          <button class="btn btn-sm btn-secondary center-btn">
            Center Map
          </button>
        </div>
      `;

      // Button listeners
      card.querySelector(".select-btn").addEventListener("click", () => {
        gameState.setSelectedAgentId(agent.id);
      });

      card.querySelector(".center-btn").addEventListener("click", () => {
        if (this.onCenterAgent) {
          this.onCenterAgent(agent.posX, agent.posY);
        }
      });

      this.container.appendChild(card);
    });
  }
}
