import { useState, useMemo } from 'react';
import { hexToPixel, hexPolygonPoints, gridViewportSize } from '../utils/hexUtils';
import { terrainColor, terrainStroke, trafficColor, agentColor, udonColor } from '../utils/colorUtils';
import { Modal } from './ui/Modal';
import { TrafficBadge, AgentTypeBadge } from './ui/Badge';
import type { CellResponse, SpotResponse, TrafficResponse, AgentResponse } from '../models/match';

const HEX_SIZE = 22;

interface HexGridViewerProps {
  cells: CellResponse[];
  spots: SpotResponse[];
  traffic: TrafficResponse[];
  agents: AgentResponse[];
  agentTeams?: Record<string, string>;
  width: number;
  height: number;
}

interface SelectedCell {
  cell: CellResponse;
  spot?: SpotResponse;
  trafficInfo?: TrafficResponse;
  cellAgents: AgentResponse[];
}

export function HexGridViewer({
  cells, spots, traffic, agents, agentTeams = {}, width, height
}: HexGridViewerProps) {
  const [selected, setSelected] = useState<SelectedCell | null>(null);

  // Build lookup maps
  const spotMap = useMemo(() => {
    const m = new Map<string, SpotResponse>();
    spots.forEach((s) => m.set(`${s.coordinate.x},${s.coordinate.y}`, s));
    return m;
  }, [spots]);

  const trafficMap = useMemo(() => {
    const m = new Map<string, TrafficResponse>();
    traffic.forEach((t) => m.set(`${t.coordinate.x},${t.coordinate.y}`, t));
    return m;
  }, [traffic]);

  const agentMap = useMemo(() => {
    const m = new Map<string, AgentResponse[]>();
    agents.forEach((a) => {
      const key = `${a.coordinate.x},${a.coordinate.y}`;
      const existing = m.get(key) ?? [];
      m.set(key, [...existing, a]);
    });
    return m;
  }, [agents]);

  const { svgWidth, svgHeight } = gridViewportSize(width, height, HEX_SIZE);

  function handleCellClick(cell: CellResponse) {
    const key = `${cell.coordinate.x},${cell.coordinate.y}`;
    setSelected({
      cell,
      spot: spotMap.get(key),
      trafficInfo: trafficMap.get(key),
      cellAgents: agentMap.get(key) ?? [],
    });
  }

  return (
    <>
      <div className="hex-grid-wrapper" style={{ maxHeight: '480px' }}>
        <svg
          width={svgWidth}
          height={svgHeight}
          style={{ display: 'block', minWidth: svgWidth }}
        >
          {cells.map((cell) => {
            const { x: col, y: row } = cell.coordinate;
            const { x: cx, y: cy } = hexToPixel(col, row, HEX_SIZE);
            const key = `${col},${row}`;
            const spot = spotMap.get(key);
            const trafficInfo = trafficMap.get(key);
            const cellAgents = agentMap.get(key) ?? [];
            const points = hexPolygonPoints(cx, cy, HEX_SIZE - 1);
            const fill = terrainColor(cell.terrainType);
            const stroke = terrainStroke(cell.terrainType);
            const tColor = trafficInfo ? trafficColor(trafficInfo.trafficLevel) : 'transparent';

            return (
              <g
                key={key}
                className="hex-cell"
                onClick={() => handleCellClick(cell)}
              >
                {/* Base terrain */}
                <polygon points={points} fill={fill} stroke={stroke} strokeWidth="0.8" />
                {/* Traffic overlay */}
                {trafficInfo && (
                  <polygon points={points} fill={tColor} stroke="none" />
                )}
                {/* Spot indicator */}
                {spot && (
                  <circle
                    cx={cx}
                    cy={cy - 5}
                    r={4}
                    fill={udonColor(spot.udonType.typeName)}
                    opacity={0.9}
                  />
                )}
                {/* Agent indicators */}
                {cellAgents.map((agent, i) => (
                  <circle
                    key={agent.agentId}
                    cx={cx + (i - (cellAgents.length - 1) / 2) * 8}
                    cy={cy + 5}
                    r={4}
                    fill={agentColor(agent.agentType)}
                    stroke="rgba(0,0,0,0.5)"
                    strokeWidth="1"
                  />
                ))}
                {/* Coordinate on hover (shown as tiny text) */}
                <text
                  x={cx}
                  y={cy + 1}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  fontSize="7"
                  fill="rgba(255,255,255,0.25)"
                  pointerEvents="none"
                >
                  {col},{row}
                </text>
              </g>
            );
          })}
        </svg>
      </div>

      {/* Legend */}
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginTop: '0.75rem', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
        {[['PLAIN', '#2d4a2d'], ['ROAD', '#3a3a4a'], ['MOUNTAIN', '#5a4a3a'], ['POND', '#1a3a5a']].map(([t, c]) => (
          <div key={t} style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <div style={{ width: 10, height: 10, background: c, borderRadius: 2 }} />
            {t}
          </div>
        ))}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#3b82f6' }} /> PATROL
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#eab308' }} /> REFUEL
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
          <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#f97316' }} /> Udon Spot
        </div>
      </div>

      {/* Cell detail modal */}
      {selected && (
        <Modal title={`Cell (${selected.cell.coordinate.x}, ${selected.cell.coordinate.y})`} onClose={() => setSelected(null)}>
          <div style={{ display: 'grid', gap: '0.75rem' }}>
            <Row label="Coordinate" value={`(${selected.cell.coordinate.x}, ${selected.cell.coordinate.y})`} />
            <Row label="Terrain" value={selected.cell.terrainType} />
            {selected.trafficInfo && (
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Traffic</span>
                <TrafficBadge level={selected.trafficInfo.trafficLevel} />
              </div>
            )}
            {selected.spot && (
              <>
                <Row label="Udon Type" value={selected.spot.udonType.typeName} />
                <Row label="Stock" value={String(selected.spot.amount)} />
              </>
            )}
            {selected.cellAgents.length > 0 && (
              <div>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginBottom: '0.4rem' }}>Agents on Cell</div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                  {selected.cellAgents.map((agent) => (
                    <div key={agent.agentId} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.35rem 0.5rem', background: 'rgba(255,255,255,0.03)', borderRadius: 6 }}>
                      <span style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '0.78rem', color: 'var(--text-primary)' }}>{agent.agentId}</span>
                      <AgentTypeBadge type={agent.agentType} />
                      {agentTeams[agent.agentId] && (
                        <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{agentTeams[agent.agentId]}</span>
                      )}
                      <span style={{ marginLeft: 'auto', fontSize: '0.7rem', color: 'var(--text-muted)' }}>⛽ {agent.fuel}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </Modal>
      )}
    </>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '0.5rem' }}>
      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{label}</span>
      <span style={{ fontSize: '0.8rem', fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-primary)' }}>{value}</span>
    </div>
  );
}
