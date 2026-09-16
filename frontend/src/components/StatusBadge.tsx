type StatusBadgeProps = {
  children: string
  tone?: 'positive' | 'warning' | 'danger' | 'neutral'
}

export function StatusBadge({ children, tone = 'neutral' }: StatusBadgeProps) {
  return <span className={`status-badge status-badge--${tone}`}><span />{children}</span>
}