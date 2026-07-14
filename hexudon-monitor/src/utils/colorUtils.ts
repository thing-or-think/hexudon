import type { TerrainType, TrafficLevel, AgentType } from '../models/match';

export function terrainColor(terrain: TerrainType): string {
  switch (terrain) {
    case 'PLAIN': return '#2d4a2d';
    case 'ROAD': return '#3a3a4a';
    case 'MOUNTAIN': return '#5a4a3a';
    case 'POND': return '#1a3a5a';
    default: return '#1e293b';
  }
}

export function terrainStroke(terrain: TerrainType): string {
  switch (terrain) {
    case 'PLAIN': return '#3d6a3d';
    case 'ROAD': return '#5a5a7a';
    case 'MOUNTAIN': return '#7a6a5a';
    case 'POND': return '#2a5a8a';
    default: return '#334155';
  }
}

export function trafficColor(level: TrafficLevel): string {
  switch (level) {
    case 'NORMAL': return 'rgba(34,197,94,0.35)';
    case 'BUSY': return 'rgba(234,179,8,0.45)';
    case 'CONGESTED': return 'rgba(239,68,68,0.55)';
    default: return 'transparent';
  }
}

export function trafficBadgeClass(level: TrafficLevel): string {
  switch (level) {
    case 'NORMAL': return 'badge-success';
    case 'BUSY': return 'badge-warning';
    case 'CONGESTED': return 'badge-danger';
    default: return 'badge-neutral';
  }
}

export function agentColor(type: AgentType): string {
  return type === 'PATROL' ? '#3b82f6' : '#eab308';
}

export function statusColor(status: string): string {
  switch (status) {
    case 'WAITING': return '#eab308';
    case 'PLAYING': return '#22c55e';
    case 'FINISHED': return '#94a3b8';
    default: return '#64748b';
  }
}

export function udonColor(typeName: string): string {
  switch (typeName) {
    case 'TANUKI': return '#f97316';
    case 'KITSUNE': return '#ec4899';
    case 'TEMPURA': return '#a78bfa';
    case 'BEEF': return '#ef4444';
    default: return '#64748b';
  }
}

export function fuelColor(fuel: number, maxFuel = 100): string {
  const pct = fuel / maxFuel;
  if (pct > 0.6) return '#22c55e';
  if (pct > 0.3) return '#eab308';
  return '#ef4444';
}
