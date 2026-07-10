import { gameState } from "../state/game_state.js";
import { logger } from "../logger/logger.js";
import { apiClient } from "../network/api_client.js";

// 1. MATCH STATUS PANEL
export class MatchStatusPanel {
  constructor(element) {
    this.element = element;
    gameState.subscribe(() => this.render());
    this.startCountdownTimer();
  }

  startCountdownTimer() {
    setInterval(() => {
      this.updateCountdown();
    }, 100);
  }

  updateCountdown() {
    const countdownEl = this.element.querySelector(".countdown-val");
    if (!countdownEl) return;

    const state = gameState.serverState;
    if (state.status !== "PLAYING") {
      countdownEl.textContent = "N/A";
      return;
    }

    const limit = state.turnTimeLimitMs || 1000;
    const startTime = state.turnStartTime || Date.now();
    const elapsed = Date.now() - startTime;
    const remaining = Math.max(0, limit - elapsed);

    countdownEl.textContent = `${(remaining / 1000).toFixed(1)}s`;
    
    // Add warning color if time is running out (< 2s)
    if (remaining < 2000) {
      countdownEl.classList.add("text-danger");
    } else {
      countdownEl.classList.remove("text-danger");
    }
  }

  render() {
    const state = gameState.serverState;
    const myTeam = gameState.getMyTeam();
    const isRegistered = gameState.isRegistered;

    this.element.innerHTML = `
      <div class="panel-section-grid">
        <div class="status-stat">
          <span class="stat-label">Game Status</span>
          <span class="stat-val status-${state.status ? state.status.toLowerCase() : 'waiting'}">${state.status || 'OFFLINE'}</span>
        </div>
        <div class="status-stat">
          <span class="stat-label">Current Turn (Day)</span>
          <span class="stat-val">${state.currentTurn || 0}</span>
        </div>
        <div class="status-stat">
          <span class="stat-label">Time Remaining</span>
          <span class="stat-val countdown-val">Calculating...</span>
        </div>
        <div class="status-stat">
          <span class="stat-label">My Score</span>
          <span class="stat-val text-primary">${myTeam ? myTeam.collectedUdon : 0} Udon</span>
        </div>
      </div>
      
      <div class="status-sub-info">
        <div>Registered Team: <strong>${gameState.registeredTeamName || "None"}</strong> (${isRegistered ? "Registered" : "Not Registered"})</div>
        <div>Server API Latency: <strong>${gameState.lastLatency} ms</strong></div>
      </div>
    `;
    this.updateCountdown();
  }
}


// 2. SCOREBOARD PANEL
export class ScoreboardPanel {
  constructor(element) {
    this.element = element;
    gameState.subscribe(() => this.render());
  }

  render() {
    const teams = gameState.serverState.teams || [];

    if (teams.length === 0) {
      this.element.innerHTML = `
        <div class="empty-state">No teams registered in this match yet.</div>
      `;
      return;
    }

    // Sort by collected udon descending
    const sortedTeams = [...teams].sort((a, b) => b.collectedUdon - a.collectedUdon);

    let rowsHtml = sortedTeams.map((team, idx) => {
      const isMe = team.teamName === gameState.registeredTeamName;
      const rank = idx + 1;
      
      return `
        <tr class="${isMe ? 'row-highlight' : ''}">
          <td style="text-align: center; font-weight: bold;">#${rank}</td>
          <td>
            ${team.teamName} ${isMe ? '<span class="badge-me">Me</span>' : ''}
            ${team.disqualified ? '<span class="badge-disqualified">DQ</span>' : ''}
          </td>
          <td style="text-align: right; font-weight: bold; color: #10B981;">${team.collectedUdon}</td>
          <td style="text-align: center; font-size:11px;">
            ${team.spamViolationCount || 0}
          </td>
        </tr>
      `;
    }).join("");

    this.element.innerHTML = `
      <table class="scoreboard-table">
        <thead>
          <tr>
            <th style="width: 60px; text-align: center;">Rank</th>
            <th>Team Name</th>
            <th style="text-align: right;">Udon Stock</th>
            <th style="width: 80px; text-align: center;">Violations</th>
          </tr>
        </thead>
        <tbody>
          ${rowsHtml}
        </tbody>
      </table>
    `;
  }
}


// 3. NETWORK MONITOR PANEL
export class NetworkPanel {
  constructor(element) {
    this.element = element;
    this.records = [];
    
    // Subscribe to API Client request logger
    apiClient.subscribeNetworkMonitor((record) => {
      this.addRecord(record);
    });

    this.render();
  }

  addRecord(record) {
    // Look up if this path + method + retry exists in pending state, if so update it
    const index = this.records.findIndex(
      r => r.path === record.path && r.method === record.method && r.retry === record.retry && r.status === "PENDING"
    );
    
    if (index !== -1) {
      this.records[index] = record;
    } else {
      this.records.unshift(record);
      if (this.records.length > 50) {
        this.records.pop();
      }
    }
    this.render();
  }

  render() {
    if (this.records.length === 0) {
      this.element.innerHTML = `
        <div class="empty-state">No network requests logged yet.</div>
      `;
      return;
    }

    const listHtml = this.records.map((r, idx) => {
      const isSuccess = r.status === "SUCCESS";
      const isPending = r.status === "PENDING";
      const isFailed = r.status === "FAILED" || r.status === "ERROR";
      
      let badgeClass = "badge-pending";
      if (isSuccess) badgeClass = "badge-success";
      if (isFailed) badgeClass = "badge-failed";

      const timeStr = r.timestamp.toTimeString().split(" ")[0];
      
      return `
        <div class="network-item">
          <div class="net-item-header">
            <span class="net-method ${r.method.toLowerCase()}">${r.method}</span>
            <span class="net-path" title="${r.path}">${r.path}</span>
            <span class="net-status ${badgeClass}">${r.statusCode || r.status}</span>
          </div>
          <div class="net-item-meta">
            <span>Time: ${timeStr}</span>
            <span>Latency: ${r.latency}ms</span>
            ${r.retry > 0 ? `<span class="text-warning">Retry: ${r.retry}</span>` : ""}
          </div>
          <div class="net-item-body">
            <details>
              <summary>Details (Payload/Response)</summary>
              <div class="net-json-block">
                ${r.requestBody ? `<div><strong>Payload:</strong><pre>${JSON.stringify(r.requestBody, null, 2)}</pre></div>` : ""}
                ${r.responseBody ? `<div><strong>Response:</strong><pre>${JSON.stringify(r.responseBody, null, 2)}</pre></div>` : ""}
                ${r.error ? `<div><strong>Error:</strong><pre style="color:#EF4444">${r.error}</pre></div>` : ""}
              </div>
            </details>
          </div>
        </div>
      `;
    }).join("");

    this.element.innerHTML = `
      <div class="network-list">
        ${listHtml}
      </div>
    `;
  }
}


// 4. LOGS PANEL
export class LogsPanel {
  constructor(element) {
    this.element = element;
    this.filterLevel = "ALL";
    this.filterType = "ALL";

    // Subscribe to global logs
    logger.subscribe({
      onLog: () => this.renderList(),
      onClear: () => this.renderList()
    });

    this.render();
  }

  render() {
    this.element.innerHTML = `
      <div class="logs-header-bar">
        <div class="log-filters">
          <select class="log-select-filter level-filter">
            <option value="ALL">All Levels</option>
            <option value="INFO">Info</option>
            <option value="WARN">Warning</option>
            <option value="ERROR">Error</option>
          </select>
          <select class="log-select-filter type-filter">
            <option value="ALL">All Types</option>
            <option value="CLIENT">Client</option>
            <option value="SERVER">Server</option>
          </select>
        </div>
        <button class="btn btn-sm btn-secondary clear-logs-btn">Clear</button>
      </div>
      <div class="logs-output-container"></div>
    `;

    // Listeners
    const lvlFilter = this.element.querySelector(".level-filter");
    const typFilter = this.element.querySelector(".type-filter");
    
    lvlFilter.addEventListener("change", (e) => {
      this.filterLevel = e.target.value;
      this.renderList();
    });

    typFilter.addEventListener("change", (e) => {
      this.filterType = e.target.value;
      this.renderList();
    });

    this.element.querySelector(".clear-logs-btn").addEventListener("click", () => {
      logger.clear();
    });

    this.renderList();
  }

  renderList() {
    const listContainer = this.element.querySelector(".logs-output-container");
    if (!listContainer) return;

    let logs = logger.getLogs();

    // Filtering
    if (this.filterLevel !== "ALL") {
      logs = logs.filter(l => l.level === this.filterLevel);
    }
    if (this.filterType !== "ALL") {
      logs = logs.filter(l => l.type === this.filterType);
    }

    if (logs.length === 0) {
      listContainer.innerHTML = `<div class="empty-state">No logs match filters.</div>`;
      return;
    }

    listContainer.innerHTML = logs.map(l => {
      const timeStr = l.timestamp.toTimeString().split(" ")[0];
      const ms = String(l.timestamp.getMilliseconds()).padStart(3, "0");
      
      let lvlClass = "log-info";
      if (l.level === "WARN") lvlClass = "log-warn";
      if (l.level === "ERROR") lvlClass = "log-error";

      return `
        <div class="log-line ${lvlClass}">
          <span class="log-time">[${timeStr}.${ms}]</span>
          <span class="log-tag">[${l.type}]</span>
          <span class="log-message">${escapeHtml(l.message)}</span>
        </div>
      `;
    }).join("");

    // Auto scroll to bottom
    listContainer.scrollTop = listContainer.scrollHeight;
  }
}

// Escape HTML utility
function escapeHtml(text) {
  return text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
