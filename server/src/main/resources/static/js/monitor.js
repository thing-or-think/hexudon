// HEXUDON Monitor Game Monitor Component
import { GameApi } from './api.js';
import { HexMap } from './map.js';

export class GameMonitor {
    /**
     * @param {HTMLElement} parentEl - Mounting target container
     * @param {String} gameId - Match ID to watch
     * @param {Object} router - Reference to app router
     * @param {Function} showToast - Toast notifier callback
     */
    constructor(parentEl, gameId, router, showToast) {
        this.parentEl = parentEl;
        this.gameId = gameId;
        this.router = router;
        this.showToast = showToast;
        
        this.boardConfig = null;
        this.hexMap = null;
        this.pollingIntervalId = null;
        this.countdownIntervalId = null;
        
        // Live state cache
        this.currentState = null;
        this.remainingSeconds = 0;
        
        this.init();
    }
    
    async init() {
        this.renderLayout();
        await this.loadBoard();
    }
    
    renderLayout() {
        this.parentEl.innerHTML = `
            <div class="monitor-layout">
                <!-- Main Map Panel -->
                <div class="monitor-main-panel">
                    <div class="monitor-header">
                        <div class="monitor-game-info">
                            <button id="monitor-back-btn" class="btn-back" title="Go back to lobby">
                                <i class="fa-solid fa-arrow-left"></i>
                            </button>
                            <div class="monitor-title">
                                <h2 id="monitor-game-id-title">Loading Match...</h2>
                                <span id="monitor-status-badge" class="game-card-status waiting">WAITING</span>
                            </div>
                        </div>
                        
                        <div class="monitor-status-section">
                            <div class="monitor-stat-pill">
                                <span class="stat-pill-lbl">Current Day</span>
                                <span class="stat-pill-val" id="val-day">--</span>
                            </div>
                            <div class="monitor-stat-pill">
                                <span class="stat-pill-lbl">Time Remaining</span>
                                <span class="stat-pill-val countdown" id="val-timer">--s</span>
                            </div>
                        </div>
                    </div>
                    
                    <!-- SVG hex grid map -->
                    <div class="map-viewport-wrapper" id="map-container">
                        <div class="app-loader">
                            <div class="loader-spinner"></div>
                            <p>Loading Game Board Assets...</p>
                        </div>
                    </div>
                    
                    <!-- Map action controls overlays -->
                    <div class="map-controls-overlay">
                        <button id="map-zoom-in" class="map-ctrl-btn" title="Zoom In"><i class="fa-solid fa-plus"></i></button>
                        <button id="map-zoom-out" class="map-ctrl-btn" title="Zoom Out"><i class="fa-solid fa-minus"></i></button>
                        <button id="map-recenter" class="map-ctrl-btn" title="Recenter Map"><i class="fa-solid fa-expand"></i></button>
                    </div>
                </div>
                
                <!-- Side Scoreboard & Information Panel -->
                <div class="monitor-side-panel">
                    <!-- Live scoreboard -->
                    <div class="side-block">
                        <h3><i class="fa-solid fa-trophy"></i> Scoreboard</h3>
                        <table class="scoreboard-table">
                            <thead>
                                <tr>
                                    <th>Team</th>
                                    <th>Servings</th>
                                    <th>Types</th>
                                    <th>Avg Resp</th>
                                </tr>
                            </thead>
                            <tbody id="scoreboard-tbody">
                                <tr>
                                    <td colspan="4" class="text-center" style="color:var(--text-muted); padding: 1.5rem 0;">No active teams</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- Traffic condition legend -->
                    <div class="side-block">
                        <h3><i class="fa-solid fa-road"></i> Traffic Legend</h3>
                        <div class="traffic-legend-grid">
                            <div class="legend-item">
                                <div class="legend-color-box normal"></div>
                                <div class="legend-info">
                                    <span class="legend-name">Normal Traffic</span>
                                    <span class="legend-desc">Regular speeds, low fuel costs</span>
                                </div>
                            </div>
                            <div class="legend-item">
                                <div class="legend-color-box busy"></div>
                                <div class="legend-info">
                                    <span class="legend-name">Busy Traffic</span>
                                    <span class="legend-desc">Mild congestion, minor speed delays</span>
                                </div>
                            </div>
                            <div class="legend-item">
                                <div class="legend-color-box congested"></div>
                                <div class="legend-info">
                                    <span class="legend-name">Congested Traffic</span>
                                    <span class="legend-desc">Gridlock, major fuel cost penalties</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Active agents details -->
                    <div class="side-block">
                        <h3><i class="fa-solid fa-truck-monster"></i> Team Agent Status</h3>
                        <div class="agent-status-list" id="agent-status-container">
                            <p class="text-center" style="color:var(--text-muted); padding: 1rem 0;">No agent tracking data</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Back Button
        this.parentEl.querySelector('#monitor-back-btn').addEventListener('click', () => {
            this.router.navigate('/games');
        });
    }
    
    async loadBoard() {
        const container = this.parentEl.querySelector('#map-container');
        try {
            const board = await GameApi.getBoard(this.gameId);
            this.boardConfig = board;
            
            // Set header title
            this.parentEl.querySelector('#monitor-game-id-title').textContent = `Match: ${this.gameId}`;
            
            // Initialize SVG Map
            this.hexMap = new HexMap(container, board);
            
            // Setup map control buttons
            this.parentEl.querySelector('#map-zoom-in').addEventListener('click', () => this.hexMap.zoomIn());
            this.parentEl.querySelector('#map-zoom-out').addEventListener('click', () => this.hexMap.zoomOut());
            this.parentEl.querySelector('#map-recenter').addEventListener('click', () => this.hexMap.centerMap());
            
            // Trigger first state load immediately, then start interval polling
            await this.loadState();
            this.startPolling();
            this.startLocalTimer();
            
        } catch (error) {
            console.error('loadBoard error:', error);
            container.innerHTML = `
                <div class="no-games text-center">
                    <i class="fa-solid fa-triangle-exclamation" style="color:var(--traffic-congested)"></i>
                    <h3>Game Board Error</h3>
                    <p>${error.message || 'Cannot load the static map layout for this match.'}</p>
                    <button id="monitor-retry-btn" class="btn-primary mt-2" style="margin: 1.5rem auto 0 auto;">
                        <i class="fa-solid fa-rotate-right"></i> Retry Loading Board
                    </button>
                </div>
            `;
            const retryBtn = container.querySelector('#monitor-retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', () => this.loadBoard());
            }
        }
    }
    
    startPolling() {
        this.stopPolling();
        // Polling interval: 1.5 seconds (1500 ms)
        this.pollingIntervalId = setInterval(() => this.loadState(), 1500);
    }
    
    stopPolling() {
        if (this.pollingIntervalId) {
            clearInterval(this.pollingIntervalId);
            this.pollingIntervalId = null;
        }
    }
    
    startLocalTimer() {
        if (this.countdownIntervalId) {
            clearInterval(this.countdownIntervalId);
        }
        this.countdownIntervalId = setInterval(() => {
            if (this.remainingSeconds > 0) {
                this.remainingSeconds--;
                this.updateTimerDisplay();
            }
        }, 1000);
    }
    
    stopLocalTimer() {
        if (this.countdownIntervalId) {
            clearInterval(this.countdownIntervalId);
            this.countdownIntervalId = null;
        }
    }
    
    async loadState() {
        try {
            const state = await GameApi.getState(this.gameId);
            this.currentState = state;
            
            // 1. Update Game Status Badge
            const badge = this.parentEl.querySelector('#monitor-status-badge');
            badge.textContent = state.status;
            badge.className = `game-card-status ${state.status.toLowerCase()}`;
            
            // 2. Update Day Info
            // backend uses 0-based days, display as 1-based or 0-based as is. Let's do 1-based (Day currentDay + 1)
            this.parentEl.querySelector('#val-day').textContent = state.currentDay + 1;
            
            // 3. Update local count down seconds
            this.remainingSeconds = Math.max(0, state.remainingTime);
            this.updateTimerDisplay();
            
            // 4. Update Map Traffic conditions
            if (this.hexMap) {
                this.hexMap.updateTraffic(state.mapStatus);
                this.hexMap.updateAgents(state.teams);
            }
            
            // 5. Update Scoreboard Panel
            this.updateScoreboard(state.teams || []);
            
            // 6. Update Agent status lists
            this.updateAgentDetailsList(state.teams || []);
            
            // 7. Check if Finished
            if (state.status === 'FINISHED') {
                this.showToast('Match completed! Compiling standings...', 'success');
                this.stopPolling();
                this.stopLocalTimer();
                
                // Fetch final results and show standings screen
                this.router.navigate(`/game/${this.gameId}/result`);
            }
            
        } catch (error) {
            console.error('loadState error:', error);
            
            // If the game was deleted (returns 404/Resource Not Found)
            if (error.status === 404 || error.code === 'RESOURCE_NOT_FOUND') {
                this.stopPolling();
                this.stopLocalTimer();
                this.showGameDeletedDialog();
            }
        }
    }
    
    updateTimerDisplay() {
        const timerEl = this.parentEl.querySelector('#val-timer');
        if (timerEl) {
            timerEl.textContent = `${this.remainingSeconds}s`;
        }
    }
    
    updateScoreboard(teams = []) {
        const tbody = this.parentEl.querySelector('#scoreboard-tbody');
        if (!tbody) return;
        
        if (teams.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center" style="color:var(--text-muted); padding: 1.5rem 0;">No active teams</td>
                </tr>
            `;
            return;
        }
        
        // Sort teams based on score metrics
        const sorted = [...teams].sort((a, b) => {
            const scoreA = a.score || { total_servings: 0, distinct_types: 0, cumulative_response_time: 0 };
            const scoreB = b.score || { total_servings: 0, distinct_types: 0, cumulative_response_time: 0 };
            
            if (scoreB.total_servings !== scoreA.total_servings) {
                return scoreB.total_servings - scoreA.total_servings;
            }
            if (scoreB.distinct_types !== scoreA.distinct_types) {
                return scoreB.distinct_types - scoreA.distinct_types;
            }
            return scoreA.cumulative_response_time - scoreB.cumulative_response_time;
        });
        
        tbody.innerHTML = '';
        sorted.forEach((team, index) => {
            const score = team.score || { total_servings: 0, distinct_types: 0, cumulative_response_time: 0 };
            
            const tr = document.createElement('tr');
            tr.className = 'team-rank-row';
            
            const teamColorVar = `var(--team-${(index + 1) > 10 ? 10 : (index + 1)})`;
            
            // Format response time: convert to seconds or keep ms
            const respTimeStr = score.cumulative_response_time.toFixed(1) + 'ms';
            
            tr.innerHTML = `
                <td>
                    <div class="scoreboard-team-name">
                        <span class="team-color-indicator" style="background-color: ${teamColorVar}"></span>
                        <span title="${team.teamId}">${team.teamId}</span>
                    </div>
                </td>
                <td class="scoreboard-score">${score.total_servings}</td>
                <td><span class="scoreboard-substats">${score.distinct_types}</span></td>
                <td><span class="scoreboard-substats" style="font-family:var(--font-mono)">${respTimeStr}</span></td>
            `;
            tbody.appendChild(tr);
        });
    }
    
    updateAgentDetailsList(teams = []) {
        const listContainer = this.parentEl.querySelector('#agent-status-container');
        if (!listContainer) return;
        
        let allAgents = [];
        let teamIndex = 1;
        
        teams.forEach(team => {
            const teamColor = `var(--team-${teamIndex > 10 ? 10 : teamIndex})`;
            const agents = team.agents || [];
            agents.forEach((agent, index) => {
                allAgents.push({
                    teamId: team.teamId,
                    teamColor,
                    agentId: index,
                    kind: agent.kind, // 0-Patrol, 1-Refuel
                    pos: agent.pos,
                    fuel: agent.fuel
                });
            });
            teamIndex++;
        });
        
        if (allAgents.length === 0) {
            listContainer.innerHTML = `<p class="text-center" style="color:var(--text-muted); padding: 1rem 0;">No active agents</p>`;
            return;
        }
        
        listContainer.innerHTML = '';
        allAgents.forEach(agent => {
            const item = document.createElement('div');
            item.className = 'agent-status-item';
            
            const kindText = agent.kind === 0 ? 'Patrol' : 'Refuel';
            const fuelLimit = this.boardConfig?.fuelLimits || 100;
            const fuelPercent = agent.kind === 0 ? Math.max(0, Math.min(100, (agent.fuel / fuelLimit) * 100)) : 0;
            
            let fuelHtml = '<span style="color:var(--text-muted)">N/A</span>';
            if (agent.kind === 0) {
                const fuelClass = fuelPercent < 25 ? 'low' : '';
                fuelHtml = `
                    <div class="agent-fuel-bar-container">
                        <div class="agent-fuel-bar ${fuelClass}" style="width: ${fuelPercent}%"></div>
                    </div>
                    <span style="font-family:var(--font-mono); font-size:0.75rem">${agent.fuel}L</span>
                `;
            }
            
            item.innerHTML = `
                <div class="agent-status-meta">
                    <span class="team-color-indicator" style="background-color: ${agent.teamColor}"></span>
                    <span class="agent-id-badge">#${agent.agentId}</span>
                    <span class="agent-kind-text">${kindText}</span>
                </div>
                <div class="agent-status-stats">
                    <span style="color:var(--text-secondary); font-family:var(--font-mono)">@${agent.pos}</span>
                    ${fuelHtml}
                </div>
            `;
            listContainer.appendChild(item);
        });
    }
    
    showGameDeletedDialog() {
        const dialog = document.createElement('div');
        dialog.className = 'dialog-overlay';
        dialog.innerHTML = `
            <div class="dialog-box">
                <i class="fa-solid fa-circle-exclamation" style="font-size:3rem; color:var(--traffic-congested); margin-bottom: 1rem;"></i>
                <h3>Match Deleted</h3>
                <p>This match is no longer available on the server. An administrator might have deleted this game.</p>
                <div class="dialog-actions">
                    <button class="btn-primary" id="dialog-confirm-btn">Return to Lobby</button>
                </div>
            </div>
        `;
        document.body.appendChild(dialog);
        
        dialog.querySelector('#dialog-confirm-btn').addEventListener('click', () => {
            dialog.remove();
            this.router.navigate('/games');
        });
    }
    
    destroy() {
        this.stopPolling();
        this.stopLocalTimer();
    }
}
