import { hexToPixel, getHexagonPoints, getNeighbors, isAdjacent } from "../utils/hex_utils.js";
import { Icons } from "../assets/icons.js";
import { gameState } from "../state/game_state.js";
import { logger } from "../logger/logger.js";

export class MapRenderer {
  constructor(svgElement, tooltipElement) {
    this.svg = svgElement;
    this.tooltip = tooltipElement;
    this.g = svgElement.querySelector("g") || document.createElementNS("http://www.w3.org/2000/svg", "g");
    if (!this.svg.contains(this.g)) {
      this.svg.appendChild(this.g);
    }

    this.hexSize = 35; // Hex size in pixels (radius)
    this.panX = 50;
    this.panY = 50;
    this.zoom = 1.0;
    this.isDragging = false;
    this.startX = 0;
    this.startY = 0;

    this.initEvents();
  }

  initEvents() {
    // Mouse dragging for pan
    this.svg.addEventListener("mousedown", (e) => {
      if (e.button === 0) { // Left click
        this.isDragging = true;
        this.startX = e.clientX - this.panX;
        this.startY = e.clientY - this.panY;
        this.svg.style.cursor = "grabbing";
      }
    });

    window.addEventListener("mousemove", (e) => {
      if (this.isDragging) {
        this.panX = e.clientX - this.startX;
        this.panY = e.clientY - this.startY;
        this.applyTransform();
      }
    });

    window.addEventListener("mouseup", () => {
      if (this.isDragging) {
        this.isDragging = false;
        this.svg.style.cursor = "grab";
      }
    });

    // Mouse wheel for zoom
    this.svg.addEventListener("wheel", (e) => {
      e.preventDefault();
      const zoomFactor = 1.1;
      const mouseX = e.clientX - this.svg.getBoundingClientRect().left;
      const mouseY = e.clientY - this.svg.getBoundingClientRect().top;
      
      // Calculate coordinates before zoom
      const beforeZoomX = (mouseX - this.panX) / this.zoom;
      const beforeZoomY = (mouseY - this.panY) / this.zoom;

      if (e.deltaY < 0) {
        this.zoom = Math.min(this.zoom * zoomFactor, 3.0);
      } else {
        this.zoom = Math.max(this.zoom / zoomFactor, 0.4);
      }

      // Adjust pan to zoom into cursor
      this.panX = mouseX - beforeZoomX * this.zoom;
      this.panY = mouseY - beforeZoomY * this.zoom;

      this.applyTransform();
    });

    this.svg.style.cursor = "grab";
  }

  applyTransform() {
    this.g.setAttribute("transform", `translate(${this.panX}, ${this.panY}) scale(${this.zoom})`);
  }

  fitMap() {
    const cells = gameState.serverState.cells || [];
    if (cells.length === 0) return;

    let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
    
    // Find grid bounding box
    cells.forEach(cell => {
      const { x, y } = hexToPixel(cell.x, cell.y, this.hexSize);
      if (x < minX) minX = x;
      if (x > maxX) maxX = x;
      if (y < minY) minY = y;
      if (y > maxY) maxY = y;
    });

    const mapW = (maxX - minX) + this.hexSize * 2;
    const mapH = (maxY - minY) + this.hexSize * 2;
    const svgW = this.svg.clientWidth || 800;
    const svgH = this.svg.clientHeight || 500;

    const zoomW = svgW / mapW;
    const zoomH = svgH / mapH;
    this.zoom = Math.min(zoomW, zoomH, 1.2) * 0.9; // 90% view fill

    // Center map
    this.panX = (svgW - mapW * this.zoom) / 2 - minX * this.zoom + this.hexSize * this.zoom;
    this.panY = (svgH - mapH * this.zoom) / 2 - minY * this.zoom + this.hexSize * this.zoom;

    this.applyTransform();
  }

  centerOn(x, y) {
    const { x: px, y: py } = hexToPixel(x, y, this.hexSize);
    const svgW = this.svg.clientWidth || 800;
    const svgH = this.svg.clientHeight || 500;

    this.panX = svgW / 2 - px * this.zoom;
    this.panY = svgH / 2 - py * this.zoom;

    this.applyTransform();
  }

  render() {
    this.g.innerHTML = ""; // Clear SVG
    this.tooltip.style.display = "none";

    const state = gameState.serverState;
    if (!state || !state.cells || state.cells.length === 0) {
      this.g.innerHTML = `<text x="100" y="100" fill="#fff" font-size="20">Waiting for game data...</text>`;
      return;
    }

    const mapWidth = gameState.serverState.mapWidth || 20;
    const mapHeight = gameState.serverState.mapHeight || 15;

    // Render Hex cells
    state.cells.forEach(cell => {
      this.renderCell(cell);
    });

    // Render Spots
    if (state.spots) {
      state.spots.forEach(spot => {
        this.renderSpot(spot);
      });
    }

    // Render Agents (stacked)
    this.renderAgents();

    // Render Action Paths
    this.renderActionPaths();
  }

  renderCell(cell) {
    const { x: cx, y: cy } = hexToPixel(cell.x, cell.y, this.hexSize);
    const points = getHexagonPoints(cx, cy, this.hexSize);

    const polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
    polygon.setAttribute("points", points);
    polygon.setAttribute("class", `hex-tile hex-${cell.terrainType.toLowerCase()}`);
    polygon.setAttribute("data-x", cell.x);
    polygon.setAttribute("data-y", cell.y);

    // Styling borders & fill via classes
    polygon.style.stroke = this.getTerrainStrokeColor(cell.terrainType);
    polygon.style.fill = this.getTerrainFillColor(cell.terrainType);
    polygon.style.strokeWidth = "1.5px";

    // Text ID (Coordinates / Index)
    const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
    text.setAttribute("x", cx);
    text.setAttribute("y", cy + 4);
    text.setAttribute("class", "hex-label");
    text.textContent = `${cell.x},${cell.y}`;

    // Click behavior
    polygon.addEventListener("click", () => {
      this.handleCellClick(cell.x, cell.y);
    });
    text.addEventListener("click", () => {
      this.handleCellClick(cell.x, cell.y);
    });

    // Hover tooltip
    const showTooltip = (e) => {
      const spot = stateSpotAt(cell.x, cell.y);
      const agents = stateAgentsAt(cell.x, cell.y);
      
      let tooltipHtml = `
        <div style="font-weight:600; color:#fff; margin-bottom:4px;">Cell (${cell.x}, ${cell.y})</div>
        <div>Terrain: <span class="terrain-badge terrain-${cell.terrainType.toLowerCase()}">${cell.terrainType}</span></div>
      `;

      if (cell.terrainType === "ROAD") {
        // Traffic simulation placeholder (as server does not provide yet)
        tooltipHtml += `<div>Traffic: <span style="color:#10B981">Smooth</span></div>`;
      }

      if (spot) {
        tooltipHtml += `<div style="margin-top:6px; border-top:1px solid #374151; padding-top:4px;">
          <strong>Spot:</strong> ${spot.spotType}<br/>
        `;
        // Stocks per team
        Object.keys(spot.teamUdonStocks || {}).forEach(team => {
          tooltipHtml += `<span style="font-size:11px;">• ${team}: ${spot.teamUdonStocks[team]} Udon</span><br/>`;
        });
        tooltipHtml += `</div>`;
      }

      if (agents.length > 0) {
        tooltipHtml += `<div style="margin-top:6px; border-top:1px solid #374151; padding-top:4px;">
          <strong>Agents here:</strong><br/>
          ${agents.map(a => `<span style="font-size:11px;">• ${a.id} (${a.type}) - Fuel: ${a.fuel}</span>`).join("<br/>")}
        </div>`;
      }

      this.tooltip.innerHTML = tooltipHtml;
      this.tooltip.style.display = "block";
      this.positionTooltip(e);
    };

    polygon.addEventListener("mousemove", showTooltip);
    polygon.addEventListener("mouseleave", () => {
      this.tooltip.style.display = "none";
    });

    this.g.appendChild(polygon);
    this.g.appendChild(text);
  }

  getTerrainFillColor(terrain) {
    switch (terrain) {
      case "PLAIN": return "rgba(16, 185, 129, 0.12)"; // green tint
      case "MOUNTAIN": return "rgba(107, 114, 128, 0.2)"; // gray tint
      case "POND": return "rgba(59, 130, 246, 0.35)"; // blue tint
      case "ROAD": return "rgba(245, 158, 11, 0.15)"; // orange tint
      default: return "transparent";
    }
  }

  getTerrainStrokeColor(terrain) {
    switch (terrain) {
      case "PLAIN": return "#10B981"; // green border
      case "MOUNTAIN": return "#4B5563"; // gray border
      case "POND": return "#2563EB"; // blue border
      case "ROAD": return "#D97706"; // orange border
      default: return "#374151";
    }
  }

  renderSpot(spot) {
    if (!spot || !spot.cell) return;
    const { x, y } = spot.cell;
    const { x: cx, y: cy } = hexToPixel(x, y, this.hexSize);

    // Add Icon Container
    const gIcon = document.createElementNS("http://www.w3.org/2000/svg", "g");
    
    // Draw SVG icon
    let iconSvg = "";
    if (spot.spotType === "FUEL_STATION") {
      iconSvg = Icons.FUEL_STATION("#10B981", 20);
    } else {
      iconSvg = Icons.UDON("#EF4444", 20);
    }
    
    gIcon.innerHTML = iconSvg;
    const iconElement = gIcon.firstElementChild;
    iconElement.setAttribute("x", cx - 10);
    iconElement.setAttribute("y", cy - 25);
    this.g.appendChild(iconElement);

    // Stock Badge
    const myTeam = gameState.registeredTeamName;
    const stock = spot.teamUdonStocks ? spot.teamUdonStocks[myTeam] : 0;
    
    if (stock !== undefined && spot.spotType !== "FUEL_STATION") {
      const badge = document.createElementNS("http://www.w3.org/2000/svg", "g");
      
      const rect = document.createElementNS("http://www.w3.org/2000/svg", "rect");
      rect.setAttribute("x", cx + 2);
      rect.setAttribute("y", cy - 26);
      rect.setAttribute("width", 14);
      rect.setAttribute("height", 14);
      rect.setAttribute("rx", 3);
      rect.setAttribute("fill", "#EF4444");

      const text = document.createElementNS("http://www.w3.org/2000/svg", "text");
      text.setAttribute("x", cx + 9);
      text.setAttribute("y", cy - 16);
      text.setAttribute("fill", "#fff");
      text.setAttribute("font-size", "9px");
      text.setAttribute("font-weight", "bold");
      text.setAttribute("text-anchor", "middle");
      text.textContent = stock;

      badge.appendChild(rect);
      badge.appendChild(text);
      this.g.appendChild(badge);
    }
  }

  renderAgents() {
    const agents = gameState.getAgents();
    if (agents.length === 0) return;

    // Group agents by position cell "x_y"
    const groups = {};
    agents.forEach(agent => {
      const key = `${agent.posX}_${agent.posY}`;
      if (!groups[key]) groups[key] = [];
      groups[key].push(agent);
    });

    // Draw agent icons
    Object.keys(groups).forEach(key => {
      const groupAgents = groups[key];
      const [firstAgent] = groupAgents;
      const { x: cx, y: cy } = hexToPixel(firstAgent.posX, firstAgent.posY, this.hexSize);

      const agentGroupG = document.createElementNS("http://www.w3.org/2000/svg", "g");
      agentGroupG.setAttribute("class", "agent-element");

      // Check if selected agent is in this cell
      const hasSelected = groupAgents.some(a => a.id === gameState.selectedAgentId);
      if (hasSelected) {
        // Highlight circle
        const ring = document.createElementNS("http://www.w3.org/2000/svg", "circle");
        ring.setAttribute("cx", cx);
        ring.setAttribute("cy", cy);
        ring.setAttribute("r", this.hexSize * 0.7);
        ring.setAttribute("fill", "none");
        ring.setAttribute("stroke", "#3B82F6");
        ring.setAttribute("stroke-width", "3");
        ring.setAttribute("stroke-dasharray", "4 2");
        
        const animate = document.createElementNS("http://www.w3.org/2000/svg", "animateTransform");
        animate.setAttribute("attributeName", "transform");
        animate.setAttribute("type", "rotate");
        animate.setAttribute("from", `0 ${cx} ${cy}`);
        animate.setAttribute("to", `360 ${cx} ${cy}`);
        animate.setAttribute("dur", "8s");
        animate.setAttribute("repeatCount", "indefinite");
        
        ring.appendChild(animate);
        agentGroupG.appendChild(ring);
      }

      // Draw standard icon (draw stacked representation if size > 1)
      const agentIconG = document.createElementNS("http://www.w3.org/2000/svg", "g");
      
      const isPatrol = firstAgent.type === "PATROL";
      const mainColor = isPatrol ? "#60A5FA" : "#F59E0B";
      const iconSvg = isPatrol ? Icons.PATROL(mainColor, 26) : Icons.REFUEL(mainColor, 26);
      
      agentIconG.innerHTML = iconSvg;
      const icon = agentIconG.firstElementChild;
      icon.setAttribute("x", cx - 13);
      icon.setAttribute("y", cy - 13);
      agentGroupG.appendChild(icon);

      // Stack badge if more than 1 agent in the cell
      if (groupAgents.length > 1) {
        const stackBadge = document.createElementNS("http://www.w3.org/2000/svg", "g");
        
        const badgeCircle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
        badgeCircle.setAttribute("cx", cx + 12);
        badgeCircle.setAttribute("cy", cy + 12);
        badgeCircle.setAttribute("r", 8);
        badgeCircle.setAttribute("fill", "#1F2937");
        badgeCircle.setAttribute("stroke", "#4B5563");
        badgeCircle.setAttribute("stroke-width", "1");

        const badgeText = document.createElementNS("http://www.w3.org/2000/svg", "text");
        badgeText.setAttribute("x", cx + 12);
        badgeText.setAttribute("y", cy + 15);
        badgeText.setAttribute("fill", "#fff");
        badgeText.setAttribute("font-size", "9px");
        badgeText.setAttribute("font-weight", "bold");
        badgeText.setAttribute("text-anchor", "middle");
        badgeText.textContent = `+${groupAgents.length - 1}`;

        stackBadge.appendChild(badgeCircle);
        stackBadge.appendChild(badgeText);
        agentGroupG.appendChild(stackBadge);
      }

      // Fuel bar for patrols
      if (isPatrol) {
        const fuelPercent = Math.min(100, Math.max(0, (firstAgent.fuel / 100) * 100)); // fuel out of 100
        const barW = 20;
        const barH = 3;
        const bx = cx - barW / 2;
        const by = cy + 14;

        const fuelBg = document.createElementNS("http://www.w3.org/2000/svg", "rect");
        fuelBg.setAttribute("x", bx);
        fuelBg.setAttribute("y", by);
        fuelBg.setAttribute("width", barW);
        fuelBg.setAttribute("height", barH);
        fuelBg.setAttribute("rx", 1);
        fuelBg.setAttribute("fill", "#374151");

        const fuelFill = document.createElementNS("http://www.w3.org/2000/svg", "rect");
        fuelFill.setAttribute("x", bx);
        fuelFill.setAttribute("y", by);
        fuelFill.setAttribute("width", (barW * fuelPercent) / 100);
        fuelFill.setAttribute("height", barH);
        fuelFill.setAttribute("rx", 1);
        fuelFill.setAttribute("fill", fuelPercent > 30 ? "#10B981" : "#EF4444");

        agentGroupG.appendChild(fuelBg);
        agentGroupG.appendChild(fuelFill);
      }

      // Add interactivity to select agent on click
      agentGroupG.style.cursor = "pointer";
      agentGroupG.addEventListener("click", (e) => {
        e.stopPropagation(); // Avoid cell click triggering
        // Select first agent or cycle selections in cell
        const currentSelectedId = gameState.selectedAgentId;
        const currentIndex = groupAgents.findIndex(a => a.id === currentSelectedId);
        const nextAgent = groupAgents[(currentIndex + 1) % groupAgents.length];
        
        gameState.setSelectedAgentId(nextAgent.id);
        logger.info(`Selected Agent ${nextAgent.id} by map click.`);
      });

      this.g.appendChild(agentGroupG);
    });
  }

  renderActionPaths() {
    const selectedAgent = gameState.getSelectedAgent();
    if (!selectedAgent) return;

    const queue = gameState.localActionQueue[selectedAgent.id] || [];
    if (queue.length === 0) return;

    let currentX = selectedAgent.posX;
    let currentY = selectedAgent.posY;

    const pathPoints = [];
    const { x: startPx, y: startPy } = hexToPixel(currentX, currentY, this.hexSize);
    pathPoints.push({ x: startPx, y: startPy });

    queue.forEach(action => {
      if (action.actionType === "MOVE" && action.targetX !== null && action.targetY !== null) {
        const tx = action.targetX;
        const ty = action.targetY;
        const { x: px, y: py } = hexToPixel(tx, ty, this.hexSize);
        
        pathPoints.push({ x: px, y: py, order: action.order });
        currentX = tx;
        currentY = ty;
      }
    });

    if (pathPoints.length < 2) return;

    // Draw lines & arrows
    for (let i = 0; i < pathPoints.length - 1; i++) {
      const p1 = pathPoints[i];
      const p2 = pathPoints[i+1];

      // Draw Line
      const line = document.createElementNS("http://www.w3.org/2000/svg", "line");
      line.setAttribute("x1", p1.x);
      line.setAttribute("y1", p1.y);
      line.setAttribute("x2", p2.x);
      line.setAttribute("y2", p2.y);
      line.setAttribute("stroke", "#3B82F6");
      line.setAttribute("stroke-width", "2.5");
      line.setAttribute("stroke-dasharray", "4 4");
      this.g.appendChild(line);

      // Draw target index circle
      const dot = document.createElementNS("http://www.w3.org/2000/svg", "circle");
      dot.setAttribute("cx", p2.x);
      dot.setAttribute("cy", p2.y);
      dot.setAttribute("r", "7");
      dot.setAttribute("fill", "#1E3A8A");
      dot.setAttribute("stroke", "#3B82F6");
      dot.setAttribute("stroke-width", "1.5");
      this.g.appendChild(dot);

      // Add order text
      const orderText = document.createElementNS("http://www.w3.org/2000/svg", "text");
      orderText.setAttribute("x", p2.x);
      orderText.setAttribute("y", p2.y + 2.5);
      orderText.setAttribute("fill", "#fff");
      orderText.setAttribute("font-size", "7.5px");
      orderText.setAttribute("font-weight", "bold");
      orderText.setAttribute("text-anchor", "middle");
      orderText.textContent = p2.order;
      this.g.appendChild(orderText);
    }
  }

  handleCellClick(x, y) {
    const selectedAgent = gameState.getSelectedAgent();
    if (!selectedAgent) {
      logger.warn("Select an agent first to plan moves.");
      return;
    }

    // Check if cell is Pond
    const cell = gameState.cellMap[`${x}_${y}`];
    if (cell && cell.terrainType === "POND") {
      logger.warn("Cannot plan a move into a Pond!");
      return;
    }

    // We want to add a move action to the clicked cell
    // Current tail of the path: either the agent's current position, or the target of their last MOVE action
    const queue = gameState.localActionQueue[selectedAgent.id] || [];
    let lastX = selectedAgent.posX;
    let lastY = selectedAgent.posY;
    
    // Find last move target
    for (let i = queue.length - 1; i >= 0; i--) {
      if (queue[i].actionType === "MOVE" && queue[i].targetX !== null) {
        lastX = queue[i].targetX;
        lastY = queue[i].targetY;
        break;
      }
    }

    // Must be adjacent to last path node
    if (lastX === x && lastY === y) {
      logger.warn("Agent is already at this position in path tail.");
      return;
    }

    if (!isAdjacent(lastX, lastY, x, y)) {
      logger.warn(`Cell (${x}, ${y}) is not adjacent to path tail (${lastX}, ${lastY}).`);
      return;
    }

    // Check if queue size has reached max
    const maxSteps = gameState.serverState.maxStepsPerTurn || 5;
    if (queue.length >= maxSteps) {
      logger.warn(`Action queue is full (${maxSteps} steps max).`);
      return;
    }

    // Valid: add MOVE action
    gameState.addAction(selectedAgent.id, "MOVE", x, y);
  }

  positionTooltip(e) {
    const svgRect = this.svg.getBoundingClientRect();
    const tooltipW = this.tooltip.offsetWidth;
    const tooltipH = this.tooltip.offsetHeight;
    
    let left = e.clientX - svgRect.left + 15;
    let top = e.clientY - svgRect.top + 15;

    // Boundary check
    if (left + tooltipW > svgRect.width) {
      left = e.clientX - svgRect.left - tooltipW - 15;
    }
    if (top + tooltipH > svgRect.height) {
      top = e.clientY - svgRect.top - tooltipH - 15;
    }

    this.tooltip.style.left = `${left}px`;
    this.tooltip.style.top = `${top}px`;
  }
}

// Helpers
function stateSpotAt(x, y) {
  const spots = gameState.serverState.spots || [];
  return spots.find(s => s.cell && s.cell.x === x && s.cell.y === y) || null;
}

function stateAgentsAt(x, y) {
  const agents = gameState.getAgents();
  return agents.filter(a => a.posX === x && a.posY === y);
}
