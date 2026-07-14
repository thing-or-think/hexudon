export function formatTurn(turn: number, maxTurn: number): string {
  return `${turn} / ${maxTurn}`;
}

export function formatFuel(fuel: number, maxFuel: number): string {
  return `${fuel} / ${maxFuel}`;
}

export function formatMs(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  return `${(ms / 1000).toFixed(1)}s`;
}

export function formatTimestamp(ts: number): string {
  return new Date(ts).toLocaleTimeString();
}

export function coordLabel(x: number, y: number): string {
  return `(${x}, ${y})`;
}

export function rankSuffix(rank: number): string {
  if (rank === 1) return '🥇';
  if (rank === 2) return '🥈';
  if (rank === 3) return '🥉';
  return `#${rank}`;
}
