import { RefreshCw, WifiOff } from 'lucide-react';
import { useMatchStore } from '../stores/useMatchStore';
import { useMatchConfig } from '../hooks/useMatchConfig';
import { useMatchState } from '../hooks/useMatchState';
import { usePolling } from '../hooks/usePolling';
import { HexGridViewer } from '../components/HexGridViewer';
import { SimulationControls } from '../components/SimulationControls';
import { StatCard, Card } from '../components/ui/Card';
import { StatusBadge } from '../components/ui/Badge';
import { ScoreBoard } from '../components/ScoreBoard';
import { MOCK_AGENT_TEAMS } from '../mock/agent.mock';
import { statusColor } from '../utils/colorUtils';

export function Dashboard() {
  const { config } = useMatchConfig();
  const { state, isLoading, lastUpdated, refetch } = useMatchState(3000);
  const { isSimulating, simulationSpeed, useMockData } = useMatchStore();

  usePolling(refetch, simulationSpeed, isSimulating);

  const turnPct = state && config ? (state.turn / config.maxTurn) * 100 : 0;

  return (
    <div className="page-body">
      {/* Topbar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.25rem' }}>
        <div>
          <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)' }}>Match Dashboard</h2>
          <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
            {lastUpdated ? `Updated ${lastUpdated.toLocaleTimeString()}` : 'Connecting...'}
          </p>
        </div>
        <div style={{ marginLeft: 'auto', display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          {useMockData && (
            <span className="mock-indicator"><WifiOff size={10} /> MOCK</span>
          )}
          {state && <StatusBadge status={state.status} />}
          <button className="btn btn-ghost" onClick={refetch} disabled={isLoading}>
            <RefreshCw size={13} className={isLoading ? 'animate-spin' : ''} />
            Refresh
          </button>
        </div>
      </div>

      {/* Simulation Controls */}
      <SimulationControls onRefresh={refetch} />

      <div style={{ height: '1rem' }} />

      {/* Stat Cards */}
      <div className="grid-4" style={{ marginBottom: '1rem' }}>
        <StatCard
          label="Match Status"
          value={state?.status ?? '—'}
          color={state ? statusColor(state.status) : undefined}
          icon="🎮"
        />
        <StatCard
          label="Current Turn"
          value={state ? `${state.turn} / ${config?.maxTurn ?? 50}` : '—'}
          color="var(--accent-cyan)"
          icon="🔄"
        />
        <StatCard
          label="Teams"
          value={state?.teamScores.length ?? config?.agentsPerTeam ?? '—'}
          color="var(--accent-purple)"
          icon="👥"
        />
        <StatCard
          label="Agents"
          value={state?.agents.length ?? '—'}
          color="var(--accent-blue)"
          icon="🤖"
        />
      </div>

      {/* Turn progress */}
      {state && config && (
        <div className="card" style={{ marginBottom: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span className="card-title" style={{ margin: 0 }}>Turn Progress</span>
            <span style={{ fontSize: '0.78rem', fontFamily: 'JetBrains Mono, monospace', color: 'var(--accent-cyan)' }}>
              {state.turn} / {config.maxTurn}
            </span>
          </div>
          <div className="progress-bar" style={{ height: 8 }}>
            <div className="progress-fill" style={{ width: `${turnPct}%`, background: 'linear-gradient(90deg, var(--accent-blue), var(--accent-cyan))' }} />
          </div>
        </div>
      )}

      {/* Hex Map */}
      {config && state && (
        <Card title="Hex Map Viewer" style={{ marginBottom: '1rem' }}>
          <HexGridViewer
            cells={config.cells}
            spots={state.spots}
            traffic={state.traffic}
            agents={state.agents}
            agentTeams={MOCK_AGENT_TEAMS}
            width={config.mapWidth}
            height={config.mapHeight}
          />
        </Card>
      )}

      {/* Scoreboard */}
      {state && state.teamScores.length > 0 && (
        <Card title="Live Scoreboard">
          <ScoreBoard scores={state.teamScores} />
        </Card>
      )}
    </div>
  );
}
