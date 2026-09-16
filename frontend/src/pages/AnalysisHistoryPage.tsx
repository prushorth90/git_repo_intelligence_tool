import { CheckCircle2, Clock3, RotateCcw } from 'lucide-react'
import { useEffect, useState } from 'react'
import { repositoryApi } from '../api/client'
import type { AnalysisJobResponse } from '../api/types'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { WorkspaceState } from '../components/WorkspaceState'
import { useRepository } from '../context/RepositoryContext'

function formatDuration(job: AnalysisJobResponse) {
  if (!job.startedAt) return 'Not started'
  if (!job.completedAt) return 'In progress'
  const seconds = Math.max(0, Math.round((Date.parse(job.completedAt) - Date.parse(job.startedAt)) / 1000))
  return `${Math.floor(seconds / 60)}m ${seconds % 60}s`
}

const columns: TableColumn<AnalysisJobResponse>[] = [
  { key: 'id', header: 'Job', render: (row) => <span className="code-path">{row.id.slice(0, 8)}</span> },
  { key: 'requested', header: 'Requested', render: (row) => new Date(row.requestedAt).toLocaleString() },
  { key: 'started', header: 'Started', render: (row) => row.startedAt ? new Date(row.startedAt).toLocaleString() : 'Pending' },
  { key: 'duration', header: 'Duration', align: 'right', render: formatDuration },
  { key: 'status', header: 'Status', align: 'right', render: (row) => <StatusBadge tone={row.status === 'COMPLETED' ? 'positive' : row.status === 'FAILED' ? 'danger' : 'warning'}>{row.status}</StatusBadge> },
]

type HistoryResult = {
  repositoryId: string
  state: 'loading' | 'success' | 'error'
  jobs: AnalysisJobResponse[]
  error: string
}

export function AnalysisHistoryPage() {
  const { repository } = useRepository()
  const [result, setResult] = useState<HistoryResult>({ repositoryId: '', state: 'loading', jobs: [], error: '' })
  const [reloadToken, setReloadToken] = useState(0)

  useEffect(() => {
    if (!repository) return
    const controller = new AbortController()
    repositoryApi.analysisHistory(repository.id, controller.signal)
      .then((response) => setResult({ repositoryId: repository.id, state: 'success', jobs: response, error: '' }))
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({
          repositoryId: repository.id,
          state: 'error',
          jobs: [],
          error: requestError instanceof Error ? requestError.message : 'Analysis history could not be loaded.',
        })
      })
    return () => controller.abort()
  }, [repository, reloadToken])

  if (!repository) return null
  const currentResult = result.repositoryId === repository.id ? result : { ...result, state: 'loading' as const, jobs: [] }
  if (currentResult.state === 'loading') return <WorkspaceState state="loading" />
  if (currentResult.state === 'error') return <WorkspaceState message={currentResult.error} onRetry={() => {
    setResult({ repositoryId: repository.id, state: 'loading', jobs: [], error: '' })
    setReloadToken((value) => value + 1)
  }} state="error" />

  const { jobs } = currentResult

  const completedJobs = jobs.filter((job) => job.status === 'COMPLETED')
  const successRate = jobs.length === 0 ? '—' : `${Math.round((completedJobs.length / jobs.length) * 100)}%`

  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Runs`} title="Analysis history" description="Audit persisted analysis jobs, outcomes, and processing duration." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={CheckCircle2} label="Success rate" value={successRate} detail={`${completedJobs.length} completed jobs`} tone="positive" />
        <MetricCard icon={Clock3} label="Latest status" value={jobs[0]?.status ?? 'None'} detail={jobs[0] ? new Date(jobs[0].requestedAt).toLocaleString() : 'No jobs recorded'} />
        <MetricCard icon={RotateCcw} label="Total jobs" value={jobs.length.toString()} detail="Persisted analysis requests" />
      </section>
      <section className="table-panel">
        <div className="panel-header"><div><h2>Analysis jobs</h2><p>Live history for {repository.fullName}</p></div><span className="mono-meta">API connected</span></div>
        <DataTable columns={columns} emptyMessage="No analysis jobs have been recorded for this repository." getRowKey={(row) => row.id} rows={jobs} />
      </section>
    </div>
  )
}