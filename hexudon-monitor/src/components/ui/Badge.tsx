import type { TrafficLevel, MatchStatus, AgentType } from '../../models/match';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'purple';
  dot?: boolean;
}

export function Badge({ children, variant = 'neutral', dot }: BadgeProps) {
  return (
    <span className={`badge badge-${variant}`}>
      {dot && <span className="status-dot" style={{ width: 5, height: 5, background: 'currentColor', borderRadius: '50%', flexShrink: 0 }} />}
      {children}
    </span>
  );
}

export function TrafficBadge({ level }: { level: TrafficLevel }) {
  const variant = level === 'NORMAL' ? 'success' : level === 'BUSY' ? 'warning' : 'danger';
  return <Badge variant={variant}>{level}</Badge>;
}

export function StatusBadge({ status }: { status: MatchStatus }) {
  const variant = status === 'PLAYING' ? 'success' : status === 'WAITING' ? 'warning' : 'neutral';
  return (
    <span className={`badge badge-${variant}`}>
      {status === 'PLAYING' && <span className="status-dot playing" />}
      {status}
    </span>
  );
}

export function AgentTypeBadge({ type }: { type: AgentType }) {
  return (
    <Badge variant={type === 'PATROL' ? 'info' : 'warning'}>
      {type === 'PATROL' ? '🔵' : '🟡'} {type}
    </Badge>
  );
}
