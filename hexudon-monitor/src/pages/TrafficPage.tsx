import { useMatchConfig } from '../hooks/useMatchConfig';
import { useMatchState } from '../hooks/useMatchState';
import { TrafficHeatMap } from '../components/TrafficHeatMap';
import { Card } from '../components/ui/Card';
import { PageLoader } from '../components/ui/LoadingSpinner';
import { MOCK_TRAFFIC_HISTORY } from '../mock/traffic.mock';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

export function TrafficPage() {
  const { config } = useMatchConfig();
  const { state } = useMatchState();

  if (!state) return <PageLoader />;

  return (
    <div className="page-body">
      <h2 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Traffic System</h2>

      <Card title="Traffic Heatmap" style={{ marginBottom: '1rem' }}>
        <TrafficHeatMap
          traffic={state.traffic}
          cells={config?.cells ?? []}
        />
      </Card>

      <Card title="Traffic History (Mock)">
        <ResponsiveContainer width="100%" height={220}>
          <LineChart data={MOCK_TRAFFIC_HISTORY} margin={{ top: 8, right: 16, bottom: 0, left: -20 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border-subtle)" />
            <XAxis dataKey="turn" stroke="var(--text-muted)" tick={{ fontSize: 10 }} label={{ value: 'Turn', position: 'insideBottomRight', offset: -5, fontSize: 10, fill: 'var(--text-muted)' }} />
            <YAxis stroke="var(--text-muted)" tick={{ fontSize: 10 }} />
            <Tooltip
              contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border-accent)', borderRadius: 8, fontSize: 12 }}
              labelStyle={{ color: 'var(--text-muted)' }}
            />
            <Legend wrapperStyle={{ fontSize: 11, color: 'var(--text-muted)' }} />
            <Line type="monotone" dataKey="NORMAL" stroke="#22c55e" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="BUSY" stroke="#eab308" strokeWidth={2} dot={false} />
            <Line type="monotone" dataKey="CONGESTED" stroke="#ef4444" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </Card>
    </div>
  );
}
