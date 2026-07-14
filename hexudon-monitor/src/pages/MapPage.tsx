import { useMatchConfig } from '../hooks/useMatchConfig';
import { useMatchState } from '../hooks/useMatchState';
import { HexGridViewer } from '../components/HexGridViewer';
import { Card } from '../components/ui/Card';
import { PageLoader } from '../components/ui/LoadingSpinner';
import { MOCK_AGENT_TEAMS } from '../mock/agent.mock';

export function MapPage() {
  const { config, isLoading } = useMatchConfig();
  const { state } = useMatchState();

  if (isLoading) return <PageLoader />;

  return (
    <div className="page-body">
      <h2 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '1rem' }}>Hex Map Viewer</h2>

      {config && (
        <div className="grid-4" style={{ marginBottom: '1rem' }}>
          {[['Map Size', `${config.mapWidth} × ${config.mapHeight}`], ['Cells', config.cells.length], ['Spots', config.spots.length], ['Max Fuel', config.maxFuel]].map(([k, v]) => (
            <div key={String(k)} className="card">
              <div className="card-title">{k}</div>
              <div className="stat-value" style={{ fontSize: '1.25rem', color: 'var(--accent-cyan)' }}>{v}</div>
            </div>
          ))}
        </div>
      )}

      <Card title="Full Hex Grid">
        {config && state ? (
          <HexGridViewer
            cells={config.cells}
            spots={state.spots}
            traffic={state.traffic}
            agents={state.agents}
            agentTeams={MOCK_AGENT_TEAMS}
            width={config.mapWidth}
            height={config.mapHeight}
          />
        ) : (
          <PageLoader />
        )}
      </Card>
    </div>
  );
}
