import { Activity, Flame, ShieldAlert } from 'lucide-react'
import { useEffect, useState } from 'react'
import { repositoryApi } from '../api/client'
import type { FileHotspotResponse, HotspotOverviewResponse } from '../api/types'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { WorkspaceState } from '../components/WorkspaceState'
import { useRepository } from '../context/RepositoryContext'

const columns: TableColumn<FileHotspotResponse>[] = [
  { key: 'file', header: 'File', render: (row) => <div className="hotspot-file"><span className="code-path" title={row.filePath}>{row.filePath}</span><small>{row.language}</small></div> },
  { key: 'risk', header: 'Risk', render: (row) => <span className={`risk-badge risk-badge--${row.riskLevel.toLowerCase()}`}>{row.riskLevel}</span> },
  { key: 'score', header: 'Score', align: 'right', render: (row) => <div className="risk-score"><strong>{row.riskScore.toFixed(1)}</strong><span><i style={{ width: `${row.riskScore}%` }} /></span></div> },
  { key: 'reasons', header: 'Why this score', render: (row) => <ul className="risk-reasons">{row.reasons.map((reason) => <li key={reason}>{reason}</li>)}</ul> },
  { key: 'factors', header: 'Factor points', render: (row) => <div className="factor-points">{row.factors.map((factor) => <span key={factor.key} title={`${factor.explanation}; normalized ${factor.normalizedValue.toFixed(3)}; weight ${(factor.weight * 100).toFixed(0)}%`}><b>{factor.label}</b>{factor.contribution.toFixed(1)}</span>)}</div> },
]

type HotspotResult = {
  repositoryId: string
  state: 'loading' | 'success' | 'error'
  data: HotspotOverviewResponse | null
  error: string
}

export function HotspotsPage() {
  const { repository } = useRepository()
  const [reloadToken, setReloadToken] = useState(0)
  const [result, setResult] = useState<HotspotResult>({ repositoryId: '', state: 'loading', data: null, error: '' })

  useEffect(() => {
    if (!repository) return
    const controller = new AbortController()
    repositoryApi.hotspots(repository.id, controller.signal)
      .then((data) => setResult({ repositoryId: repository.id, state: 'success', data, error: '' }))
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({ repositoryId: repository.id, state: 'error', data: null, error: requestError instanceof Error ? requestError.message : 'Risk rankings could not be loaded.' })
      })
    return () => controller.abort()
  }, [repository, reloadToken])

  if (!repository) return null
  const current = result.repositoryId === repository.id ? result : { ...result, state: 'loading' as const, data: null }
  if (current.state === 'loading') return <WorkspaceState state="loading" />
  if (current.state === 'error') return <WorkspaceState message={current.error} onRetry={() => { setResult({ repositoryId: repository.id, state: 'loading', data: null, error: '' }); setReloadToken((value) => value + 1) }} state="error" />

  const data = current.data ?? { analysisId: null, analyzedAt: null, formula: '', normalization: '', thresholds: '', files: [] }
  const criticalFiles = data.files.filter((file) => file.riskLevel === 'CRITICAL').length
  const elevatedFiles = data.files.filter((file) => file.riskLevel === 'CRITICAL' || file.riskLevel === 'HIGH').length
  const averageRisk = data.files.length === 0 ? 0 : data.files.reduce((sum, file) => sum + file.riskScore, 0) / data.files.length

  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Risk`} title="Repository hotspots" description="Files ranked by normalized engineering risk signals from source structure and Git history." />

      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={ShieldAlert} label="Critical files" value={criticalFiles.toString()} detail="Scores of 75 or higher" tone={criticalFiles > 0 ? 'warning' : 'default'} />
        <MetricCard icon={Flame} label="High-risk files" value={elevatedFiles.toString()} detail="High and critical classifications" tone={elevatedFiles > 0 ? 'warning' : 'default'} />
        <MetricCard icon={Activity} label="Average risk" value={averageRisk.toFixed(1)} detail={`${data.files.length} files ranked`} />
      </section>

      <section className="hotspot-model">
        <div><span className="eyebrow">Deterministic model</span><h2>{data.formula}</h2></div>
        <div><strong>Normalization</strong><p>{data.normalization}</p></div>
        <div><strong>Risk bands</strong><p>{data.thresholds}</p></div>
      </section>

      {data.files.length === 0 ? (
        <section className="hotspot-empty"><Flame size={22} /><h2>No hotspot scores yet</h2><p>Run an analysis with <strong>Include Git history</strong> enabled to calculate repository risk.</p></section>
      ) : (
        <section className="table-panel hotspot-ranking"><div className="panel-header"><div><h2>Highest-risk files</h2><p>{data.analyzedAt ? `Analyzed ${new Date(data.analyzedAt).toLocaleString()}` : 'Latest history-enabled analysis'}</p></div><span className="panel-note">Hover factor points for raw and normalized values</span></div><DataTable columns={columns} getRowKey={(row) => row.filePath} rows={data.files} /></section>
      )}
    </div>
  )
}
