import { Flame, GitCommitHorizontal, ShieldAlert } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { repositoryApi } from '../api/client'
import type { CodeChurnRankingResponse, FileChurnResponse, HistoryPeriod } from '../api/types'
import { ChartPanel } from '../components/ChartPanel'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { WorkspaceState } from '../components/WorkspaceState'
import { useRepository } from '../context/RepositoryContext'

const periods: Array<{ value: HistoryPeriod; label: string }> = [
  { value: 'DAYS_30', label: '30 days' },
  { value: 'DAYS_90', label: '90 days' },
  { value: 'MONTHS_6', label: '6 months' },
  { value: 'ALL', label: 'All time' },
]

const columns: TableColumn<FileChurnResponse>[] = [
  { key: 'path', header: 'File path', render: (row) => <span className="code-path">{row.filePath}</span> },
  { key: 'language', header: 'Language', render: (row) => row.language },
  { key: 'commits', header: 'Commits', align: 'right', render: (row) => row.commitCount },
  { key: 'additions', header: 'Added', align: 'right', render: (row) => `+${row.additions.toLocaleString()}` },
  { key: 'deletions', header: 'Removed', align: 'right', render: (row) => `-${row.deletions.toLocaleString()}` },
  { key: 'contributors', header: 'Contributors', align: 'right', render: (row) => row.uniqueContributors },
  { key: 'modified', header: 'Last modified', render: (row) => new Date(row.lastModifiedAt).toLocaleDateString() },
  { key: 'churn', header: 'Total churn', align: 'right', render: (row) => <strong className="score-cell">{row.totalChurn.toLocaleString()}</strong> },
]

type ChurnResult = {
  repositoryId: string
  period: HistoryPeriod
  state: 'loading' | 'success' | 'error'
  data: CodeChurnRankingResponse | null
  error: string
}

export function HotspotsPage() {
  const { repository } = useRepository()
  const [period, setPeriod] = useState<HistoryPeriod>('DAYS_90')
  const [reloadToken, setReloadToken] = useState(0)
  const [result, setResult] = useState<ChurnResult>({ repositoryId: '', period, state: 'loading', data: null, error: '' })

  useEffect(() => {
    if (!repository) return
    const controller = new AbortController()
    repositoryApi.codeChurn(repository.id, period, controller.signal)
      .then((data) => setResult({ repositoryId: repository.id, period, state: 'success', data, error: '' }))
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({
          repositoryId: repository.id,
          period,
          state: 'error',
          data: null,
          error: requestError instanceof Error ? requestError.message : 'Code churn rankings could not be loaded.',
        })
      })
    return () => controller.abort()
  }, [repository, period, reloadToken])

  if (!repository) return null
  const current = result.repositoryId === repository.id && result.period === period
    ? result
    : { ...result, state: 'loading' as const, data: null }
  const files = current.data?.files ?? []
  const totalChurn = files.reduce((sum, file) => sum + file.totalChurn, 0)
  const totalCommits = files.reduce((sum, file) => sum + file.commitCount, 0)

  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / History`} title="Code hotspots" description="Files ranked by additions and removals across Git history." action={<div className="period-control">{periods.map((option) => <button className={period === option.value ? 'active' : ''} key={option.value} onClick={() => setPeriod(option.value)} type="button">{option.label}</button>)}</div>} />

      {current.state === 'error' && <WorkspaceState message={current.error} onRetry={() => { setResult({ repositoryId: repository.id, period, state: 'loading', data: null, error: '' }); setReloadToken((value) => value + 1) }} state="error" />}
      {current.state === 'loading' && <WorkspaceState state="loading" />}
      {current.state === 'success' && (
        <>
          <section className="metrics-grid metrics-grid--three">
            <MetricCard icon={ShieldAlert} label="Files ranked" value={files.length.toString()} detail="Top 100 by total churn" />
            <MetricCard icon={Flame} label="Total churn" value={totalChurn.toLocaleString()} detail="Added plus removed lines" tone="warning" />
            <MetricCard icon={GitCommitHorizontal} label="File touches" value={totalCommits.toLocaleString()} detail="Commit-to-file modifications" />
          </section>

          {files.length === 0 ? (
            <section className="hotspot-empty"><Flame size={22} /><h2>No history metrics yet</h2><p>Run an analysis with <strong>Include Git history</strong> enabled, then return to this page.</p></section>
          ) : (
            <>
              <ChartPanel title="Highest churn files" description={`Top files for ${periods.find((option) => option.value === period)?.label.toLowerCase()}`}>
                <div className="chart-container chart-container--large">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={files.slice(0, 10)} layout="vertical" margin={{ top: 8, right: 24, bottom: 8, left: 18 }}>
                      <CartesianGrid horizontal={false} stroke="#e3e5df" strokeDasharray="3 3" />
                      <XAxis type="number" tick={{ fill: '#737d76', fontSize: 11 }} />
                      <YAxis dataKey="filePath" type="category" width={170} tick={{ fill: '#47514b', fontSize: 9 }} tickFormatter={(value: string) => value.split('/').pop() ?? value} />
                      <Tooltip contentStyle={{ border: '1px solid #d8dbd4', borderRadius: 0, fontSize: 12 }} />
                      <Bar dataKey="totalChurn" fill="#ed5b35" radius={[0, 2, 2, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </ChartPanel>
              <section className="table-panel"><div className="panel-header"><div><h2>Churn ranking</h2><p>{current.data?.analyzedAt ? `Analyzed ${new Date(current.data.analyzedAt).toLocaleString()}` : 'Latest history analysis'}</p></div></div><DataTable columns={columns} getRowKey={(row) => row.filePath} rows={files} /></section>
            </>
          )}
        </>
      )}
    </div>
  )
}