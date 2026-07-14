import { useMatchState } from '../hooks/useMatchState';
import { Card } from '../components/ui/Card';
import { PageLoader } from '../components/ui/LoadingSpinner';
import { rankSuffix, formatMs } from '../utils/formatters';
import { MOCK_AGENT_TEAMS } from '../mock/agent.mock';

export function TeamsPage() {
  const { state } = useMatchState();
  if (!state) return <PageLoader />;

  const sorted = [...state.teamScores].sort((a, b) => b.totalUdonServings - a.totalUdonServings);

  // Build agent counts per team
  const agentCountByTeam: Record<string, number> = {};
  state.agents.forEach((a) => {
    const team = MOCK_AGENT_TEAMS[a.agentId] ?? 'Unknown';
    agentCountByTeam[team] = (agentCountByTeam[team] ?? 0) + 1;
  });

  return (
    <div className="page-body">
      <h2 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Team Management</h2>

      {/* Team Cards */}
      <div className="grid-2" style={{ marginBottom: '1.5rem' }}>
        {sorted.map((score, i) => {
          const rank = i + 1;
          const pctServings = sorted[0].totalUdonServings > 0
            ? (score.totalUdonServings / sorted[0].totalUdonServings) * 100
            : 0;
          return (
            <div key={score.teamName} className="card" style={{ borderLeft: rank === 1 ? '3px solid var(--accent-green)' : '3px solid var(--border-subtle)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div>
                  <div style={{ fontSize: '1.25rem', marginBottom: '0.25rem' }}>{rankSuffix(rank)}</div>
                  <div style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)' }}>{score.teamName}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--accent-green)', fontFamily: 'JetBrains Mono, monospace' }}>{score.totalUdonServings}</div>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Servings</div>
                </div>
              </div>
              <div className="progress-bar" style={{ marginBottom: '0.75rem', height: 6 }}>
                <div className="progress-fill" style={{ width: `${pctServings}%`, background: rank === 1 ? 'var(--accent-green)' : 'var(--accent-blue)' }} />
              </div>
              <div className="grid-2" style={{ gap: '0.5rem' }}>
                {[
                  ['Agents', agentCountByTeam[score.teamName] ?? 0],
                  ['Daily Udon', score.totalDailyUdon],
                  ['Unique Types', score.uniqueUdonTypeCount],
                  ['Avg Response', formatMs(score.totalResponseTimeMillis)],
                ].map(([label, value]) => (
                  <div key={String(label)} style={{ background: 'rgba(255,255,255,0.02)', borderRadius: 6, padding: '0.4rem 0.625rem' }}>
                    <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>{label}</div>
                    <div style={{ fontWeight: 700, fontFamily: 'JetBrains Mono, monospace', color: 'var(--text-primary)', fontSize: '0.9rem' }}>{value}</div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      {/* Ranking Table */}
      <Card title="Ranking Table">
        <div style={{ overflowX: 'auto' }}>
          <table className="data-table">
            <thead>
              <tr><th>Rank</th><th>Team</th><th>Servings</th><th>Daily Udon</th><th>Unique Types</th><th>Response Time</th></tr>
            </thead>
            <tbody>
              {sorted.map((score, i) => (
                <tr key={score.teamName}>
                  <td><span className={i < 3 ? `rank-${i + 1}` : ''} style={{ fontWeight: 700 }}>{rankSuffix(i + 1)}</span></td>
                  <td style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{score.teamName}</td>
                  <td style={{ color: 'var(--accent-green)', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace' }}>{score.totalUdonServings}</td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace' }}>{score.totalDailyUdon}</td>
                  <td>{score.uniqueUdonTypeCount}</td>
                  <td>{formatMs(score.totalResponseTimeMillis)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
