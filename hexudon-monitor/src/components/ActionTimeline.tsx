import type { ActionHistoryEntry } from '../models/match';

function statusStyle(status: ActionHistoryEntry['status']): string {
  switch (status) {
    case 'SUCCESS': return 'badge-success';
    case 'FAILED': return 'badge-danger';
    case 'REJECTED': return 'badge-warning';
    default: return 'badge-neutral';
  }
}

interface ActionTimelineProps {
  history: ActionHistoryEntry[];
}

export function ActionTimeline({ history }: ActionTimelineProps) {
  // Group by turn
  const byTurn = history.reduce<Record<number, ActionHistoryEntry[]>>((acc, entry) => {
    acc[entry.turn] = acc[entry.turn] ?? [];
    acc[entry.turn].push(entry);
    return acc;
  }, {});

  const turns = Object.keys(byTurn)
    .map(Number)
    .sort((a, b) => b - a); // newest first

  if (turns.length === 0) {
    return (
      <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', padding: '2rem', textAlign: 'center' }}>
        No action history available.
      </div>
    );
  }

  return (
    <div>
      {turns.map((turn) => (
        <div key={turn} className="timeline-turn">
          <div className="timeline-turn-label">⟳ Turn {turn}</div>
          {byTurn[turn].map((entry, i) => (
            <div key={i} className="timeline-event">
              <span style={{ color: 'var(--accent-cyan)', fontWeight: 600 }}>{entry.agentId}</span>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem' }}>{entry.teamName}</span>
              <span style={{ color: 'var(--text-primary)' }}>{entry.actionType}</span>
              {entry.toCoordinate && (
                <span style={{ color: 'var(--text-muted)' }}>
                  → ({entry.toCoordinate.x},{entry.toCoordinate.y})
                </span>
              )}
              <span className={`badge ${statusStyle(entry.status)}`} style={{ marginLeft: 'auto', fontSize: '0.6rem' }}>
                {entry.status}
              </span>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
