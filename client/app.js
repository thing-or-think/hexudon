import { gameState } from "./state/game_state.js";
import { logger } from "./logger/logger.js";
import { MapRenderer } from "./renderer/map_renderer.js";
import { AgentDashboard } from "./ui/agent_dashboard.js";
import { ActionEditor } from "./ui/action_editor.js";
import {
  MatchStatusPanel,
  ScoreboardPanel,
  NetworkPanel,
  LogsPanel
} from "./ui/dashboard_panels.js";
import { Config } from "./config/config.js";

// Global component references
let mapRenderer;
let agentDashboard;
let actionEditor;
let matchStatusPanel;
let scoreboardPanel;
let networkPanel;
let logsPanel;

let hasAutoFittedMap = false;
let pollingIntervalId = null;

// Initialize app when DOM is fully loaded
window.addEventListener("DOMContentLoaded", () => {
  logger.info("Initializing HEXUDON Game Client...");

  // 1. Initialize UI panels
  const svgEl = document.getElementById("hex-map-svg");
  const tooltipEl = document.getElementById("map-tooltip");
  
  mapRenderer = new MapRenderer(svgEl, tooltipEl);
  
  agentDashboard = new AgentDashboard(
    document.getElementById("agent-dashboard"),
    (x, y) => mapRenderer.centerOn(x, y) // Allow dashboard to center map
  );
  
  actionEditor = new ActionEditor(
    document.getElementById("action-editor")
  );
  
  matchStatusPanel = new MatchStatusPanel(
    document.getElementById("match-status-panel")
  );
  
  scoreboardPanel = new ScoreboardPanel(
    document.getElementById("scoreboard-panel")
  );
  
  networkPanel = new NetworkPanel(
    document.getElementById("network-panel")
  );
  
  logsPanel = new LogsPanel(
    document.getElementById("logs-panel")
  );

  // Subscribe map rendering to game state
  gameState.subscribe((state) => {
    mapRenderer.render();
    
    // Auto-fit map once grid cells are loaded
    if (state.serverState.cells && state.serverState.cells.length > 0 && !hasAutoFittedMap) {
      setTimeout(() => {
        mapRenderer.fitMap();
        hasAutoFittedMap = true;
      }, 100);
    }

    // Update connection status UI
    updateConnectionStatusUI(state.connectionStatus);
  });

  // 2. Setup control listeners
  setupControlListeners();

  // 3. Initialize default configurations
  const defaultUrl = localStorage.getItem("hexudon_server_url") || Config.DEFAULT_BASE_URL;
  const defaultTeam = localStorage.getItem("hexudon_team_name") || Config.DEFAULT_TEAM_NAME;
  
  document.getElementById("server-url").value = defaultUrl;
  document.getElementById("team-name").value = defaultTeam;
  
  // Apply defaults
  gameState.configure(defaultUrl, defaultTeam);

  // 4. Start polling loop
  startStatePolling();
  
  logger.info("Client initialized successfully. Ready to connect!");
});

function setupControlListeners() {
  // Config Apply button
  document.getElementById("btn-save-config").addEventListener("click", () => {
    const url = document.getElementById("server-url").value.trim();
    const team = document.getElementById("team-name").value.trim();

    if (!url || !team) {
      logger.error("Server URL and Team Name cannot be empty.");
      alert("Please fill in both Server URL and Team Name.");
      return;
    }

    localStorage.setItem("hexudon_server_url", url);
    localStorage.setItem("hexudon_team_name", team);
    
    hasAutoFittedMap = false; // Reset map fit on config changes
    gameState.configure(url, team);
    
    // Restart polling
    startStatePolling();
  });

  // Register Team button
  document.getElementById("btn-register").addEventListener("click", async () => {
    const regBtn = document.getElementById("btn-register");
    regBtn.disabled = true;
    try {
      logger.info(`Registering team: ${gameState.registeredTeamName}...`);
      await gameState.register();
      alert("Team registered successfully!");
    } catch (e) {
      alert(`Registration failed: ${e.message}`);
    } finally {
      regBtn.disabled = false;
    }
  });

  // Start Match button
  document.getElementById("btn-start").addEventListener("click", async () => {
    if (confirm("Are you sure you want to start the match?")) {
      try {
        await gameState.start();
        logger.info("Match started successfully.");
      } catch (e) {
        alert(`Failed to start match: ${e.message}`);
      }
    }
  });

  // Sync button
  document.getElementById("btn-refresh").addEventListener("click", async () => {
    const btn = document.getElementById("btn-refresh");
    btn.disabled = true;
    try {
      await gameState.fetchState();
      logger.info("Game state synchronized.");
    } catch (e) {
      logger.error(`Manual synchronization failed: ${e.message}`);
    } finally {
      btn.disabled = false;
    }
  });

  // Submit Daily Actions button
  document.getElementById("btn-submit-actions").addEventListener("click", async () => {
    const subBtn = document.getElementById("btn-submit-actions");
    subBtn.disabled = true;
    try {
      await gameState.submit();
      alert("Actions submitted and executed successfully!");
    } catch (e) {
      if (e.message !== "Validation failed") {
        alert(`Submission failed: ${e.message}`);
      } else {
        alert("Submission aborted. Please correct action errors listed in the editor.");
      }
    } finally {
      subBtn.disabled = false;
    }
  });

  // Zoom controls
  document.getElementById("map-btn-zoom-in").addEventListener("click", () => {
    mapRenderer.zoom = Math.min(mapRenderer.zoom * 1.2, 3.0);
    mapRenderer.applyTransform();
  });

  document.getElementById("map-btn-zoom-out").addEventListener("click", () => {
    mapRenderer.zoom = Math.max(mapRenderer.zoom / 1.2, 0.4);
    mapRenderer.applyTransform();
  });

  document.getElementById("map-btn-fit").addEventListener("click", () => {
    mapRenderer.fitMap();
  });
}

function startStatePolling() {
  if (pollingIntervalId) {
    clearInterval(pollingIntervalId);
  }

  // Poll state immediately, then set interval
  gameState.fetchState().catch(() => {});

  pollingIntervalId = setInterval(async () => {
    try {
      await gameState.fetchState();
    } catch (e) {
      // Errors are handled and logged by the state/client modules
    }
  }, Config.POLL_INTERVAL_MS);
}

function updateConnectionStatusUI(status) {
  const dot = document.getElementById("connection-status-dot");
  const text = document.getElementById("connection-status-text");
  
  if (!dot || !text) return;

  if (status === "CONNECTED") {
    dot.className = "status-dot connected";
    text.textContent = "Connected";
  } else {
    dot.className = "status-dot disconnected";
    text.textContent = "Disconnected";
  }
}
