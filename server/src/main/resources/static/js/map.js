// HEXUDON Monitor SVG Hexagonal Map Renderer

const HEX_SIZE = 35; // Radius of hex cells (pixels)
const HEX_WIDTH = Math.sqrt(3) * HEX_SIZE;
const HEX_HEIGHT = 2 * HEX_SIZE;
const COL_SPACING = HEX_WIDTH;
const ROW_SPACING = HEX_HEIGHT * 0.75;

export class HexMap {
    /**
     * @param {HTMLElement} containerEl - Wrapper div for the SVG map
     * @param {Object} boardConfig - Static game board configurations
     * @param {Function} onCellSelect - Callback when cell is clicked
     */
    constructor(containerEl, boardConfig, onCellSelect = null) {
        this.containerEl = containerEl;
        this.boardConfig = boardConfig;
        this.onCellSelect = onCellSelect;
        
        this.width = boardConfig.map.width;
        this.height = boardConfig.map.height;
        this.cells = boardConfig.map.cells;
        this.spots = boardConfig.spots || [];
        
        // Pan & Zoom state
        this.scale = 1.0;
        this.panX = 50;
        this.panY = 50;
        this.isDragging = false;
        this.startX = 0;
        this.startY = 0;
        
        // Selection state
        this.selectedCellIndex = null;
        
        // DOM Elements
        this.svgEl = null;
        this.gContainer = null;
        
        // Cache cell coordinates
        this.cellCoords = [];
        this.initCellCoords();
        
        // Setup SVG
        this.createSvg();
        this.setupEvents();
    }
    
    initCellCoords() {
        for (let idx = 0; idx < this.width * this.height; idx++) {
            const x = idx % this.width;
            const y = Math.floor(idx / this.width);
            
            // odd-r layout: shift odd rows to the right by w / 2
            const cx = x * COL_SPACING + (y % 2 !== 0 ? COL_SPACING / 2 : 0);
            const cy = y * ROW_SPACING;
            
            this.cellCoords[idx] = { cx, cy, x, y };
        }
    }
    
    getHexPoints(cx, cy, r) {
        const points = [];
        for (let i = 0; i < 6; i++) {
            const angleRad = (Math.PI / 180) * (60 * i + 30);
            const px = cx + r * Math.cos(angleRad);
            const py = cy + r * Math.sin(angleRad);
            points.push(`${px.toFixed(1)},${py.toFixed(1)}`);
        }
        return points.join(' ');
    }
    
    createSvg() {
        this.containerEl.innerHTML = '';
        
        // Create tooltip element if not existing
        let tooltip = this.containerEl.querySelector('.cell-info-tooltip');
        if (!tooltip) {
            tooltip = document.createElement('div');
            tooltip.className = 'cell-info-tooltip';
            this.containerEl.appendChild(tooltip);
        }
        this.tooltipEl = tooltip;
        
        // Create SVG
        this.svgEl = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        this.svgEl.id = 'map-svg';
        
        // Main transform group for Pan/Zoom
        this.gContainer = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        this.gContainer.setAttribute('class', 'map-pan-zoom-container');
        this.updateTransform();
        
        // Groups for layering elements correctly
        this.gCells = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        this.gCells.setAttribute('id', 'layer-cells');
        
        this.gSpots = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        this.gSpots.setAttribute('id', 'layer-spots');
        
        this.gAgents = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        this.gAgents.setAttribute('id', 'layer-agents');
        
        this.gContainer.appendChild(this.gCells);
        this.gContainer.appendChild(this.gSpots);
        this.gContainer.appendChild(this.gAgents);
        this.svgEl.appendChild(this.gContainer);
        this.containerEl.appendChild(this.svgEl);
        
        this.renderMap();
        this.centerMap();
    }
    
    renderMap() {
        this.gCells.innerHTML = '';
        this.gSpots.innerHTML = '';
        
        // 1. Render cells
        for (let idx = 0; idx < this.width * this.height; idx++) {
            const coord = this.cellCoords[idx];
            const terrainVal = this.cells[coord.y][coord.x];
            
            // Map Terrain enum: 0-Plain, 1-Road, 2-Mountain, 3-Pond
            let terrainClass = 'terrain-plain';
            let terrainName = 'Plain';
            if (terrainVal === 1) { terrainClass = 'terrain-road'; terrainName = 'Road'; }
            else if (terrainVal === 2) { terrainClass = 'terrain-mountain'; terrainName = 'Mountain'; }
            else if (terrainVal === 3) { terrainClass = 'terrain-pond'; terrainName = 'Pond'; }
            
            const poly = document.createElementNS('http://www.w3.org/2000/svg', 'polygon');
            poly.setAttribute('points', this.getHexPoints(coord.cx, coord.cy, HEX_SIZE - 1.5));
            poly.setAttribute('class', `hex-cell ${terrainClass}`);
            poly.setAttribute('data-index', idx);
            poly.setAttribute('data-x', coord.x);
            poly.setAttribute('data-y', coord.y);
            poly.setAttribute('data-terrain', terrainName);
            
            // Event hooks
            poly.addEventListener('mouseenter', (e) => this.showTooltip(e, idx));
            poly.addEventListener('mouseleave', () => this.hideTooltip());
            poly.addEventListener('mousemove', (e) => this.moveTooltip(e));
            poly.addEventListener('click', () => {
                this.selectedCellIndex = idx;
                if (this.onCellSelect) {
                    this.onCellSelect(idx, coord.x, coord.y, terrainVal);
                }
            });
            
            this.gCells.appendChild(poly);
        }
        
        // 2. Render spots (Udon stores)
        this.spots.forEach(spot => {
            const coord = this.cellCoords[spot.pos];
            if (!coord) return;
            
            const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            g.setAttribute('class', 'spot-marker');
            g.setAttribute('id', `spot-${spot.pos}`);
            
            // Render diamond store shape
            const rect = document.createElementNS('http://www.w3.org/2000/svg', 'polygon');
            const halfW = 12;
            const pts = [
                `${coord.cx},${coord.cy - halfW - 2}`,
                `${coord.cx + halfW + 2},${coord.cy}`,
                `${coord.cx},${coord.cy + halfW + 2}`,
                `${coord.cx - halfW - 2},${coord.cy}`
            ].join(' ');
            rect.setAttribute('points', pts);
            rect.setAttribute('class', 'spot-bg');
            
            // Store text label representing brand number
            const txt = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            txt.setAttribute('x', coord.cx);
            txt.setAttribute('y', coord.cy);
            txt.setAttribute('class', 'spot-text');
            txt.textContent = `U${spot.brand}`;
            
            // Stocks counter badge
            const badge = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            badge.setAttribute('cx', coord.cx + 10);
            badge.setAttribute('cy', coord.cy - 10);
            badge.setAttribute('r', 7);
            badge.setAttribute('class', 'spot-stock-badge');
            
            const badgeTxt = document.createElementNS('http://www.w3.org/2000/svg', 'text');
            badgeTxt.setAttribute('x', coord.cx + 10);
            badgeTxt.setAttribute('y', coord.cy - 10);
            badgeTxt.setAttribute('class', 'spot-stock-text');
            badgeTxt.textContent = spot.stocks;
            
            g.appendChild(rect);
            g.appendChild(txt);
            g.appendChild(badge);
            g.appendChild(badgeTxt);
            
            this.gSpots.appendChild(g);
        });
    }
    
    /**
     * Updates road traffic density
     * @param {Array} mapStatus - List of {pos, status} objects
     */
    updateTraffic(mapStatus = []) {
        if (!mapStatus) return;
        
        // Reset all road cells status classes
        const cellPolys = this.gCells.querySelectorAll('.hex-cell');
        cellPolys.forEach(p => {
            p.classList.remove('traffic-normal', 'traffic-busy', 'traffic-congested');
        });
        
        mapStatus.forEach(item => {
            const poly = this.gCells.querySelector(`polygon[data-index="${item.pos}"]`);
            if (poly) {
                // Remove terrain defaults styling, apply traffic density glows
                let trafficClass = 'traffic-normal';
                if (item.status === 1) trafficClass = 'traffic-busy';
                else if (item.status === 2) trafficClass = 'traffic-congested';
                
                poly.classList.add(trafficClass);
            }
        });
    }
    
    /**
     * Updates agents visual positions and fuel rings
     * @param {Array} teams - List of team status objects containing agents
     */
    updateAgents(teams = []) {
        this.gAgents.innerHTML = '';
        if (!teams) return;
        
        let teamIndex = 1;
        teams.forEach(team => {
            const teamColorVar = `var(--team-${teamIndex > 10 ? 10 : teamIndex})`;
            const agents = team.agents || [];
            
            agents.forEach((agent, index) => {
                const coord = this.cellCoords[agent.pos];
                if (!coord) return;
                
                const agentId = `${team.teamId}-a${index}`;
                
                const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
                g.setAttribute('class', 'agent-token');
                g.setAttribute('id', `agent-token-${agentId}`);
                g.setAttribute('transform', `translate(${coord.cx}, ${coord.cy})`);
                
                // Outer circle ring representing fuel
                const fuelRadius = 14;
                const fuelLimit = this.boardConfig.fuelLimits || 100;
                
                // Draw backing glow circle
                const glowCircle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                glowCircle.setAttribute('cx', 0);
                glowCircle.setAttribute('cy', 0);
                glowCircle.setAttribute('r', 11);
                glowCircle.setAttribute('fill', teamColorVar);
                glowCircle.setAttribute('opacity', 0.25);
                glowCircle.setAttribute('class', 'agent-glow-bg');
                
                // Agent center circle
                const dot = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                dot.setAttribute('cx', 0);
                dot.setAttribute('cy', 0);
                dot.setAttribute('r', 10);
                dot.setAttribute('fill', teamColorVar);
                dot.setAttribute('class', 'agent-dot');
                
                // Text overlay (kind index: P = patrol, R = refuel)
                const txt = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                txt.setAttribute('x', 0);
                txt.setAttribute('y', 0);
                txt.setAttribute('class', 'agent-text');
                txt.textContent = agent.kind === 0 ? 'P' : 'R';
                
                // Fuel Ring Indicator (only for PatrolAgent, fuel > 0)
                if (agent.kind === 0) {
                    const fuelPercentage = Math.max(0, Math.min(100, (agent.fuel / fuelLimit) * 100));
                    const ring = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                    ring.setAttribute('cx', 0);
                    ring.setAttribute('cy', 0);
                    ring.setAttribute('r', fuelRadius);
                    ring.setAttribute('class', 'agent-fuel-ring');
                    
                    // Arc math
                    const circ = 2 * Math.PI * fuelRadius;
                    const strokeDashOffset = circ - (fuelPercentage / 100) * circ;
                    
                    ring.setAttribute('stroke-dasharray', circ);
                    ring.setAttribute('stroke-dashoffset', strokeDashOffset);
                    ring.setAttribute('stroke', fuelPercentage < 25 ? 'var(--traffic-congested)' : 'var(--traffic-normal)');
                    
                    g.appendChild(ring);
                }
                
                g.appendChild(glowCircle);
                g.appendChild(dot);
                g.appendChild(txt);
                
                // Agent tooltip triggers
                g.addEventListener('mouseenter', (e) => {
                    const kindName = agent.kind === 0 ? 'Patrol Agent' : 'Refuel Agent';
                    this.tooltipEl.style.display = 'block';
                    this.tooltipEl.innerHTML = `
                        <div class="tooltip-row"><span class="tooltip-lbl">Team ID:</span><span class="tooltip-val" style="color:${teamColorVar}">${team.teamId}</span></div>
                        <div class="tooltip-row"><span class="tooltip-lbl">Agent ID:</span><span class="tooltip-val">${index}</span></div>
                        <div class="tooltip-row"><span class="tooltip-lbl">Role:</span><span class="tooltip-val">${kindName}</span></div>
                        <div class="tooltip-row"><span class="tooltip-lbl">Cell index:</span><span class="tooltip-val">${agent.pos}</span></div>
                        <div class="tooltip-row"><span class="tooltip-lbl">Fuel remaining:</span><span class="tooltip-val">${agent.kind === 0 ? agent.fuel : 'N/A'}</span></div>
                    `;
                });
                g.addEventListener('mouseleave', () => this.hideTooltip());
                
                this.gAgents.appendChild(g);
            });
            teamIndex++;
        });
    }
    
    /**
     * Updates specific spots stocks realtime
     * @param {Object} teamStocks - Mapping of cell_pos -> stocks (if state exposes stocks)
     */
    updateStocks(teamStocks = {}) {
        // Option to dynamically reflect team stock views
    }
    
    // TOOLTIP CONTROLS
    showTooltip(e, index) {
        const coord = this.cellCoords[index];
        const terrainVal = this.cells[coord.y][coord.x];
        
        let terrainName = 'Plain';
        let costVal = '1 Fuel, 2 Steps';
        if (terrainVal === 1) { terrainName = 'Road'; costVal = '2 Fuel, 1 Step'; }
        else if (terrainVal === 2) { terrainName = 'Mountain'; costVal = '2 Fuel, 3 Steps'; }
        else if (terrainVal === 3) { terrainName = 'Pond'; costVal = 'Impassable'; }
        
        // Spot details on this cell?
        const spot = this.spots.find(s => s.pos === index);
        const spotDetails = spot ? `<div class="tooltip-row"><span class="tooltip-lbl">Udon Shop:</span><span class="tooltip-val" style="color:#ff9f43">Brand ${spot.brand} (Stock: ${spot.stocks})</span></div>` : '';
        
        this.tooltipEl.style.display = 'block';
        this.tooltipEl.innerHTML = `
            <div class="tooltip-row"><span class="tooltip-lbl">Index:</span><span class="tooltip-val">${index}</span></div>
            <div class="tooltip-row"><span class="tooltip-lbl">Coordinate:</span><span class="tooltip-val">(${coord.x}, ${coord.y})</span></div>
            <div class="tooltip-row"><span class="tooltip-lbl">Terrain:</span><span class="tooltip-val">${terrainName}</span></div>
            <div class="tooltip-row"><span class="tooltip-lbl">Cost:</span><span class="tooltip-val">${costVal}</span></div>
            ${spotDetails}
        `;
    }
    
    hideTooltip() {
        this.tooltipEl.style.display = 'none';
    }
    
    moveTooltip(e) {
        const bounds = this.containerEl.getBoundingClientRect();
        const tooltipW = this.tooltipEl.offsetWidth;
        const tooltipH = this.tooltipEl.offsetHeight;
        
        let left = e.clientX - bounds.left + 15;
        let top = e.clientY - bounds.top + 15;
        
        // boundary adjustments
        if (left + tooltipW > bounds.width) {
            left = e.clientX - bounds.left - tooltipW - 15;
        }
        if (top + tooltipH > bounds.height) {
            top = e.clientY - bounds.top - tooltipH - 15;
        }
        
        this.tooltipEl.style.left = `${left}px`;
        this.tooltipEl.style.top = `${top}px`;
    }
    
    // PAN AND ZOOM EVENT LISTENERS
    setupEvents() {
        // Drag events
        this.containerEl.addEventListener('mousedown', (e) => {
            if (e.target.closest('.map-controls-overlay')) return;
            this.isDragging = true;
            this.startX = e.clientX - this.panX;
            this.startY = e.clientY - this.panY;
            this.containerEl.style.cursor = 'grabbing';
        });
        
        window.addEventListener('mousemove', (e) => {
            if (!this.isDragging) return;
            this.panX = e.clientX - this.startX;
            this.panY = e.clientY - this.startY;
            this.updateTransform();
        });
        
        window.addEventListener('mouseup', () => {
            if (this.isDragging) {
                this.isDragging = false;
                this.containerEl.style.cursor = 'grab';
            }
        });
        
        // Mousewheel zoom
        this.containerEl.addEventListener('wheel', (e) => {
            e.preventDefault();
            const zoomIntensity = 0.08;
            
            // Get mouse position relative to container
            const bounds = this.containerEl.getBoundingClientRect();
            const mouseX = e.clientX - bounds.left;
            const mouseY = e.clientY - bounds.top;
            
            // Math for zooming relative to cursor position
            const xs = (mouseX - this.panX) / this.scale;
            const ys = (mouseY - this.panY) / this.scale;
            
            if (e.deltaY < 0) {
                this.scale = Math.min(2.5, this.scale + zoomIntensity);
            } else {
                this.scale = Math.max(0.3, this.scale - zoomIntensity);
            }
            
            this.panX = mouseX - xs * this.scale;
            this.panY = mouseY - ys * this.scale;
            
            this.updateTransform();
        });
    }
    
    zoomIn() {
        this.scale = Math.min(2.5, this.scale + 0.2);
        this.updateTransform();
    }
    
    zoomOut() {
        this.scale = Math.max(0.3, this.scale - 0.2);
        this.updateTransform();
    }
    
    centerMap() {
        const bounds = this.containerEl.getBoundingClientRect();
        
        // Compute bounding box of hex coordinates
        let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
        this.cellCoords.forEach(c => {
            if (c.cx < minX) minX = c.cx;
            if (c.cx > maxX) maxX = c.cx;
            if (c.cy < minY) minY = c.cy;
            if (c.cy > maxY) maxY = c.cy;
        });
        
        const mapW = (maxX - minX) + HEX_WIDTH;
        const mapH = (maxY - minY) + HEX_HEIGHT;
        
        // Adjust zoom scale to fit
        const scaleX = (bounds.width - 80) / mapW;
        const scaleY = (bounds.height - 80) / mapH;
        this.scale = Math.min(1.2, Math.max(0.4, Math.min(scaleX, scaleY)));
        
        // Center the camera offsets
        this.panX = (bounds.width - mapW * this.scale) / 2 - minX * this.scale + (HEX_WIDTH * this.scale) / 4;
        this.panY = (bounds.height - mapH * this.scale) / 2 - minY * this.scale + (HEX_HEIGHT * this.scale) / 4;
        
        this.updateTransform();
    }
    
    updateTransform() {
        if (this.gContainer) {
            this.gContainer.setAttribute('transform', `translate(${this.panX}, ${this.panY}) scale(${this.scale})`);
        }
    }
}
