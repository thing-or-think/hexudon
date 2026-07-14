import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Users, Map, Cpu, Activity, BarChart2,
  Wifi, WifiOff
} from 'lucide-react';
import { useMatchStore } from '../stores/useMatchStore';

const NAV_ITEMS = [
  { to: '/dashboard', icon: <LayoutDashboard size={15} />, label: 'Dashboard' },
  { to: '/map', icon: <Map size={15} />, label: 'Hex Map' },
  { to: '/agents', icon: <Cpu size={15} />, label: 'Agents' },
  { to: '/teams', icon: <Users size={15} />, label: 'Teams' },
  { to: '/actions', icon: <Activity size={15} />, label: 'Actions' },
  { to: '/traffic', icon: <Activity size={15} />, label: 'Traffic' },
  { to: '/score', icon: <BarChart2 size={15} />, label: 'Score' },
];

export function Sidebar() {
  const { useMockData, toggleMockData, state } = useMatchStore();

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h1>HEXUDON</h1>
        <p>Match Monitor</p>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-section-label">Navigation</div>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
          >
            {item.icon}
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div style={{ padding: '0.75rem', borderTop: '1px solid var(--border-subtle)' }}>
        {state && (
          <div style={{ marginBottom: '0.625rem', padding: '0.5rem', background: 'rgba(255,255,255,0.02)', borderRadius: '6px', fontSize: '0.72rem' }}>
            <div style={{ color: 'var(--text-muted)', marginBottom: '0.25rem' }}>Match Status</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
              <span
                className="status-dot"
                style={{
                  background: state.status === 'PLAYING' ? 'var(--accent-green)' :
                    state.status === 'WAITING' ? 'var(--accent-yellow)' : 'var(--text-muted)'
                }}
              />
              <span style={{ color: 'var(--text-primary)', fontWeight: 600, fontFamily: 'JetBrains Mono, monospace' }}>
                {state.status}
              </span>
            </div>
            <div style={{ color: 'var(--text-muted)', marginTop: '0.25rem', fontFamily: 'JetBrains Mono, monospace' }}>
              Turn {state.turn}
            </div>
          </div>
        )}

        <button
          className={`btn ${useMockData ? 'btn-ghost' : 'btn-primary'}`}
          style={{ width: '100%', justifyContent: 'center', fontSize: '0.72rem' }}
          onClick={toggleMockData}
        >
          {useMockData ? <WifiOff size={12} /> : <Wifi size={12} />}
          {useMockData ? 'Mock Mode' : 'Live Mode'}
        </button>
      </div>
    </aside>
  );
}
