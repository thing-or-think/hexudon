// HEXUDON Monitor Admin Panel Component
import { GameApi } from './api.js';
import { HexMap } from './map.js';

export class AdminPanel {
    /**
     * @param {HTMLElement} parentEl - Mounting target container
     * @param {Object} router - Reference to app router
     * @param {Function} showToast - Toast notifier callback
     */
    constructor(parentEl, router, showToast) {
        this.parentEl = parentEl;
        this.router = router;
        this.showToast = showToast;
        
        // Active view in Admin Panel
        this.currentView = 'create'; // 'create' or 'manage'
        
        // Wizard step counter
        this.wizardStep = 1;
        
        // Maps cache
        this.generatedMap = null;
        this.previewMapInstance = null;
        this.wizardMapInstance = null;
        
        // Forms memory
        this.wizardData = {
            gameId: '',
            startsAt: Math.floor(Date.now() / 1000) + 3600, // default starts in 1 hour
            agentSelectionTimeLimit: 60.0,
            fuelLimits: 20,
            players: 8,
            busyThreshold: 2.0,
            jammedThreshold: 4.0,
            agents: [],
            daySeconds: [60.0, 60.0],
            daySteps: [100, 100],
            map: null // Holds width, height, cells, spots
        };
        
        this.init();
    }
    
    init() {
        this.renderLayout();
        this.setupMenu();
        this.showView(this.currentView);
    }
    
    renderLayout() {
        this.parentEl.innerHTML = `
            <div class="admin-layout">
                <!-- Admin Sidebar Menu -->
                <div class="admin-sidebar">
                    <div class="side-block">
                        <h3><i class="fa-solid fa-gears"></i> Admin Actions</h3>
                        <div class="admin-btn-group">
                            <button class="admin-menu-btn active" id="menu-btn-create">
                                <i class="fa-solid fa-folder-plus"></i> Initialize Match
                            </button>
                            <button class="admin-menu-btn" id="menu-btn-manage">
                                <i class="fa-solid fa-list-check"></i> Manage Matches
                            </button>
                        </div>
                    </div>
                </div>
                
                <!-- Admin Main View Area -->
                <div class="admin-main-panel" id="admin-view-content">
                    <!-- Views will render here dynamically -->
                </div>
            </div>
        `;
    }
    
    setupMenu() {
        const btnCreate = this.parentEl.querySelector('#menu-btn-create');
        const btnManage = this.parentEl.querySelector('#menu-btn-manage');
        
        btnCreate.addEventListener('click', () => {
            btnCreate.classList.add('active');
            btnManage.classList.remove('active');
            this.showView('create');
        });
        
        btnManage.addEventListener('click', () => {
            btnManage.classList.add('active');
            btnCreate.classList.remove('active');
            this.showView('manage');
        });
    }
    
    showView(viewName) {
        this.currentView = viewName;
        const container = this.parentEl.querySelector('#admin-view-content');
        
        if (viewName === 'manage') {
            this.renderManageView(container);
        } else {
            this.renderCreateView(container);
        }
    }
    
    // --- MANAGE MATCHES VIEW ---
    renderManageView(container) {
        container.innerHTML = `
            <div class="admin-panel-header">
                <h2>Manage Active Matches</h2>
            </div>
            <div id="admin-games-list" style="display:flex; flex-direction:column; gap:0.5rem; flex:1;">
                <div class="app-loader">
                    <div class="loader-spinner"></div>
                    <p>Loading matches...</p>
                </div>
            </div>
        `;
        
        this.loadGamesList();
    }
    
    async loadGamesList() {
        const listDiv = this.parentEl.querySelector('#admin-games-list');
        try {
            const response = await GameApi.getGames();
            const games = response.games || [];
            
            if (games.length === 0) {
                listDiv.innerHTML = `
                    <div class="no-games text-center">
                        <i class="fa-regular fa-folder-open"></i>
                        <h3>No active matches</h3>
                        <p>Use the Initialize Match tab to create a new game</p>
                    </div>
                `;
                return;
            }
            
            listDiv.innerHTML = '';
            games.forEach(game => {
                const row = document.createElement('div');
                row.className = 'admin-game-row';
                
                const startsAtDate = new Date(game.startsAt * 1000).toLocaleString();
                
                row.innerHTML = `
                    <div class="admin-game-info">
                        <div>
                            <span class="admin-game-id">${game.gameId}</span>
                            <div class="admin-game-meta">
                                ${game.map.width}x${game.map.height} map | ${game.players} players/team | Total days: ${game.totalDays}
                            </div>
                        </div>
                    </div>
                    <div>
                        <span style="font-size:0.8rem; color:var(--text-muted); margin-right:1.5rem;"><i class="fa-regular fa-clock"></i> ${startsAtDate}</span>
                        <button class="btn-danger btn-delete-game" data-game-id="${game.gameId}">
                            <i class="fa-solid fa-trash-can"></i> Delete
                        </button>
                    </div>
                `;
                
                // Delete button handler
                row.querySelector('.btn-delete-game').addEventListener('click', (e) => {
                    const gameId = e.target.getAttribute('data-game-id');
                    this.confirmDeleteGame(gameId);
                });
                
                listDiv.appendChild(row);
            });
            
        } catch (error) {
            listDiv.innerHTML = `
                <div class="no-games text-center">
                    <i class="fa-solid fa-triangle-exclamation" style="color:var(--traffic-congested)"></i>
                    <h3>Error loading games list</h3>
                    <p>${error.message}</p>
                </div>
            `;
        }
    }
    
    confirmDeleteGame(gameId) {
        const dialog = document.createElement('div');
        dialog.className = 'dialog-overlay';
        dialog.innerHTML = `
            <div class="dialog-box">
                <i class="fa-solid fa-circle-exclamation" style="font-size:3rem; color:var(--traffic-congested); margin-bottom: 1rem;"></i>
                <h3>Delete Match Config?</h3>
                <p>Are you sure you want to delete match <strong>${gameId}</strong>?<br>This action will wipe all states and standings and cannot be undone.</p>
                <div class="dialog-actions">
                    <button class="btn-card" id="dialog-cancel-btn" style="border:1px solid var(--border-color)">Cancel</button>
                    <button class="btn-danger" id="dialog-delete-btn" style="background-color:var(--traffic-congested); color:#fff">Confirm Delete</button>
                </div>
            </div>
        `;
        document.body.appendChild(dialog);
        
        dialog.querySelector('#dialog-cancel-btn').addEventListener('click', () => dialog.remove());
        dialog.querySelector('#dialog-delete-btn').addEventListener('click', async () => {
            dialog.remove();
            this.showToast(`Deleting game ${gameId}...`, 'info');
            try {
                await GameApi.deleteGame(gameId);
                this.showToast(`Game ${gameId} successfully deleted.`, 'success');
                this.loadGamesList();
            } catch (err) {
                this.showToast(`Failed to delete: ${err.message}`, 'error');
            }
        });
    }
    
    // --- CREATE MATCH WIZARD VIEW ---
    renderCreateView(container) {
        this.wizardStep = 1;
        container.innerHTML = `
            <div class="admin-panel-header">
                <h2>Initialize New Match</h2>
            </div>
            
            <!-- STEPPER PROGRESS BAR -->
            <div class="wizard-stepper">
                <div class="step-indicator active" id="step-ind-1">
                    <div class="step-number">1</div>
                    <div class="step-label">Basic Details</div>
                </div>
                <div class="step-indicator" id="step-ind-2">
                    <div class="step-number">2</div>
                    <div class="step-label">Map Design</div>
                </div>
                <div class="step-indicator" id="step-ind-3">
                    <div class="step-number">3</div>
                    <div class="step-label">Match Rules</div>
                </div>
                <div class="step-indicator" id="step-ind-4">
                    <div class="step-number">4</div>
                    <div class="step-label">Final Review</div>
                </div>
            </div>
            
            <div class="wizard-content">
                <!-- STEP 1: Basic settings -->
                <div class="wizard-step active" id="step-content-1">
                    <div style="display:grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
                        <div>
                            <div class="form-group">
                                <label for="form-game-id">Game ID (camelCase unique name)</label>
                                <input type="text" id="form-game-id" class="form-control" placeholder="e.g. tournamentMatch1">
                            </div>
                            <div class="form-group">
                                <label for="form-start-time">Match Start Time</label>
                                <input type="datetime-local" id="form-start-time" class="form-control">
                            </div>
                            <div class="form-group">
                                <label for="form-selection-limit">Agent Selection Time Limit (seconds)</label>
                                <input type="number" id="form-selection-limit" class="form-control" value="60" min="5">
                            </div>
                        </div>
                        <div>
                            <div class="form-group">
                                <label for="form-players">Players (Agents) Count per Team</label>
                                <input type="number" id="form-players" class="form-control" value="4" min="1" max="100">
                            </div>
                            <div class="form-group">
                                <label for="form-fuel-limit">Agent Fuel Limit (Liters)</label>
                                <input type="number" id="form-fuel-limit" class="form-control" value="40" min="5">
                            </div>
                            <div class="form-group">
                                <label for="form-agent-positions">Starting Agent Cell Positions (Comma-separated 1D indices)</label>
                                <input type="text" id="form-agent-positions" class="form-control" placeholder="e.g. 10, 15, 34, 45">
                                <span style="font-size:0.75rem; color:var(--text-muted);">Must input distinct non-negative indices matching coordinates width/height bounds</span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- STEP 2: Map Design panel -->
                <div class="wizard-step" id="step-content-2">
                    <div class="generator-container">
                        <!-- Left controls: input or generated map integration -->
                        <div>
                            <div class="form-group">
                                <label>Map Generator Options</label>
                                <div style="display:flex; flex-direction:column; gap:0.5rem; background:rgba(0,0,0,0.15); padding:1rem; border-radius:var(--radius-md); border:1px solid var(--border-color);">
                                    <div class="form-group" style="margin-bottom:0.6rem">
                                        <label style="font-size:0.75rem">Width (5-50)</label>
                                        <input type="number" id="gen-width" class="form-control" value="10" min="5" max="50">
                                    </div>
                                    <div class="form-group" style="margin-bottom:0.6rem">
                                        <label style="font-size:0.75rem">Height (5-50)</label>
                                        <input type="number" id="gen-height" class="form-control" value="10" min="5" max="50">
                                    </div>
                                    <div class="form-group" style="margin-bottom:0.8rem">
                                        <label style="font-size:0.75rem">Teams Count (2-10)</label>
                                        <input type="number" id="gen-teams" class="form-control" value="4" min="2" max="10">
                                    </div>
                                    <button class="btn-primary" id="btn-trigger-generate" style="width:100%; justify-content:center;">
                                        <i class="fa-solid fa-dice"></i> Generate Random Map
                                    </button>
                                </div>
                            </div>
                            
                            <div class="form-group" style="margin-top:1.5rem">
                                <label for="form-map-json">Raw Map Configuration JSON</label>
                                <textarea id="form-map-json" class="json-textarea" placeholder="Paste Map JSON here..."></textarea>
                                <button class="btn-card mt-2" id="btn-parse-json" style="width:100%;">
                                    <i class="fa-solid fa-code"></i> Parse & Load JSON Map
                                </button>
                            </div>
                        </div>
                        
                        <!-- Right Map preview -->
                        <div>
                            <label style="font-size:0.85rem; font-weight:600; color:var(--text-secondary); margin-bottom:0.5rem; display:block">Map Preview</label>
                            <div class="preview-wrapper" id="wizard-map-preview-container">
                                <div class="preview-placeholder">
                                    <i class="fa-solid fa-map-location-dot"></i>
                                    <p>No map loaded. Generate a random map or import custom map JSON to view preview here.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- STEP 3: Match Rules -->
                <div class="wizard-step" id="step-content-3">
                    <div style="display:grid; grid-template-columns: 1fr 1.5fr; gap:1.5rem;">
                        <div>
                            <div class="form-group">
                                <label for="form-busy-thresh">Traffic BUSY Threshold (Density factor)</label>
                                <input type="number" step="0.1" id="form-busy-thresh" class="form-control" value="2.0" min="0.1">
                            </div>
                            <div class="form-group">
                                <label for="form-jammed-thresh">Traffic CONGESTED Threshold (Density factor)</label>
                                <input type="number" step="0.1" id="form-jammed-thresh" class="form-control" value="4.0" min="0.2">
                                <span style="font-size:0.75rem; color:var(--text-muted)">Must be strictly greater than busy threshold</span>
                            </div>
                        </div>
                        <div>
                            <label style="font-size:0.85rem; font-weight:600; color:var(--text-secondary); margin-bottom:0.5rem; display:block">Tournament Days Config</label>
                            <div style="background-color:rgba(0,0,0,0.15); border:1px solid var(--border-color); padding:1rem; border-radius:var(--radius-md);">
                                <table class="scoreboard-table" id="days-table" style="margin-bottom:1rem">
                                    <thead>
                                        <tr>
                                            <th>Day</th>
                                            <th>Max Duration (s)</th>
                                            <th>Max Steps limit</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody id="days-tbody">
                                        <!-- Dynamic day rows -->
                                    </tbody>
                                </table>
                                <button class="btn-card" id="btn-add-day">
                                    <i class="fa-solid fa-plus"></i> Add Competition Day
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- STEP 4: Review Summary -->
                <div class="wizard-step" id="step-content-4">
                    <div class="review-grid">
                        <div class="review-section">
                            <h4>Game Details</h4>
                            <div class="review-item"><span class="review-lbl">Game ID:</span><span class="review-val" id="rev-game-id">--</span></div>
                            <div class="review-item"><span class="review-lbl">Starts At:</span><span class="review-val" id="rev-starts-at">--</span></div>
                            <div class="review-item"><span class="review-lbl">Selection Limit:</span><span class="review-val" id="rev-selection-limit">--</span></div>
                            <div class="review-item"><span class="review-lbl">Players/Team:</span><span class="review-val" id="rev-players">--</span></div>
                            <div class="review-item"><span class="review-lbl">Fuel limits:</span><span class="review-val" id="rev-fuel">--</span></div>
                            <div class="review-item"><span class="review-lbl">Starting Positions:</span><span class="review-val" id="rev-agent-pos">--</span></div>
                        </div>
                        <div class="review-section">
                            <h4>Map & Regulations</h4>
                            <div class="review-item"><span class="review-lbl">Map Dimensions:</span><span class="review-val" id="rev-map-dim">--</span></div>
                            <div class="review-item"><span class="review-lbl">Udon Spots Count:</span><span class="review-val" id="rev-spots">--</span></div>
                            <div class="review-item"><span class="review-lbl">Busy Threshold:</span><span class="review-val" id="rev-busy">--</span></div>
                            <div class="review-item"><span class="review-lbl">Congested Threshold:</span><span class="review-val" id="rev-jammed">--</span></div>
                            <div class="review-item"><span class="review-lbl">Total Days count:</span><span class="review-val" id="rev-days">--</span></div>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- WIZARD NAV CONTROLS -->
            <div class="wizard-actions">
                <button class="btn-card" id="wizard-prev-btn" style="visibility:hidden">
                    <i class="fa-solid fa-arrow-left"></i> Previous Step
                </button>
                <button class="btn-primary" id="wizard-next-btn">
                    Next Step <i class="fa-solid fa-arrow-right"></i>
                </button>
            </div>
        `;
        
        this.initWizardEvents();
    }
    
    initWizardEvents() {
        const ind1 = this.parentEl.querySelector('#step-ind-1');
        const ind2 = this.parentEl.querySelector('#step-ind-2');
        const ind3 = this.parentEl.querySelector('#step-ind-3');
        const ind4 = this.parentEl.querySelector('#step-ind-4');
        
        const nextBtn = this.parentEl.querySelector('#wizard-next-btn');
        const prevBtn = this.parentEl.querySelector('#wizard-prev-btn');
        
        // Auto datetime input setup
        const dateInput = this.parentEl.querySelector('#form-start-time');
        const defaultDate = new Date(Date.now() + 60 * 60 * 1000); // 1 hour later
        dateInput.value = new Date(defaultDate.getTime() - defaultDate.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
        
        // Setup table rows for day configurations
        this.renderDaysRows();
        
        // Add Day button handler
        this.parentEl.querySelector('#btn-add-day').addEventListener('click', () => {
            const count = this.wizardData.daySeconds.length;
            this.wizardData.daySeconds.push(60.0);
            this.wizardData.daySteps.push(100);
            this.renderDaysRows();
        });
        
        // Map generator click
        this.parentEl.querySelector('#btn-trigger-generate').addEventListener('click', async () => {
            const w = parseInt(this.parentEl.querySelector('#gen-width').value);
            const h = parseInt(this.parentEl.querySelector('#gen-height').value);
            const t = parseInt(this.parentEl.querySelector('#gen-teams').value);
            
            if (isNaN(w) || w < 5 || w > 50 || isNaN(h) || h < 5 || h > 50 || isNaN(t) || t < 2 || t > 10) {
                this.showToast('Please verify map generator parameters conform to boundaries.', 'error');
                return;
            }
            
            this.showToast('Generating random map layout...', 'info');
            try {
                const mapData = await GameApi.generateMap(w, h, t);
                this.generatedMap = mapData;
                
                // Show JSON config in textarea
                this.parentEl.querySelector('#form-map-json').value = JSON.stringify(mapData, null, 2);
                
                // Render preview HexMap
                this.renderWizardMapPreview(mapData);
                this.showToast('Map configuration successfully generated.', 'success');
                
                // Suggest starter agent positions
                this.suggestStartingPositions(mapData);
            } catch (err) {
                this.showToast(`Generation failed: ${err.message}`, 'error');
            }
        });
        
        // Parse custom JSON map click
        this.parentEl.querySelector('#btn-parse-json').addEventListener('click', () => {
            const raw = this.parentEl.querySelector('#form-map-json').value;
            try {
                const parsed = JSON.parse(raw);
                if (!parsed.width || !parsed.height || !parsed.cells) {
                    throw new Error('JSON map configuration is missing width, height, or cells.');
                }
                this.generatedMap = parsed;
                this.renderWizardMapPreview(parsed);
                this.showToast('Custom JSON Map parsed and loaded successfully.', 'success');
                this.suggestStartingPositions(parsed);
            } catch (err) {
                this.showToast(`JSON Parse Error: ${err.message}`, 'error');
            }
        });
        
        // Prev and Next Step Wizard Click handlers
        prevBtn.addEventListener('click', () => {
            if (this.wizardStep > 1) {
                this.changeWizardStep(this.wizardStep - 1);
            }
        });
        
        nextBtn.addEventListener('click', async () => {
            if (this.wizardStep === 4) {
                // Submit initialization
                await this.submitMatchConfiguration();
                return;
            }
            
            if (this.validateWizardStep(this.wizardStep)) {
                this.changeWizardStep(this.wizardStep + 1);
            }
        });
    }
    
    suggestStartingPositions(mapData) {
        // Collect first few walkable plain/road positions
        const list = [];
        const width = mapData.width;
        const height = mapData.height;
        const cells = mapData.cells;
        
        let found = 0;
        const targetCount = parseInt(this.parentEl.querySelector('#form-players').value) || 4;
        
        for (let r = 0; r < height; r++) {
            for (let c = 0; c < width; c++) {
                const terrain = cells[r][c];
                const posIndex = r * width + c;
                
                // Check if plain or road and spot is not present
                const spotFound = (mapData.spots || []).some(s => s.pos === posIndex);
                if ((terrain === 0 || terrain === 1) && !spotFound) {
                    list.push(posIndex);
                    found++;
                    if (found >= targetCount) break;
                }
            }
            if (found >= targetCount) break;
        }
        
        this.parentEl.querySelector('#form-agent-positions').value = list.join(', ');
    }
    
    renderDaysRows() {
        const tbody = this.parentEl.querySelector('#days-tbody');
        tbody.innerHTML = '';
        
        this.wizardData.daySeconds.forEach((sec, idx) => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td style="font-weight:700">#${idx + 1}</td>
                <td><input type="number" class="form-control form-day-sec" data-idx="${idx}" value="${sec}" min="1" style="padding:0.4rem; width:100px;"></td>
                <td><input type="number" class="form-control form-day-step" data-idx="${idx}" value="${this.wizardData.daySteps[idx]}" min="10" style="padding:0.4rem; width:120px;"></td>
                <td>
                    <button class="btn-danger btn-remove-day" data-idx="${idx}" ${this.wizardData.daySeconds.length <= 1 ? 'disabled style="opacity:0.5"' : ''}>
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </td>
            `;
            
            // Event hooks
            tr.querySelector('.form-day-sec').addEventListener('input', (e) => {
                const val = parseFloat(e.target.value);
                if (!isNaN(val)) this.wizardData.daySeconds[idx] = val;
            });
            
            tr.querySelector('.form-day-step').addEventListener('input', (e) => {
                const val = parseInt(e.target.value);
                if (!isNaN(val)) this.wizardData.daySteps[idx] = val;
            });
            
            tr.querySelector('.btn-remove-day').addEventListener('click', () => {
                this.wizardData.daySeconds.splice(idx, 1);
                this.wizardData.daySteps.splice(idx, 1);
                this.renderDaysRows();
            });
            
            tbody.appendChild(tr);
        });
    }
    
    renderWizardMapPreview(mapData) {
        const previewContainer = this.parentEl.querySelector('#wizard-map-preview-container');
        // Wrap coordinates config inside GameBoard DTO schema for map compatibility
        const boardDTO = {
            map: {
                width: mapData.width,
                height: mapData.height,
                cells: mapData.cells
            },
            spots: mapData.spots || [],
            fuelLimits: 100,
            players: 1
        };
        this.wizardMapInstance = new HexMap(previewContainer, boardDTO);
    }
    
    changeWizardStep(nextStep) {
        const ind1 = this.parentEl.querySelector('#step-ind-1');
        const ind2 = this.parentEl.querySelector('#step-ind-2');
        const ind3 = this.parentEl.querySelector('#step-ind-3');
        const ind4 = this.parentEl.querySelector('#step-ind-4');
        
        const nextBtn = this.parentEl.querySelector('#wizard-next-btn');
        const prevBtn = this.parentEl.querySelector('#wizard-prev-btn');
        
        // Hide all contents
        this.parentEl.querySelectorAll('.wizard-step').forEach(el => el.classList.remove('active'));
        
        // Show target step contents
        this.parentEl.querySelector(`#step-content-${nextStep}`).classList.add('active');
        
        // Update stepper indicator colors
        const indicators = [ind1, ind2, ind3, ind4];
        indicators.forEach((ind, index) => {
            const stepNum = index + 1;
            ind.classList.remove('active', 'completed');
            if (stepNum === nextStep) {
                ind.classList.add('active');
            } else if (stepNum < nextStep) {
                ind.classList.add('completed');
            }
        });
        
        // Update control button labels & visibility
        this.wizardStep = nextStep;
        
        if (nextStep === 1) {
            prevBtn.style.visibility = 'hidden';
        } else {
            prevBtn.style.visibility = 'visible';
        }
        
        if (nextStep === 4) {
            nextBtn.innerHTML = '<i class="fa-solid fa-circle-check"></i> Initialize Game';
            nextBtn.style.backgroundColor = 'var(--traffic-normal)';
            this.prepareWizardReviewSummary();
        } else {
            nextBtn.innerHTML = 'Next Step <i class="fa-solid fa-arrow-right"></i>';
            nextBtn.style.backgroundColor = '';
        }
    }
    
    validateWizardStep(step) {
        if (step === 1) {
            const gameId = this.parentEl.querySelector('#form-game-id').value.trim();
            const startsAtVal = this.parentEl.querySelector('#form-start-time').value;
            const selectLimit = parseFloat(this.parentEl.querySelector('#form-selection-limit').value);
            const playersVal = parseInt(this.parentEl.querySelector('#form-players').value);
            const fuelVal = parseInt(this.parentEl.querySelector('#form-fuel-limit').value);
            const rawPos = this.parentEl.querySelector('#form-agent-positions').value.trim();
            
            if (!gameId) {
                this.showToast('Please enter a valid Game ID.', 'error');
                return false;
            }
            if (!startsAtVal) {
                this.showToast('Please select a start time date.', 'error');
                return false;
            }
            if (isNaN(selectLimit) || selectLimit <= 0) {
                this.showToast('Agent selection time limit must be greater than 0.', 'error');
                return false;
            }
            if (isNaN(playersVal) || playersVal <= 0) {
                this.showToast('Player count per team must be greater than 0.', 'error');
                return false;
            }
            if (isNaN(fuelVal) || fuelVal <= 0) {
                this.showToast('Fuel limits must be greater than 0.', 'error');
                return false;
            }
            
            // Validate agents positions array
            if (!rawPos) {
                this.showToast('Please enter initial agent positions.', 'error');
                return false;
            }
            
            const posArray = rawPos.split(',').map(s => parseInt(s.trim()));
            if (posArray.some(isNaN) || posArray.some(p => p < 0)) {
                this.showToast('Agent positions must be non-negative integers.', 'error');
                return false;
            }
            
            // Check duplicates in agent positions
            const uniquePos = [...new Set(posArray)];
            if (uniquePos.length !== posArray.length) {
                this.showToast('Duplicate positions are not allowed in starting agent positions.', 'error');
                return false;
            }
            
            // Save state
            this.wizardData.gameId = gameId;
            this.wizardData.startsAt = Math.floor(new Date(startsAtVal).getTime() / 1000);
            this.wizardData.agentSelectionTimeLimit = selectLimit;
            this.wizardData.players = playersVal;
            this.wizardData.fuelLimits = fuelVal;
            this.wizardData.agents = posArray;
            return true;
        }
        
        if (step === 2) {
            if (!this.generatedMap) {
                this.showToast('Please generate a map or parse a valid JSON map config.', 'error');
                return false;
            }
            
            // Verify agents starting coordinates fit inside bounds
            const bounds = this.generatedMap.width * this.generatedMap.height;
            const invalidIdx = this.wizardData.agents.find(p => p >= bounds);
            if (invalidIdx !== undefined) {
                this.showToast(`Agent starting cell index ${invalidIdx} exceeds map limits of ${bounds - 1}.`, 'error');
                return false;
            }
            
            this.wizardData.map = this.generatedMap;
            return true;
        }
        
        if (step === 3) {
            const busy = parseFloat(this.parentEl.querySelector('#form-busy-thresh').value);
            const jammed = parseFloat(this.parentEl.querySelector('#form-jammed-thresh').value);
            
            if (isNaN(busy) || busy <= 0) {
                this.showToast('BUSY traffic threshold must be greater than 0.', 'error');
                return false;
            }
            if (isNaN(jammed) || jammed <= busy) {
                this.showToast('CONGESTED threshold must be strictly greater than BUSY threshold.', 'error');
                return false;
            }
            
            this.wizardData.busyThreshold = busy;
            this.wizardData.jammedThreshold = jammed;
            return true;
        }
        
        return true;
    }
    
    prepareWizardReviewSummary() {
        this.parentEl.querySelector('#rev-game-id').textContent = this.wizardData.gameId;
        this.parentEl.querySelector('#rev-starts-at').textContent = new Date(this.wizardData.startsAt * 1000).toLocaleString();
        this.parentEl.querySelector('#rev-selection-limit').textContent = `${this.wizardData.agentSelectionTimeLimit}s`;
        this.parentEl.querySelector('#rev-players').textContent = this.wizardData.players;
        this.parentEl.querySelector('#rev-fuel').textContent = `${this.wizardData.fuelLimits} Liters`;
        this.parentEl.querySelector('#rev-agent-pos').textContent = this.wizardData.agents.join(', ');
        
        this.parentEl.querySelector('#rev-map-dim').textContent = `${this.wizardData.map.width}x${this.wizardData.map.height} grid`;
        this.parentEl.querySelector('#rev-spots').textContent = this.wizardData.map.spots?.length || 0;
        this.parentEl.querySelector('#rev-busy').textContent = this.wizardData.busyThreshold;
        this.parentEl.querySelector('#rev-jammed').textContent = this.wizardData.jammedThreshold;
        this.parentEl.querySelector('#rev-days').textContent = `${this.wizardData.daySeconds.length} days`;
    }
    
    async submitMatchConfiguration() {
        // Construct the DTO request body exactly matching InitGameRequest.java
        const payload = {
            gameId: this.wizardData.gameId,
            startsAt: this.wizardData.startsAt,
            agentSelectionTimeLimit: this.wizardData.agentSelectionTimeLimit,
            daySeconds: this.wizardData.daySeconds,
            daySteps: this.wizardData.daySteps,
            map: {
                width: this.wizardData.map.width,
                height: this.wizardData.map.height,
                cells: this.wizardData.map.cells,
                spots: this.wizardData.map.spots || []
            },
            fuelLimits: this.wizardData.fuelLimits,
            players: this.wizardData.players,
            busyThreshold: this.wizardData.busyThreshold,
            jammedThreshold: this.wizardData.jammedThreshold,
            agents: this.wizardData.agents
        };
        
        this.showToast('Submitting match configuration settings...', 'info');
        try {
            await GameApi.initGame(payload);
            this.showToast('Game created successfully!', 'success');
            
            // Redirect back to lobby after creation
            this.router.navigate('/games');
        } catch (err) {
            console.error('Initialization failed:', err);
            this.showToast(`Initialization failed: ${err.message || 'Check validation constraints.'}`, 'error');
            
            // If it has specific validation errors array
            if (err.errors && err.errors.length > 0) {
                err.errors.forEach(subErr => {
                    this.showToast(`${subErr.field}: ${subErr.message}`, 'error');
                });
            }
        }
    }
}
