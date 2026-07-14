import { useMatchState } from '../hooks/useMatchState';
import { useMatchConfig } from '../hooks/useMatchConfig';
import { AgentTable } from '../components/AgentTable';
import { Card, StatCard } from '../components/ui/Card';
import { PageLoader } from '../components/ui/LoadingSpinner';
import { MOCK_AGENT_TEAMS } from '../mock/agent.mock';

export function AgentsPage() {
  const { state } = useMatchState();
  const { config } = useMatchConfig();

  if (!state) return <PageLoader />;

  const patrol = state.agents.filter((a) => a.agentType === 'PATROL');
  const refuel = state.agents.filter((a) => a.agentType === 'REFUEL');
  const avgFuel = state.agents.length > 0
    ? Math.round(state.agents.reduce((s, a) => s + a.fuel, 0) / state.agents.length)
    : 0;

  return (
    <div className="page-body">
      <h2 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Agent Monitoring</h2>

      <div className="grid-4" style={{ marginBottom: '1rem' }}>
        <StatCard label="Total Agents" value={state.agents.length} color="var(--accent-blue)" icon="🤖" />
        <StatCard label="Patrol Agents" value={patrol.length} color="var(--accent-blue)" icon="🔵" />
        <StatCard label="Refuel Agents" value={refuel.length} color="var(--accent-yellow)" icon="🟡" />
        <StatCard label="Avg Fuel" value={`${avgFuel}`} sub={`/ ${config?.maxFuel ?? 100}`} color="var(--accent-green)" icon="⛽" />
      </div>

      <Card title="Agent List">
        <AgentTable
          agents={state.agents}
          agentTeams={MOCK_AGENT_TEAMS}
          maxFuel={config?.maxFuel ?? 100}
        />
      </Card>
    </div>
  );
}
