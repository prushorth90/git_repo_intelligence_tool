import type { ReactNode } from 'react'

type ChartPanelProps = {
  title: string
  description: string
  children: ReactNode
  action?: ReactNode
  className?: string
}

export function ChartPanel({ title, description, children, action, className = '' }: ChartPanelProps) {
  return (
    <section className={`chart-panel ${className}`}>
      <header className="panel-header">
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        {action}
      </header>
      <div className="chart-panel__body">{children}</div>
    </section>
  )
}