import { FileWarning, UserRoundCheck, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { repositoryApi } from '../api/client'
import type { ConcentratedFileResponse, ContributorOverviewResponse, ContributorOwnershipResponse } from '../api/types'
import { ChartPanel } from '../components/ChartPanel'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { WorkspaceState } from '../components/WorkspaceState'
import { useRepository } from '../context/RepositoryContext'

const contributorColumns: TableColumn<ContributorOwnershipResponse>[] = [
  { key: 'name', header: 'Contributor', render: (row) => <div className="person-cell"><span>{row.displayName.split(' ').map((part) => part[0]).join('').slice(0, 2)}</span><div><strong>{row.displayName}</strong><small>{row.contributorKey}</small></div></div> },
  { key: 'modules', header: 'Primary modules', render: (row) => <div className="module-list">{row.primaryModules.map((module) => <span key={module}>{module}</span>)}</div> },
  { key: 'commits', header: 'Commits', align: 'right', render: (row) => row.totalCommits.toLocaleString() },
  { key: 'files', header: 'Files touched', align: 'right', render: (row) => row.filesTouched.toLocaleString() },
  { key: 'changes', header: 'Lines changed', align: 'right', render: (row) => (row.additions + row.deletions).toLocaleString() },
  { key: 'activity', header: 'Recent activity', render: (row) => new Date(row.lastActivityAt).toLocaleDateString() },
  { key: 'ownership', header: 'Est. ownership', align: 'right', render: (row) => <strong className="score-cell">{row.estimatedOwnershipPercent.toFixed(1)}%</strong> },
]

const concentrationColumns: TableColumn<ConcentratedFileResponse>[] = [
  { key: 'path', header: 'File', render: (row) => <span className="code-path">{row.filePath}</span> },
  { key: 'owner', header: 'Primary owner', render: (row) => row.topContributorName },
  { key: 'ownership', header: 'Ownership', align: 'right', render: (row) => `${row.topOwnershipPercent.toFixed(1)}%` },
  { key: 'bus', header: 'Bus factor', align: 'right', render: (row) => <StatusBadge tone="danger">{row.busFactor.toString()}</StatusBadge> },
  { key: 'churn', header: 'Churn', align: 'right', render: (row) => row.totalChurn.toLocaleString() },
]

type OwnershipResult = {
  repositoryId: string
  state: 'loading' | 'success' | 'error'
  data: ContributorOverviewResponse | null
  error: string
}

export function ContributorsPage() {
  const { repository } = useRepository()
  const [reloadToken, setReloadToken] = useState(0)
  const [result, setResult] = useState<OwnershipResult>({ repositoryId: '', state: 'loading', data: null, error: '' })

  useEffect(() => {
    if (!repository) return
    const controller = new AbortController()
    repositoryApi.contributorOwnership(repository.id, controller.signal)
      .then((data) => setResult({ repositoryId: repository.id, state: 'success', data, error: '' }))
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({ repositoryId: repository.id, state: 'error', data: null, error: requestError instanceof Error ? requestError.message : 'Contributor ownership could not be loaded.' })
      })
    return () => controller.abort()
  }, [repository, reloadToken])

  if (!repository) return null
  const current = result.repositoryId === repository.id ? result : { ...result, state: 'loading' as const, data: null }
  if (current.state === 'loading') return <WorkspaceState state="loading" />
  if (current.state === 'error') return <WorkspaceState message={current.error} onRetry={() => { setResult({ repositoryId: repository.id, state: 'loading', data: null, error: '' }); setReloadToken((value) => value + 1) }} state="error" />

  const data = current.data ?? { analysisId: null, analyzedAt: null, repositoryBusFactor: 0, concentratedFileCount: 0, contributors: [], concentratedFiles: [] }
  const totalCommits = data.contributors.reduce((sum, contributor) => sum + contributor.totalCommits, 0)

  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Ownership`} title="Contributors" description="Weighted ownership combines code churn, file touches, and recent activity instead of relying on commit count alone." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={Users} label="Contributors" value={data.contributors.length.toString()} detail="Authors in latest history analysis" />
        <MetricCard icon={UserRoundCheck} label="Repository bus factor" value={data.repositoryBusFactor === 0 ? '—' : data.repositoryBusFactor.toString()} detail="Contributors exceeding 50% ownership" tone={data.repositoryBusFactor > 1 ? 'positive' : 'warning'} />
        <MetricCard icon={FileWarning} label="Concentrated files" value={data.concentratedFileCount.toString()} detail="One owner controls at least 70%" tone={data.concentratedFileCount > 0 ? 'warning' : 'positive'} />
      </section>

      {data.contributors.length === 0 ? (
        <section className="hotspot-empty"><Users size={22} /><h2>No ownership metrics yet</h2><p>Run an analysis with <strong>Include Git history</strong> enabled to calculate ownership.</p></section>
      ) : (
        <>
          <ChartPanel title="Estimated ownership" description="Weighted share across files, churn, touches, and recency">
            <div className="chart-container chart-container--large"><ResponsiveContainer width="100%" height="100%"><BarChart data={data.contributors.slice(0, 12)} layout="vertical" margin={{ top: 8, right: 28, bottom: 8, left: 16 }}><CartesianGrid horizontal={false} stroke="#e3e5df" strokeDasharray="3 3" /><XAxis type="number" domain={[0, 100]} tick={{ fill: '#737d76', fontSize: 11 }} unit="%" /><YAxis dataKey="displayName" type="category" width={110} tick={{ fill: '#47514b', fontSize: 10 }} /><Tooltip formatter={(value) => `${Number(value).toFixed(1)}%`} /><Bar dataKey="estimatedOwnershipPercent" fill="#207355" radius={[0, 2, 2, 0]} /></BarChart></ResponsiveContainer></div>
          </ChartPanel>
          <section className="table-panel"><div className="panel-header"><div><h2>Contributor ownership</h2><p>{totalCommits.toLocaleString()} unique commits across source files</p></div><span className="mono-meta">{data.analyzedAt ? `Analyzed ${new Date(data.analyzedAt).toLocaleDateString()}` : ''}</span></div><DataTable columns={contributorColumns} getRowKey={(row) => row.contributorKey} rows={data.contributors} /></section>
          <section className="table-panel"><div className="panel-header"><div><h2>Concentrated ownership</h2><p>Files where one contributor owns at least 70% of weighted changes</p></div></div><DataTable columns={concentrationColumns} emptyMessage="No highly concentrated files were detected." getRowKey={(row) => row.filePath} rows={data.concentratedFiles} /></section>
        </>
      )}
    </div>
  )
}