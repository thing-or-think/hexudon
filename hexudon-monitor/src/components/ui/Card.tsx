import type { ReactNode, CSSProperties } from 'react';

interface CardProps {
  title?: string;
  children: ReactNode;
  className?: string;
  action?: ReactNode;
  style?: CSSProperties;
}

export function Card({ title, children, className = '', action, style }: CardProps) {
  return (
    <div className={`card ${className}`} style={style}>
      {(title || action) && (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
          {title && <div className="card-title">{title}</div>}
          {action && <div>{action}</div>}
        </div>
      )}
      {children}
    </div>
  );
}

interface StatCardProps {
  label: string;
  value: string | number;
  sub?: string;
  color?: string;
  icon?: ReactNode;
}

export function StatCard({ label, value, sub, color, icon }: StatCardProps) {
  return (
    <div className="card" style={{ position: 'relative', overflow: 'hidden' }}>
      {icon && (
        <div style={{ position: 'absolute', top: '0.875rem', right: '0.875rem', opacity: 0.2, fontSize: '1.5rem' }}>
          {icon}
        </div>
      )}
      <div className="card-title">{label}</div>
      <div className="stat-value" style={{ color: color }}>{value}</div>
      {sub && <div className="stat-label">{sub}</div>}
    </div>
  );
}
