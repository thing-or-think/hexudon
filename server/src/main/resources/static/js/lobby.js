// HEXUDON Monitor Lobby Component
import { GameApi } from './api.js';

export class Lobby {
    /**
     * @param {HTMLElement} parentEl - Mounting target container
     * @param {Object} router - Reference to app router
     * @param {Function} showToast - Toast notifier callback
     */
    constructor(parentEl, router, showToast) {
        this.parentEl = parentEl;
        this.router = router;
        this.showToast = showToast;
        
        this.games = [];
        this.searchQuery = '';
        this.statusFilter = 'ALL';
        
        this.init();
    }
    
    async init() {
        this.renderLayout();
        this.setupEvents();
        await this.loadGames();
    }
    
    renderLayout() {
        this.parentEl.innerHTML = `
            <div class="lobby-header">
                <div class="lobby-title">
                    <h1>Available Matches</h1>
                    <p>Select a live game to watch or check results from finished matches</p>
                </div>
                <div class="lobby-controls">
                    <div class="search-input-wrapper">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input type="text" id="lobby-search" class="search-input" placeholder="Search by game ID...">
                    </div>
                    <select id="lobby-filter" class="filter-select">
                        <option value="ALL">All Statuses</option>
                        <option value="WAITING">Waiting</option>
                        <option value="PLAYING">Playing</option>
                        <option value="FINISHED">Finished</option>
                    </select>
                    <button id="lobby-refresh-btn" class="btn-primary">
                        <i class="fa-solid fa-rotate"></i> Refresh
                    </button>
                </div>
            </div>
            
            <div id="lobby-games-grid" class="games-grid">
                <div class="app-loader">
                    <div class="loader-spinner"></div>
                    <p>Loading available matches...</p>
                </div>
            </div>
        `;
    }
    
    setupEvents() {
        const searchInput = this.parentEl.querySelector('#lobby-search');
        searchInput.addEventListener('input', (e) => {
            this.searchQuery = e.target.value.toLowerCase().trim();
            this.filterAndRenderGames();
        });
        
        const filterSelect = this.parentEl.querySelector('#lobby-filter');
        filterSelect.addEventListener('change', (e) => {
            this.statusFilter = e.target.value;
            this.filterAndRenderGames();
        });
        
        const refreshBtn = this.parentEl.querySelector('#lobby-refresh-btn');
        refreshBtn.addEventListener('click', () => {
            this.loadGames();
            this.showToast('Refreshing match list...', 'info');
        });
    }
    
    async loadGames() {
        const gridContainer = this.parentEl.querySelector('#lobby-games-grid');
        gridContainer.innerHTML = `
            <div class="app-loader">
                <div class="loader-spinner"></div>
                <p>Loading available matches...</p>
            </div>
        `;
        
        try {
            const response = await GameApi.getGames();
            this.games = response.games || [];
            
            // For each game, we asynchronously poll its state once to set the real status badge,
            // because `GET /api/game/list` returns static configs and does not contain the match status.
            await this.enrichGameStatuses();
            this.filterAndRenderGames();
        } catch (error) {
            gridContainer.innerHTML = `
                <div class="no-games text-center">
                    <i class="fa-solid fa-triangle-exclamation" style="color:var(--traffic-congested)"></i>
                    <h3>Failed to load games</h3>
                    <p>${error.message || 'Unable to connect to the server.'}</p>
                    <button id="lobby-retry-btn" class="btn-primary mt-2" style="margin: 1.5rem auto 0 auto;">
                        <i class="fa-solid fa-rotate-right"></i> Retry Connection
                    </button>
                </div>
            `;
            const retryBtn = gridContainer.querySelector('#lobby-retry-btn');
            if (retryBtn) {
                retryBtn.addEventListener('click', () => this.loadGames());
            }
        }
    }
    
    async enrichGameStatuses() {
        // Parallel status checks to keep Lobby extremely responsive
        const statusPromises = this.games.map(async (game) => {
            try {
                const state = await GameApi.getState(game.gameId);
                game.status = state.status || 'WAITING'; // NOT_STARTED / REGISTERING / PLAYING / FINISHED
            } catch (err) {
                // If get state fails (e.g. 404), default to WAITING
                game.status = 'WAITING';
            }
        });
        await Promise.all(statusPromises);
    }
    
    filterAndRenderGames() {
        const gridContainer = this.parentEl.querySelector('#lobby-games-grid');
        
        // Apply filters
        const filteredGames = this.games.filter(game => {
            const matchesSearch = game.gameId.toLowerCase().includes(this.searchQuery);
            
            // Map state-status into basic filters:
            // WAITING: NOT_STARTED, REGISTERING
            // PLAYING: PLAYING
            // FINISHED: FINISHED
            let statusCat = 'WAITING';
            if (game.status === 'PLAYING') statusCat = 'PLAYING';
            else if (game.status === 'FINISHED') statusCat = 'FINISHED';
            
            const matchesFilter = this.statusFilter === 'ALL' || statusCat === this.statusFilter;
            
            return matchesSearch && matchesFilter;
        });
        
        if (filteredGames.length === 0) {
            gridContainer.innerHTML = `
                <div class="no-games">
                    <i class="fa-regular fa-folder-open"></i>
                    <h3>No matches found</h3>
                    <p>Try resetting filters or checking the admin panel to start a new match</p>
                </div>
            `;
            return;
        }
        
        gridContainer.innerHTML = '';
        filteredGames.forEach(game => {
            const card = document.createElement('div');
            
            let statusClass = 'waiting';
            let statusText = 'Waiting';
            if (game.status === 'PLAYING') {
                statusClass = 'playing';
                statusText = 'Live';
            } else if (game.status === 'FINISHED') {
                statusClass = 'finished';
                statusText = 'Finished';
            } else if (game.status === 'REGISTERING') {
                statusClass = 'waiting';
                statusText = 'Registering';
            }
            
            // Format start time date
            const dateStr = new Date(game.startsAt * 1000).toLocaleString();
            
            card.className = `game-card ${statusClass}`;
            card.innerHTML = `
                <div class="game-card-header">
                    <span class="game-card-id" title="${game.gameId}">${game.gameId}</span>
                    <span class="game-card-status ${statusClass}">${statusText}</span>
                </div>
                
                <div class="game-card-details">
                    <div class="detail-item">
                        <span class="detail-label">Map Dimensions</span>
                        <span class="detail-val">${game.map.width} x ${game.map.height} Hex</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Days Limit</span>
                        <span class="detail-val">${game.totalDays} days</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Agents / Team</span>
                        <span class="detail-val">${game.players}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Fuel capacity</span>
                        <span class="detail-val">${game.fuelLimits} L</span>
                    </div>
                </div>
                
                <div class="detail-item mt-2">
                    <span class="detail-label"><i class="fa-regular fa-clock"></i> Starts At</span>
                    <span class="detail-val" style="font-size:0.8rem">${dateStr}</span>
                </div>
                
                <div class="game-card-actions">
                    <button class="btn-card btn-card-watch" data-game-id="${game.gameId}">
                        <i class="fa-solid fa-tv"></i> Watch Match
                    </button>
                </div>
            `;
            
            // Watch button handler
            const watchBtn = card.querySelector('.btn-card-watch');
            watchBtn.addEventListener('click', () => {
                this.router.navigate(`/game/${game.gameId}`);
            });
            
            gridContainer.appendChild(card);
        });
    }
}
