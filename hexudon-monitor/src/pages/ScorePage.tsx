import { useMatchState } from '../hooks/useMatchState';
import { ScoreBoard } from '../components/ScoreBoard';
import { Card } from '../components/ui/Card';
import { PageLoader } from '../components/ui/LoadingSpinner';
import { MOCK_SCORE_HISTORY } from '../mock/match.mock';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

const LINE_COLORS = ['#22c55e', '#3b82f6', '#a855f7', '#f97316'];

export function ScorePage() {
  const { state } = useMatchState();
  if (!state) return <PageLoader />;

  const teamNames = state.teamScores.map((t) => t.teamName);

  return (
    <div className="page-body">
      <h2 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Score Dashboard</h2>

      <Card title="Scoreboard" style={{ marginBottom: '1rem' }}>
        <ScoreBoard scores={state.teamScores} />
      </Card>

      <Card title="Score Over Time (Mock)">
        <ResponsiveContainer width="100%" height={240}>
          <LineChart data={MOCK_SCORE_HISTORY} margin={{ top: 8, right: 16, bottom: 0, left: -20 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border-subtle)" />
            <XAxis dataKey="turn" stroke="var(--text-muted)" tick={{ fontSize: 10 }} label={{ value: 'Turn', position: 'insideBottomRight', offset: -5, fontSize: 10, fill: 'var(--text-muted)' }} />
            <YAxis stroke="var(--text-muted)" tick={{ fontSize: 10 }} />
            <Tooltip
              contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border-accent)', borderRadius: 8, fontSize: 12 }}
              labelStyle={{ color: 'var(--text-muted)' }}
            />
            <Legend wrapperStyle={{ fontSize: 11, color: 'var(--text-muted)' }} />
            {teamNames.map((name, i) => (
              <Line
                key={name}
                type="monotone"
                dataKey={name}
                stroke={LINE_COLORS[i % LINE_COLORS.length]}
                strokeWidth={2}
                dot={false}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </Card>
    </div>
  );
}
