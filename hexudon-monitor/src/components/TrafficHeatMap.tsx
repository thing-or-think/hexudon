import type { TrafficResponse, CellResponse } from '../models/match';
import { TrafficBadge } from './ui/Badge';

interface TrafficHeatMapProps {
  traffic: TrafficResponse[];
  cells: CellResponse[];
}

const LEVEL_ORDER = ['NORMAL', 'BUSY', 'CONGESTED'] as const;

export function TrafficHeatMap({ traffic }: TrafficHeatMapProps) {
  const counts = {
    NORMAL: traffic.filter((t) => t.trafficLevel === 'NORMAL').length,
    BUSY: traffic.filter((t) => t.trafficLevel === 'BUSY').length,
    CONGESTED: traffic.filter((t) => t.trafficLevel === 'CONGESTED').length,
  };
  const total = traffic.length || 1;

  return (
    <div style={{ display: 'grid', gap: '1rem' }}>
      {/* Summary */}
      <div className="grid-3" style={{ gap: '0.75rem' }}>
        {LEVEL_ORDER.map((level) => {
          const pct = Math.round((counts[level] / total) * 100);
          const colors: Record<string, string> = {
            NORMAL: 'var(--accent-green)',
            BUSY: 'var(--accent-yellow)',
            CONGESTED: 'var(--accent-red)',
          };
          return (
            <div key={level} className="card" style={{ textAlign: 'center' }}>
              <div className="card-title">{level}</div>
              <div className="stat-value" style={{ color: colors[level], fontSize: '1.75rem' }}>
                {counts[level]}
              </div>
              <div style={{ marginTop: '0.5rem' }}>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: `${pct}%`, background: colors[level] }} />
                </div>
                <div className="stat-label">{pct}% of cells</div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Table */}
      <div style={{ overflowX: 'auto', maxHeight: 300, overflowY: 'auto' }}>
        <table className="data-table">
          <thead>
            <tr><th>X</th><th>Y</th><th>Traffic Level</th></tr>
          </thead>
          <tbody>
            {traffic.map((t) => (
              <tr key={`${t.coordinate.x}-${t.coordinate.y}`}>
                <td>{t.coordinate.x}</td>
                <td>{t.coordinate.y}</td>
                <td><TrafficBadge level={t.trafficLevel} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
