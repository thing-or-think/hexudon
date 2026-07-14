import type { TeamScoreResponse } from '../models/match';
import { rankSuffix } from '../utils/formatters';
import { formatMs } from '../utils/formatters';

interface ScoreBoardProps {
  scores: TeamScoreResponse[];
}

export function ScoreBoard({ scores }: ScoreBoardProps) {
  const sorted = [...scores].sort((a, b) => b.totalUdonServings - a.totalUdonServings);

  return (
    <div style={{ overflowX: 'auto' }}>
      <table className="data-table">
        <thead>
          <tr>
            <th>Rank</th>
            <th>Team</th>
            <th>Servings</th>
            <th>Daily Udon</th>
            <th>Unique Types</th>
            <th>Avg Response</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((score, i) => {
            const rank = i + 1;
            const avgMs = score.totalResponseTimeMillis > 0
              ? Math.round(score.totalResponseTimeMillis / Math.max(score.totalUdonServings, 1))
              : 0;
            return (
              <tr key={score.teamName}>
                <td>
                  <span className={rank <= 3 ? `rank-${rank}` : ''} style={{ fontWeight: 700, fontSize: '1rem' }}>
                    {rankSuffix(rank)}
                  </span>
                </td>
                <td style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{score.teamName}</td>
                <td style={{ color: 'var(--accent-green)', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace' }}>
                  {score.totalUdonServings}
                </td>
                <td style={{ fontFamily: 'JetBrains Mono, monospace' }}>{score.totalDailyUdon}</td>
                <td>
                  <div style={{ display: 'flex', gap: '0.25rem' }}>
                    {Array.from({ length: score.uniqueUdonTypeCount }, (_, j) => (
                      <span key={j} style={{ fontSize: '0.75rem' }}>
                        {['🍜', '🦊', '🦝', '🥩'][j] ?? '🍲'}
                      </span>
                    ))}
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginLeft: '0.25rem' }}>
                      ({score.uniqueUdonTypeCount})
                    </span>
                  </div>
                </td>
                <td style={{ color: avgMs > 3000 ? 'var(--accent-red)' : 'var(--text-secondary)' }}>
                  {formatMs(avgMs)}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
