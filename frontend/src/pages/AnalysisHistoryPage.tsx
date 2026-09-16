import { CheckCircle2, Clock3, RotateCcw } from 'lucide-react'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useRepository } from '../context/RepositoryContext'
import { analysisHistory } from '../data/mockData'

type AnalysisRun = (typeof analysisHistory)[number]
const columns: TableColumn<AnalysisRun>[] = [
  { key: 'id', header: 'Run', render: (row) => <span className="code-path">{row.id}</span> },
  { key: 'started', header: 'Started', render: (row) => row.started },
  { key: 'trigger', header: 'Trigger', render: (row) => row.trigger },
  { key: 'commit', header: 'Commit', render: (row) => <span className="code-path">{row.commit}</span> },
  { key: 'duration', header: 'Duration', align: 'right', render: (row) => row.duration },
  { key: 'status', header: 'Status', align: 'right', render: (row) => <StatusBadge tone={row.status === 'Completed' ? 'positive' : 'danger'}>{row.status}</StatusBadge> },
]

export function AnalysisHistoryPage() {
  const { repository } = useRepository()
  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Runs`} title="Analysis history" description="Audit repository analysis runs, triggers, outcomes, and processing duration." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={CheckCircle2} label="Success rate" value="96.8%" detail="30 of the last 31 runs" tone="positive" />
        <MetricCard icon={Clock3} label="Avg. duration" value="4m 24s" detail="32 seconds faster this month" />
        <MetricCard icon={RotateCcw} label="Runs this month" value="31" detail="21 push · 7 PR · 3 manual" />
      </section>
      <section className="table-panel"><div className="panel-header"><div><h2>Recent runs</h2><p>Analysis activity for {repository.organization}/{repository.name}</p></div><span className="mono-meta">Retention: 90 days</span></div><DataTable columns={columns} getRowKey={(row) => row.id} rows={analysisHistory} /></section>
    </div>
  )
}