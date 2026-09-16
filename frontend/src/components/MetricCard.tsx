import type { LucideIcon } from 'lucide-react'

type MetricCardProps = {
  icon: LucideIcon
  label: string
  value: string
  detail: string
  tone?: 'default' | 'positive' | 'warning'
}

export function MetricCard({ icon: Icon, label, value, detail, tone = 'default' }: MetricCardProps) {
  return (
    <article className={`metric-card metric-card--${tone}`}>
      <div className="metric-card__header">
        <span>{label}</span>
        <Icon size={17} />
      </div>
      <strong>{value}</strong>
      <p>{detail}</p>
    </article>
  )
}