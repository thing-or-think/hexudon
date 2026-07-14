import { Play, Pause, SkipForward, RotateCcw } from 'lucide-react';
import { useMatchStore } from '../stores/useMatchStore';

export function SimulationControls({ onRefresh }: { onRefresh: () => void }) {
  const { isSimulating, setSimulating, simulationSpeed, setSimulationSpeed, state } = useMatchStore();

  return (
    <div className="sim-controls">
      <span style={{ fontSize: '0.7rem', fontWeight: 700, color: 'var(--text-muted)', letterSpacing: '0.1em', textTransform: 'uppercase' }}>
        Simulation
      </span>

      <button
        className={`btn ${isSimulating ? 'btn-danger' : 'btn-success'}`}
        onClick={() => setSimulating(!isSimulating)}
      >
        {isSimulating ? <Pause size={13} /> : <Play size={13} />}
        {isSimulating ? 'Pause' : 'Play'}
      </button>

      <button className="btn btn-ghost" onClick={onRefresh}>
        <SkipForward size={13} />
        Next
      </button>

      <button className="btn btn-ghost" onClick={() => setSimulating(false)}>
        <RotateCcw size={13} />
        Reset
      </button>

      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginLeft: 'auto' }}>
        <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Speed</span>
        <select
          className="select"
          value={simulationSpeed}
          onChange={(e) => setSimulationSpeed(Number(e.target.value))}
          style={{ fontSize: '0.72rem', padding: '0.25rem 0.5rem' }}
        >
          <option value={500}>0.5s</option>
          <option value={1000}>1s</option>
          <option value={2000}>2s</option>
          <option value={5000}>5s</option>
        </select>
      </div>

      {state && (
        <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)', fontFamily: 'JetBrains Mono, monospace', marginLeft: '0.5rem' }}>
          Turn <span style={{ color: 'var(--accent-cyan)', fontWeight: 600 }}>{state.turn}</span> / 50
        </div>
      )}
    </div>
  );
}
