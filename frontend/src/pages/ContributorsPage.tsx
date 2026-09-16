import { GitCommitHorizontal, UserRoundCheck, Users } from 'lucide-react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { ChartPanel } from '../components/ChartPanel'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { useRepository } from '../context/RepositoryContext'
import { contributors } from '../data/mockData'
import type { Contributor } from '../data/mockData'

const columns: TableColumn<Contributor>[] = [
  { key: 'name', header: 'Contributor', render: (row) => <div className="person-cell"><span>{row.name.split(' ').map((part) => part[0]).join('')}</span><div><strong>{row.name}</strong><small>{row.handle}</small></div></div> },
  { key: 'focus', header: 'Primary area', render: (row) => row.focus },
  { key: 'commits', header: 'Commits', align: 'right', render: (row) => row.commits },
  { key: 'changes', header: 'Lines changed', align: 'right', render: (row) => (row.additions + row.deletions).toLocaleString() },
  { key: 'ownership', header: 'Ownership', align: 'right', render: (row) => `${row.ownership}%` },
]

export function ContributorsPage() {
  const { repository } = useRepository()
  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Ownership`} title="Contributors" description="Contribution patterns, code ownership, and areas of concentrated knowledge." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={Users} label="Active contributors" value="24" detail="18 active in the last 30 days" />
        <MetricCard icon={UserRoundCheck} label="Bus factor" value="4" detail="Stable across core modules" tone="positive" />
        <MetricCard icon={GitCommitHorizontal} label="Median commits" value="48" detail="Per active contributor" />
      </section>
      <ChartPanel title="Contribution distribution" description="Commits by the most active contributors">
        <div className="chart-container chart-container--large">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={contributors} layout="vertical" margin={{ top: 8, right: 20, bottom: 8, left: 12 }}>
              <CartesianGrid horizontal={false} stroke="#e3e5df" strokeDasharray="3 3" />
              <XAxis type="number" tick={{ fill: '#737d76', fontSize: 11 }} />
              <YAxis dataKey="name" type="category" width={88} tick={{ fill: '#47514b', fontSize: 11 }} />
              <Tooltip contentStyle={{ border: '1px solid #d8dbd4', borderRadius: 0, fontSize: 12 }} />
              <Bar dataKey="commits" fill="#207355" radius={[0, 2, 2, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </ChartPanel>
      <section className="table-panel"><div className="panel-header"><div><h2>Ownership detail</h2><p>Contribution totals for the current analysis window</p></div></div><DataTable columns={columns} getRowKey={(row) => row.handle} rows={contributors} /></section>
    </div>
  )
}