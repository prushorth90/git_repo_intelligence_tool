import { Braces, FileCode2, Gauge } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { repositoryApi } from '../api/client'
import type { ComplexityOverviewResponse, FileComplexityResponse, MethodComplexityResponse } from '../api/types'
import { ChartPanel } from '../components/ChartPanel'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { WorkspaceState } from '../components/WorkspaceState'
import { useRepository } from '../context/RepositoryContext'

const fileColumns: TableColumn<FileComplexityResponse>[] = [
  { key: 'file', header: 'File', render: (row) => <span className="code-path">{row.filePath}</span> },
  { key: 'language', header: 'Language', render: (row) => row.language },
  { key: 'callables', header: 'Callables', align: 'right', render: (row) => row.methodCount + row.functionCount },
  { key: 'flow', header: 'Flow branches', align: 'right', render: (row) => row.controlFlowCount },
  { key: 'nesting', header: 'Max nesting', align: 'right', render: (row) => row.maximumNestingDepth },
  { key: 'method', header: 'Max method', align: 'right', render: (row) => row.maximumMethodComplexity },
  { key: 'complexity', header: 'File complexity', align: 'right', render: (row) => <strong className="score-cell">{row.cyclomaticComplexity}</strong> },
]

const methodColumns: TableColumn<MethodComplexityResponse>[] = [
  { key: 'symbol', header: 'Method / function', render: (row) => <div className="primary-cell"><span className="table-icon"><Braces size={15} /></span><span><strong>{row.symbolName}</strong><small>{row.filePath}:{row.startLine}</small></span></div> },
  { key: 'language', header: 'Language', render: (row) => row.language },
  { key: 'kind', header: 'Kind', render: (row) => row.symbolKind },
  { key: 'length', header: 'Lines', align: 'right', render: (row) => row.lineCount },
  { key: 'nesting', header: 'Nesting', align: 'right', render: (row) => row.nestingDepth },
  { key: 'complexity', header: 'Complexity', align: 'right', render: (row) => <strong className="score-cell">{row.cyclomaticComplexity}</strong> },
]

type ComplexityResult = {
  repositoryId: string
  state: 'loading' | 'success' | 'error'
  data: ComplexityOverviewResponse | null
  error: string
}

export function ComplexityPage() {
  const { repository } = useRepository()
  const [reloadToken, setReloadToken] = useState(0)
  const [result, setResult] = useState<ComplexityResult>({ repositoryId: '', state: 'loading', data: null, error: '' })

  useEffect(() => {
    if (!repository) return
    const controller = new AbortController()
    repositoryApi.complexity(repository.id, controller.signal)
      .then((data) => setResult({ repositoryId: repository.id, state: 'success', data, error: '' }))
      .catch((requestError: unknown) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return
        setResult({ repositoryId: repository.id, state: 'error', data: null, error: requestError instanceof Error ? requestError.message : 'Complexity rankings could not be loaded.' })
      })
    return () => controller.abort()
  }, [repository, reloadToken])

  if (!repository) return null
  const current = result.repositoryId === repository.id ? result : { ...result, state: 'loading' as const, data: null }
  if (current.state === 'loading') return <WorkspaceState state="loading" />
  if (current.state === 'error') return <WorkspaceState message={current.error} onRetry={() => { setResult({ repositoryId: repository.id, state: 'loading', data: null, error: '' }); setReloadToken((value) => value + 1) }} state="error" />

  const data = current.data ?? { analysisId: null, analyzedAt: null, scoringFormula: '', decisionRules: [], files: [], methods: [] }
  const maximumFile = data.files[0]?.cyclomaticComplexity ?? 0
  const maximumMethod = data.methods[0]?.cyclomaticComplexity ?? 0

  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / AST`} title="Complexity" description="Explainable cyclomatic-complexity approximation derived from Tree-sitter syntax trees." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={FileCode2} label="Parsed files" value={data.files.length.toString()} detail="Top 100 ranked files" />
        <MetricCard icon={Gauge} label="Highest file score" value={maximumFile.toString()} detail="One plus file decision points" tone={maximumFile > 20 ? 'warning' : 'default'} />
        <MetricCard icon={Braces} label="Highest callable" value={maximumMethod.toString()} detail="Nested callables scored separately" tone={maximumMethod > 10 ? 'warning' : 'default'} />
      </section>

      <section className="complexity-rules"><div><span className="eyebrow">Scoring model</span><h2>{data.scoringFormula}</h2></div><ul>{data.decisionRules.map((rule) => <li key={rule}>{rule}</li>)}</ul></section>

      {data.files.length === 0 ? (
        <section className="hotspot-empty"><Gauge size={22} /><h2>No complexity metrics yet</h2><p>Run repository analysis to parse supported source files.</p></section>
      ) : (
        <>
          <ChartPanel title="Highest-complexity files" description="Top file scores from the latest structural analysis"><div className="chart-container chart-container--large"><ResponsiveContainer width="100%" height="100%"><BarChart data={data.files.slice(0, 12)} layout="vertical" margin={{ top: 8, right: 24, bottom: 8, left: 18 }}><CartesianGrid horizontal={false} stroke="#e3e5df" strokeDasharray="3 3" /><XAxis type="number" tick={{ fill: '#737d76', fontSize: 11 }} /><YAxis dataKey="filePath" type="category" width={180} tick={{ fill: '#47514b', fontSize: 9 }} tickFormatter={(value: string) => value.split('/').pop() ?? value} /><Tooltip /><Bar dataKey="cyclomaticComplexity" fill="#ed5b35" radius={[0, 2, 2, 0]} /></BarChart></ResponsiveContainer></div></ChartPanel>
          <section className="table-panel"><div className="panel-header"><div><h2>File complexity</h2><p>{data.analyzedAt ? `Analyzed ${new Date(data.analyzedAt).toLocaleString()}` : 'Latest structural analysis'}</p></div></div><DataTable columns={fileColumns} getRowKey={(row) => row.filePath} rows={data.files} /></section>
          <section className="table-panel"><div className="panel-header"><div><h2>Method and function complexity</h2><p>Callable scores exclude nested callable bodies</p></div></div><DataTable columns={methodColumns} emptyMessage="No methods or functions were found." getRowKey={(row) => `${row.filePath}:${row.startLine}:${row.symbolName}`} rows={data.methods} /></section>
        </>
      )}
    </div>
  )
}