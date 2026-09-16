import {
  Activity,
  Clock3,
  GitCommitHorizontal,
  ShieldAlert,
  TrendingUp,
  Users,
} from 'lucide-react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { ChartPanel } from '../components/ChartPanel'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useRepository } from '../context/RepositoryContext'
import { activityTrend, hotspots } from '../data/mockData'
import type { Hotspot } from '../data/mockData'

const hotspotColumns: TableColumn<Hotspot>[] = [
  {
    key: 'path',
    header: 'File',
    render: (row) => <span className="code-path">{row.path}</span>,
  },
  { key: 'churn', header: 'Churn', align: 'right', render: (row) => row.churn.toLocaleString() },
  { key: 'complexity', header: 'Complexity', align: 'right', render: (row) => row.complexity },
  {
    key: 'risk',
    header: 'Risk',
    align: 'right',
    render: (row) => (
      <StatusBadge tone={row.risk === 'Critical' ? 'danger' : row.risk === 'High' ? 'warning' : 'neutral'}>
        {row.risk}
      </StatusBadge>
    ),
  },
]

export function DashboardPage() {
  const { repository } = useRepository()
  if (!repository) return null

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow={`${repository.organization} / ${repository.branch}`}
        title="Engineering health overview"
        description={`A current view of delivery flow, ownership, and code risk across ${repository.name}.`}
        action={<StatusBadge tone="positive">{`Analyzed ${repository.lastAnalyzed}`}</StatusBadge>}
      />

      <section className="metrics-grid" aria-label="Repository health metrics">
        <MetricCard icon={Activity} label="Health score" value={`${repository.healthScore}/100`} detail="Up 4 points this month" tone="positive" />
        <MetricCard icon={GitCommitHorizontal} label="Total commits" value={repository.commits.toLocaleString()} detail="274 in the last 30 days" />
        <MetricCard icon={Users} label="Active contributors" value={repository.contributors.toString()} detail="18 contributed this month" />
        <MetricCard icon={ShieldAlert} label="High-risk files" value="12" detail="3 require immediate review" tone="warning" />
        <MetricCard icon={Clock3} label="Avg. PR review time" value="4h 36m" detail="18% faster than last month" tone="positive" />
        <MetricCard icon={TrendingUp} label="Code churn" value="21.4%" detail="Within the expected range" />
      </section>

      <div className="dashboard-grid">
        <ChartPanel title="Commit activity" description="Merged commits over the last six months" className="chart-panel--wide">
          <div className="chart-container">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={activityTrend} margin={{ top: 12, right: 12, left: -24, bottom: 0 }}>
                <CartesianGrid stroke="#e3e5df" strokeDasharray="3 3" vertical={false} />
                <XAxis axisLine={false} dataKey="label" tick={{ fill: '#737d76', fontSize: 11 }} tickLine={false} />
                <YAxis axisLine={false} tick={{ fill: '#737d76', fontSize: 11 }} tickLine={false} />
                <Tooltip contentStyle={{ border: '1px solid #d8dbd4', borderRadius: 0, fontSize: 12 }} />
                <Line dataKey="commits" dot={{ fill: '#ed5b35', r: 3 }} stroke="#ed5b35" strokeWidth={2} type="monotone" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </ChartPanel>

        <ChartPanel title="Code churn" description="Changed lines as a share of codebase">
          <div className="chart-container">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={activityTrend} margin={{ top: 12, right: 4, left: -28, bottom: 0 }}>
                <CartesianGrid stroke="#e3e5df" strokeDasharray="3 3" vertical={false} />
                <XAxis axisLine={false} dataKey="label" tick={{ fill: '#737d76', fontSize: 11 }} tickLine={false} />
                <YAxis axisLine={false} tick={{ fill: '#737d76', fontSize: 11 }} tickLine={false} />
                <Tooltip contentStyle={{ border: '1px solid #d8dbd4', borderRadius: 0, fontSize: 12 }} />
                <Bar dataKey="churn" fill="#207355" radius={[2, 2, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </ChartPanel>
      </div>

      <section className="table-panel">
        <div className="panel-header">
          <div>
            <h2>Files needing attention</h2>
            <p>Highest combined churn and complexity scores</p>
          </div>
          <span className="mono-meta">Updated 12m ago</span>
        </div>
        <DataTable columns={hotspotColumns} getRowKey={(row) => row.path} rows={hotspots.slice(0, 4)} />
      </section>
    </div>
  )
}