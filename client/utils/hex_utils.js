/**
 * Hexagonal Grid Utilities
 *
 * Coordinates are represented as (x, y) where:
 * - x is the horizontal column index
 * - y is the vertical row index
 *
 * This grid uses an "even-y" or "odd-y" offset layout.
 * Based on the server implementation in HexGridUtils / MapValidator:
 * For row y:
 * - If y is odd:
 *   1. (1, 0)   -> East (1)
 *   2. (0, 1)   -> South-East (2)
 *   3. (-1, 1)  -> South-West (3)
 *   4. (-1, 0)  -> West (4)
 *   5. (-1, -1) -> North-West (5)
 *   6. (0, -1)  -> North-East (6)
 * - If y is even:
 *   1. (1, 0)   -> East (1)
 *   2. (1, 1)   -> South-East (2)
 *   3. (0, 1)   -> South-West (3)
 *   4. (-1, 0)  -> West (4)
 *   5. (0, -1)  -> North-West (5)
 *   6. (1, -1)  -> North-East (6)
 */

export function getHexOffsets(y) {
  if (y % 2 !== 0) {
    // y is odd
    return [
      { dx: 1, dy: 0 },   // 1: East
      { dx: 0, dy: 1 },   // 2: South-East
      { dx: -1, dy: 1 },  // 3: South-West
      { dx: -1, dy: 0 },  // 4: West
      { dx: -1, dy: -1 }, // 5: North-West
      { dx: 0, dy: -1 }   // 6: North-East
    ];
  } else {
    // y is even
    return [
      { dx: 1, dy: 0 },   // 1: East
      { dx: 1, dy: 1 },   // 2: South-East
      { dx: 0, dy: 1 },   // 3: South-West
      { dx: -1, dy: 0 },  // 4: West
      { dx: 0, dy: -1 },  // 5: North-West
      { dx: 1, dy: -1 }   // 6: North-East
    ];
  }
}

/**
 * Returns the neighbors of a cell at (x, y)
 */
export function getNeighbors(x, y, mapWidth, mapHeight) {
  const offsets = getHexOffsets(y);
  const neighbors = [];
  for (let i = 0; i < offsets.length; i++) {
    const nx = x + offsets[i].dx;
    const ny = y + offsets[i].dy;
    if (nx >= 0 && nx < mapWidth && ny >= 0 && ny < mapHeight) {
      neighbors.push({ x: nx, y: ny, direction: i + 1 });
    }
  }
  return neighbors;
}

/**
 * Check if cell 2 is adjacent to cell 1
 */
export function isAdjacent(x1, y1, x2, y2) {
  const offsets = getHexOffsets(y1);
  for (const offset of offsets) {
    if (x1 + offset.dx === x2 && y1 + offset.dy === y2) {
      return true;
    }
  }
  return false;
}

/**
 * Get direction number (1 to 6) from cell 1 to cell 2. Returns 0 if not adjacent.
 */
export function getDirection(x1, y1, x2, y2) {
  const offsets = getHexOffsets(y1);
  for (let i = 0; i < offsets.length; i++) {
    if (x1 + offsets[i].dx === x2 && y1 + offsets[i].dy === y2) {
      return i + 1;
    }
  }
  return 0;
}

/**
 * Get target coordinate from cell (x, y) moving in direction (1-6)
 */
export function getTargetCell(x, y, direction) {
  const offsets = getHexOffsets(y);
  const idx = direction - 1;
  if (idx >= 0 && idx < offsets.length) {
    return {
      x: x + offsets[idx].dx,
      y: y + offsets[idx].dy
    };
  }
  return null;
}

/**
 * Converts axial/offset coordinates to 2D pixel coordinates for rendering.
 * Using a flat-topped hexagon configuration:
 * Width of flat-topped hex = 2 * size
 * Horizontal spacing = 1.5 * size
 * Vertical spacing = sqrt(3) * size
 * If row is odd, offset vertical position horizontally.
 */
export function hexToPixel(x, y, size) {
  const width = 2 * size;
  const height = Math.sqrt(3) * size;
  
  // Flat-topped layout:
  // Column x spacing is 1.5 * size.
  // Row y spacing is height.
  // Odd rows are shifted vertically.
  const px = x * size * 1.5;
  let py = y * height;
  if (x % 2 !== 0) {
    // In flat-topped, y coordinates of odd columns are offset by height / 2.
    // Wait, is the server's layout flat-topped or pointy-topped?
    // Let's look at getDirections:
    // If y is odd/even, the neighbor offsets in X change. This means rows are shifted horizontally, which is Pointy-Topped!
    // In Pointy-topped hex grid:
    // y goes straight down. Odd rows have different X neighbors (shifted by 0.5 hex width).
    // Let's verify Pointy-topped coordinates:
    // Width of pointy hex = sqrt(3) * size.
    // Height of pointy hex = 2 * size.
    // Horizontal spacing = width.
    // Vertical spacing = 0.75 * height (1.5 * size).
    // Column x of row y is shifted by width / 2 if y is odd.
  }
  
  // Let's use standard pointy-topped calculation:
  const hexWidth = Math.sqrt(3) * size;
  const hexHeight = 2 * size;
  const vertSpacing = 1.5 * size;
  
  const cx = x * hexWidth + (y % 2 !== 0 ? hexWidth / 2 : 0);
  const cy = y * vertSpacing;
  
  return { x: cx, y: cy, width: hexWidth, height: hexHeight };
}

/**
 * Generates the SVG path points for a pointy-topped hexagon
 */
export function getHexagonPoints(cx, cy, size) {
  const points = [];
  for (let i = 0; i < 6; i++) {
    // For pointy topped: angle starts at 30 degrees (pi/6)
    const angle = (Math.PI / 180) * (60 * i - 30);
    points.push(`${cx + size * Math.cos(angle)},${cy + size * Math.sin(angle)}`);
  }
  return points.join(" ");
}
