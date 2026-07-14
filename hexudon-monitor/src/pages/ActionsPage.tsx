
import { Card } from '../components/ui/Card';
import { ActionTimeline } from '../components/ActionTimeline';
import type { ActionHistoryEntry, ActionType } from '../models/match';
import { useMatchState } from '../hooks/useMatchState';

// Generate mock action history based on agents
function generateMockHistory(turn: number): ActionHistoryEntry[] {
  const entries: ActionHistoryEntry[] = [];
  const teamAgents = [
    { agentId: 'A1', teamName: 'Team Alpha' },
    { agentId: 'A2', teamName: 'Team Alpha' },
    { agentId: 'A3', teamName: 'Team Beta' },
    { agentId: 'A4', teamName: 'Team Beta' },
  ];
  const actions: ActionType[] = ['MOVE', 'MOVE', 'WAIT', 'MOVE'];
  const statuses: ActionHistoryEntry['status'][] = ['SUCCESS', 'SUCCESS', 'FAILED', 'SUCCESS'];

  for (let t = Math.max(1, turn - 4); t <= turn; t++) {
    teamAgents.forEach((agent, i) => {
      entries.push({
        turn: t,
        teamName: agent.teamName,
        agentId: agent.agentId,
        actionType: actions[i % actions.length],
        fromCoordinate: { x: i * 2, y: t % 5 },
        toCoordinate: actions[i % actions.length] === 'MOVE' ? { x: i * 2 + 1, y: t % 5 } : undefined,
        status: statuses[(t + i) % statuses.length],
        timestamp: Date.now() - (turn - t) * 3000,
      });
    });
  }
  return entries.reverse();
}

export function ActionsPage() {
  const { state } = useMatchState();
  const history = generateMockHistory(state?.turn ?? 1);

  return (
    <div className="page-body">
      <h2 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Action Monitoring</h2>

      <div className="grid-3" style={{ marginBottom: '1rem' }}>
        {[
          ['Total Actions', history.length, 'var(--accent-blue)'],
          ['Success', history.filter(h => h.status === 'SUCCESS').length, 'var(--accent-green)'],
          ['Failed / Rejected', history.filter(h => h.status !== 'SUCCESS').length, 'var(--accent-red)'],
        ].map(([label, value, color]) => (
          <div key={String(label)} className="card">
            <div className="card-title">{label}</div>
            <div className="stat-value" style={{ color: String(color) }}>{value}</div>
          </div>
        ))}
      </div>

      <Card title="Action Timeline">
        <ActionTimeline history={history} />
      </Card>
    </div>
  );
}
