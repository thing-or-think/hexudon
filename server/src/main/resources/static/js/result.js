// HEXUDON Monitor Game Result Component
import { GameApi } from './api.js';

export class GameResult {
    /**
     * @param {HTMLElement} parentEl - Mounting target container
     * @param {String} gameId - Match ID to load results for
     * @param {Object} router - Reference to app router
     * @param {Function} showToast - Toast notifier callback
     */
    constructor(parentEl, gameId, router, showToast) {
        this.parentEl = parentEl;
        this.gameId = gameId;
        this.router = router;
        this.showToast = showToast;
        
        this.init();
    }
    
    async init() {
        this.renderLayout();
        await this.loadResults();
    }
    
    renderLayout() {
        this.parentEl.innerHTML = `
            <div class="results-page">
                <div class="results-header">
                    <div class="results-badge">
                        <i class="fa-solid fa-trophy"></i>
                    </div>
                    <h1>Match Standings</h1>
                    <p id="results-match-id">Game ID: ${this.gameId}</p>
                </div>
                
                <div id="results-loader" class="app-loader">
                    <div class="loader-spinner"></div>
                    <p>Fetching final standings...</p>
                </div>
                
                <div id="results-container" style="display:none; width: 100%; display:flex; flex-direction:column; gap:1.5rem;">
                    <!-- Winner Spotlight Card -->
                    <div class="winner-banner">
                        <span class="winner-lbl">Match Champion</span>
                        <div class="winner-name" id="val-winner-name">TEAM --</div>
                    </div>
                    
                    <!-- Rankings details table -->
                    <div class="results-details">
                        <table class="results-table">
                            <thead>
                                <tr>
                                    <th>Rank</th>
                                    <th>Team ID</th>
                                    <th style="text-align:right">Udon Bowls</th>
                                    <th style="text-align:right">Distinct Types</th>
                                    <th style="text-align:right">Response Time</th>
                                </tr>
                            </thead>
                            <tbody id="results-tbody">
                                <!-- Dynamic standings rows -->
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <div class="flex-row-center mt-2">
                    <button class="btn-primary" id="btn-result-lobby">
                        <i class="fa-solid fa-arrow-left"></i> Return to Games Lobby
                    </button>
                </div>
            </div>
        `;
        
        this.parentEl.querySelector('#btn-result-lobby').addEventListener('click', () => {
            this.router.navigate('/games');
        });
    }
    
    async loadResults() {
        const loader = this.parentEl.querySelector('#results-loader');
        const container = this.parentEl.querySelector('#results-container');
        
        try {
            const result = await GameApi.getResult(this.gameId);
            loader.style.display = 'none';
            container.style.display = 'flex';
            
            const ranking = result.ranking || [];
            const details = result.detail || {};
            
            // Set champion spotlight
            const winnerNameEl = this.parentEl.querySelector('#val-winner-name');
            if (ranking.length > 0) {
                winnerNameEl.textContent = ranking[0];
            } else {
                winnerNameEl.textContent = 'NO ACTIVE TEAMS';
            }
            
            // Set table standings
            const tbody = this.parentEl.querySelector('#results-tbody');
            tbody.innerHTML = '';
            
            ranking.forEach((teamId, index) => {
                const detail = details[teamId] || {
                    total_servings: 0,
                    distinct_types: 0,
                    cumulative_response_time: 0
                };
                
                const tr = document.createElement('tr');
                
                const rankNum = index + 1;
                let rankCellClass = 'results-rank';
                let rankIcon = rankNum;
                if (rankNum === 1) { rankCellClass += ' rank-1'; rankIcon = '<i class="fa-solid fa-medal"></i> 1'; }
                else if (rankNum === 2) { rankCellClass += ' rank-2'; rankIcon = '<i class="fa-solid fa-medal"></i> 2'; }
                else if (rankNum === 3) { rankCellClass += ' rank-3'; rankIcon = '<i class="fa-solid fa-medal"></i> 3'; }
                
                tr.innerHTML = `
                    <td class="${rankCellClass}">${rankIcon}</td>
                    <td style="font-weight:700">${teamId}</td>
                    <td style="text-align:right; font-family:var(--font-mono); font-weight:700">${detail.total_servings}</td>
                    <td style="text-align:right">${detail.distinct_types}</td>
                    <td style="text-align:right; font-family:var(--font-mono)">${detail.cumulative_response_time.toFixed(1)}ms</td>
                `;
                tbody.appendChild(tr);
            });
            
        } catch (error) {
            console.error('loadResults error:', error);
            loader.innerHTML = `
                <div class="text-center" style="padding:2rem 0">
                    <i class="fa-solid fa-triangle-exclamation" style="font-size:3rem; color:var(--traffic-congested); margin-bottom:1rem;"></i>
                    <h3>Standings Unreachable</h3>
                    <p>${error.message || 'Cannot read match completion standings.'}</p>
                </div>
            `;
        }
    }
}
