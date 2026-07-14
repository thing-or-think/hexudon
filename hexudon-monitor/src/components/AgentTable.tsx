import { useState } from 'react';
import { AgentTypeBadge } from './ui/Badge';
import { Modal } from './ui/Modal';
import type { AgentResponse, AgentType } from '../models/match';
import { fuelColor } from '../utils/colorUtils';

function coordStr(x: number, y: number) { return `(${x}, ${y})`; }

interface AgentTableProps {
  agents: AgentResponse[];
  agentTeams: Record<string, string>;
  maxFuel?: number;
}

export function AgentTable({ agents, agentTeams, maxFuel = 100 }: AgentTableProps) {
  const [teamFilter, setTeamFilter] = useState<string>('ALL');
  const [typeFilter, setTypeFilter] = useState<AgentType | 'ALL'>('ALL');
  const [selected, setSelected] = useState<AgentResponse | null>(null);

  const teams = ['ALL', ...Array.from(new Set(Object.values(agentTeams)))];

  const filtered = agents.filter((a) => {
    if (teamFilter !== 'ALL' && agentTeams[a.agentId] !== teamFilter) return false;
    if (typeFilter !== 'ALL' && a.agentType !== typeFilter) return false;
    return true;
  });

  return (
    <>
      <div className="filter-bar">
        <select className="select" value={teamFilter} onChange={(e) => setTeamFilter(e.target.value)} style={{ fontSize: '0.75rem' }}>
          {teams.map((t) => <option key={t}>{t}</option>)}
        </select>
        <select className="select" value={typeFilter} onChange={(e) => setTypeFilter(e.target.value as AgentType | 'ALL')} style={{ fontSize: '0.75rem' }}>
          <option value="ALL">ALL TYPES</option>
          <option value="PATROL">PATROL</option>
          <option value="REFUEL">REFUEL</option>
        </select>
        <span style={{ marginLeft: 'auto', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
          {filtered.length} agent{filtered.length !== 1 ? 's' : ''}
        </span>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Type</th>
              <th>Team</th>
              <th>Position</th>
              <th>Fuel</th>
              <th>Steps</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((agent) => {
              const pct = agent.fuel / maxFuel;
              return (
                <tr key={agent.agentId} onClick={() => setSelected(agent)}>
                  <td style={{ color: 'var(--accent-cyan)', fontWeight: 700 }}>{agent.agentId}</td>
                  <td><AgentTypeBadge type={agent.agentType} /></td>
                  <td style={{ color: 'var(--text-secondary)' }}>{agentTeams[agent.agentId] ?? '—'}</td>
                  <td>{coordStr(agent.coordinate.x, agent.coordinate.y)}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <div className="progress-bar" style={{ width: 60 }}>
                        <div
                          className="progress-fill"
                          style={{ width: `${pct * 100}%`, background: fuelColor(agent.fuel, maxFuel) }}
                        />
                      </div>
                      <span style={{ color: fuelColor(agent.fuel, maxFuel), fontWeight: 600 }}>{agent.fuel}</span>
                    </div>
                  </td>
                  <td>{agent.step}</td>
                </tr>
              );
            })}
            {filtered.length === 0 && (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>No agents found</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {selected && (
        <Modal title={`Agent ${selected.agentId}`} onClose={() => setSelected(null)}>
          <div style={{ display: 'grid', gap: '0.75rem' }}>
            <InfoRow label="Agent ID" value={selected.agentId} />
            <InfoRow label="Type" value={selected.agentType} />
            <InfoRow label="Team" value={agentTeams[selected.agentId] ?? 'Unknown'} />
            <InfoRow label="Position" value={coordStr(selected.coordinate.x, selected.coordinate.y)} />
            <InfoRow label="Fuel" value={`${selected.fuel} / ${maxFuel}`} />
            <InfoRow label="Remaining Steps" value={String(selected.step)} />
          </div>
        </Modal>
      )}
    </>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.4rem 0', borderBottom: '1px solid var(--border-subtle)' }}>
      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{label}</span>
      <span style={{ fontSize: '0.8rem', fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-primary)' }}>{value}</span>
    </div>
  );
}
