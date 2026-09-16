import { CheckCircle2, Clock3, Play, RotateCcw, XCircle } from 'lucide-react'
import { useEffect, useState } from 'react'
import { repositoryApi } from '../api/client'
import type { AnalysisJobResponse, AnalysisJobStatus } from '../api/types'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { WorkspaceState } from '../components/WorkspaceState'
import { useRepository } from '../context/RepositoryContext'

function formatDuration(job: AnalysisJobResponse) {
  if (!job.startedAt) return 'Not started'
  const end = job.completedAt ? Date.parse(job.completedAt) : Date.now()
  const seconds = Math.max(0, Math.round((end - Date.parse(job.startedAt)) / 1000))
  return `${Math.floor(seconds / 60)}m ${seconds % 60}s`
}

function statusTone(status: AnalysisJobStatus) {
  if (status === 'COMPLETED') return 'positive' as const
  if (status === 'FAILED') return 'danger' as const
  if (status === 'CANCELLED') return 'neutral' as const
  return 'warning' as const
}

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
  const [commandState, setCommandState] = useState<'idle' | 'submitting' | 'error'>('idle')
  const [commandError, setCommandError] = useState('')
  const [includeHistory, setIncludeHistory] = useState(false)

  useEffect(() => {
    if (!repository) return
    const controller = new AbortController()
    let refreshTimer: number | undefined
    repositoryApi.analysisHistory(repository.id, controller.signal)
      .then((response) => {
        setResult({ repositoryId: repository.id, state: 'success', jobs: response, error: '' })
        if (response.some((job) => job.status === 'QUEUED' || job.status === 'RUNNING')) {
          refreshTimer = window.setTimeout(() => setReloadToken((value) => value + 1), 1500)
        }
      })
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({
          repositoryId: repository.id,
          state: 'error',
          jobs: [],
          error: requestError instanceof Error ? requestError.message : 'Analysis history could not be loaded.',
        })
      })
    return () => {
      controller.abort()
      if (refreshTimer) window.clearTimeout(refreshTimer)
    }
  }, [repository, reloadToken])

  if (!repository) return null
  const currentResult = result.repositoryId === repository.id ? result : { ...result, state: 'loading' as const, jobs: [] }
  if (currentResult.state === 'loading') return <WorkspaceState state="loading" />
  if (currentResult.state === 'error') return <WorkspaceState message={currentResult.error} onRetry={() => {
    setResult({ repositoryId: repository.id, state: 'loading', jobs: [], error: '' })
    setReloadToken((value) => value + 1)
  }} state="error" />

  const { jobs } = currentResult
  const activeJob = jobs.find((job) => job.status === 'QUEUED' || job.status === 'RUNNING')
  const completedJobs = jobs.filter((job) => job.status === 'COMPLETED')
  const successRate = jobs.length === 0 ? '—' : `${Math.round((completedJobs.length / jobs.length) * 100)}%`

  async function requestAnalysis() {
    if (!repository) return
    setCommandState('submitting')
    setCommandError('')
    try {
      const job = await repositoryApi.requestAnalysis(repository.id, includeHistory)
      setResult((current) => ({ ...current, jobs: [job, ...current.jobs] }))
      setCommandState('idle')
      setReloadToken((value) => value + 1)
    } catch (requestError) {
      setCommandError(requestError instanceof Error ? requestError.message : 'Analysis could not be queued.')
      setCommandState('error')
    }
  }

  async function cancel(jobId: string) {
    if (!repository) return
    setCommandError('')
    try {
      const cancelled = await repositoryApi.cancelAnalysis(repository.id, jobId)
      setResult((current) => ({ ...current, jobs: current.jobs.map((job) => job.id === jobId ? cancelled : job) }))
    } catch (requestError) {
      setCommandError(requestError instanceof Error ? requestError.message : 'Analysis could not be cancelled.')
      setCommandState('error')
    }
  }

  const columns: TableColumn<AnalysisJobResponse>[] = [
    { key: 'id', header: 'Job', render: (row) => <span className="code-path">{row.id.slice(0, 8)}</span> },
    { key: 'requested', header: 'Requested', render: (row) => new Date(row.requestedAt).toLocaleString() },
    { key: 'progress', header: 'Progress', render: (row) => <div className="job-progress"><div><i style={{ width: `${row.progressPercentage}%` }} /></div><span>{row.progressPercentage}%</span></div> },
    { key: 'mode', header: 'Mode', render: (row) => row.includeHistory ? 'Full history' : 'Shallow' },
    { key: 'retries', header: 'Retries', align: 'right', render: (row) => row.retryCount },
    { key: 'duration', header: 'Duration', align: 'right', render: formatDuration },
    { key: 'status', header: 'Status', align: 'right', render: (row) => <StatusBadge tone={statusTone(row.status)}>{row.status}</StatusBadge> },
    { key: 'cancel', header: '', align: 'right', render: (row) => (row.status === 'QUEUED' || row.status === 'RUNNING') ? <button className="table-icon-button" onClick={() => void cancel(row.id)} title="Cancel analysis" type="button"><XCircle size={15} /></button> : null },
  ]

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow={`${repository.name} / Runs`}
        title="Analysis history"
        description="Queue asynchronous repository analysis and monitor worker progress."
        action={<div className="analysis-action"><label><input checked={includeHistory} disabled={Boolean(activeJob)} onChange={(event) => setIncludeHistory(event.target.checked)} type="checkbox" /><span>Include Git history</span></label><button className="primary-button" disabled={Boolean(activeJob) || commandState === 'submitting'} onClick={() => void requestAnalysis()} type="button"><Play size={15} />{commandState === 'submitting' ? 'Queueing...' : activeJob ? 'Analysis active' : 'Analyze Repository'}</button></div>}
      />
      {commandError && <p className="command-error" role="alert">{commandError}</p>}
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={CheckCircle2} label="Success rate" value={successRate} detail={`${completedJobs.length} completed jobs`} tone="positive" />
        <MetricCard icon={Clock3} label="Latest status" value={jobs[0]?.status ?? 'None'} detail={jobs[0] ? new Date(jobs[0].requestedAt).toLocaleString() : 'No jobs recorded'} />
        <MetricCard icon={RotateCcw} label="Total jobs" value={jobs.length.toString()} detail="Persisted analysis requests" />
      </section>
      <section className="table-panel">
        <div className="panel-header"><div><h2>Analysis jobs</h2><p>Live worker state for {repository.fullName}</p></div><span className="mono-meta">Auto-refresh while active</span></div>
        <DataTable columns={columns} emptyMessage="No analysis jobs have been recorded for this repository." getRowKey={(row) => row.id} rows={jobs} />
        {jobs.find((job) => job.failureReason) && <div className="failure-summary"><strong>Latest failure</strong><span>{jobs.find((job) => job.failureReason)?.failureReason}</span></div>}
      </section>
    </div>
  )
}