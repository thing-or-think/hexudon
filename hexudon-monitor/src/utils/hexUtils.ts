// Utilities for rendering Odd-R offset hex grids on canvas/SVG

export interface HexPoint {
  x: number;
  y: number;
}

/**
 * Convert Odd-R offset (col, row) to pixel center position.
 * Uses flat-top hexagons.
 */
export function hexToPixel(col: number, row: number, size: number): HexPoint {
  const oddOffset = row % 2 === 1 ? size * Math.sqrt(3) / 2 : 0;
  const x = col * size * Math.sqrt(3) + oddOffset + size * Math.sqrt(3) / 2;
  const y = row * size * 1.5 + size;
  return { x, y };
}

/**
 * Get the 6 corner points of a pointy-top hexagon centered at (cx, cy).
 */
export function hexCorners(cx: number, cy: number, size: number): HexPoint[] {
  return Array.from({ length: 6 }, (_, i) => {
    const angle = (Math.PI / 180) * (60 * i - 30);
    return {
      x: cx + size * Math.cos(angle),
      y: cy + size * Math.sin(angle),
    };
  });
}

/**
 * Get SVG polygon points string for a hex.
 */
export function hexPolygonPoints(cx: number, cy: number, size: number): string {
  return hexCorners(cx, cy, size)
    .map((p) => `${p.x.toFixed(2)},${p.y.toFixed(2)}`)
    .join(' ');
}

/**
 * Convert pixel (px, py) to nearest hex (col, row).
 */
export function pixelToHex(px: number, py: number, size: number): { col: number; row: number } {
  // Approximate row
  const row = Math.round((py - size) / (size * 1.5));
  const oddOffset = row % 2 === 1 ? (size * Math.sqrt(3)) / 2 : 0;
  const col = Math.round((px - oddOffset - (size * Math.sqrt(3)) / 2) / (size * Math.sqrt(3)));
  return { col: Math.max(0, col), row: Math.max(0, row) };
}

/**
 * Calculate the SVG viewport size for a grid.
 */
export function gridViewportSize(
  width: number,
  height: number,
  hexSize: number
): { svgWidth: number; svgHeight: number } {
  const svgWidth = width * hexSize * Math.sqrt(3) + hexSize * Math.sqrt(3) / 2 + 10;
  const svgHeight = height * hexSize * 1.5 + hexSize + 10;
  return { svgWidth, svgHeight };
}
