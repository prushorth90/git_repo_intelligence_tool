import { Flame, GitCommitHorizontal, ShieldAlert } from 'lucide-react'
import { Cell, Scatter, ScatterChart, ResponsiveContainer, Tooltip, XAxis, YAxis, ZAxis } from 'recharts'
import { ChartPanel } from '../components/ChartPanel'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useRepository } from '../context/RepositoryContext'
import { hotspots } from '../data/mockData'
import type { Hotspot } from '../data/mockData'

const riskColors = { Low: '#5f8b78', Medium: '#d2a02e', High: '#e27435', Critical: '#c63f31' }

const columns: TableColumn<Hotspot>[] = [
  { key: 'path', header: 'File path', render: (row) => <span className="code-path">{row.path}</span> },
  { key: 'language', header: 'Language', render: (row) => row.language },
  { key: 'churn', header: 'Churn', align: 'right', render: (row) => row.churn.toLocaleString() },
  { key: 'complexity', header: 'Complexity', align: 'right', render: (row) => row.complexity },
  { key: 'contributors', header: 'Owners', align: 'right', render: (row) => row.contributors },
  { key: 'risk', header: 'Risk', align: 'right', render: (row) => <StatusBadge tone={row.risk === 'Critical' ? 'danger' : row.risk === 'High' ? 'warning' : 'neutral'}>{row.risk}</StatusBadge> },
]

export function HotspotsPage() {
  const { repository } = useRepository()

  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Risk`} title="Code hotspots" description="Files where frequent change and structural complexity compound engineering risk." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={ShieldAlert} label="Critical files" value="3" detail="Above the intervention threshold" tone="warning" />
        <MetricCard icon={Flame} label="Hotspot coverage" value="18%" detail="Share of code touched frequently" />
        <MetricCard icon={GitCommitHorizontal} label="Hotspot commits" value="146" detail="Across the last 30 days" />
      </section>
      <ChartPanel title="Risk matrix" description="Complexity against 90-day code churn; bubble size reflects contributor count">
        <div className="chart-container chart-container--large">
          <ResponsiveContainer width="100%" height="100%">
            <ScatterChart margin={{ top: 16, right: 24, bottom: 8, left: 0 }}>
              <XAxis dataKey="churn" name="Churn" tick={{ fill: '#737d76', fontSize: 11 }} type="number" />
              <YAxis dataKey="complexity" name="Complexity" tick={{ fill: '#737d76', fontSize: 11 }} type="number" />
              <ZAxis dataKey="contributors" range={[80, 400]} />
              <Tooltip cursor={{ strokeDasharray: '3 3' }} />
              <Scatter data={hotspots}>
                {hotspots.map((item) => <Cell fill={riskColors[item.risk]} key={item.path} />)}
              </Scatter>
            </ScatterChart>
          </ResponsiveContainer>
        </div>
      </ChartPanel>
      <section className="table-panel">
        <div className="panel-header"><div><h2>Ranked files</h2><p>Sorted by combined churn and complexity</p></div></div>
        <DataTable columns={columns} getRowKey={(row) => row.path} rows={hotspots} />
      </section>
    </div>
  )
}