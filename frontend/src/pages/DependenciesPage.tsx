import { Boxes, CircleAlert, PackageCheck } from 'lucide-react'
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { ChartPanel } from '../components/ChartPanel'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useRepository } from '../context/RepositoryContext'
import { dependencyGroups, languageMix } from '../data/mockData'

type Dependency = { name: string; version: string; latest: string; type: string; status: 'Current' | 'Outdated' | 'Vulnerable' }
const dependencies: Dependency[] = [
  { name: 'react', version: '19.2.0', latest: '19.2.0', type: 'Production', status: 'Current' },
  { name: 'vite', version: '8.1.0', latest: '8.3.0', type: 'Development', status: 'Outdated' },
  { name: 'path-to-regexp', version: '6.2.1', latest: '8.2.0', type: 'Transitive', status: 'Vulnerable' },
  { name: 'typescript', version: '6.0.2', latest: '6.0.2', type: 'Development', status: 'Current' },
  { name: 'zod', version: '4.1.5', latest: '4.1.9', type: 'Production', status: 'Outdated' },
]
const columns: TableColumn<Dependency>[] = [
  { key: 'name', header: 'Package', render: (row) => <span className="package-name">{row.name}</span> },
  { key: 'installed', header: 'Installed', render: (row) => row.version },
  { key: 'latest', header: 'Latest', render: (row) => row.latest },
  { key: 'type', header: 'Type', render: (row) => row.type },
  { key: 'status', header: 'Status', align: 'right', render: (row) => <StatusBadge tone={row.status === 'Current' ? 'positive' : row.status === 'Vulnerable' ? 'danger' : 'warning'}>{row.status}</StatusBadge> },
]

export function DependenciesPage() {
  const { repository } = useRepository()
  if (!repository) return null
  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Supply chain`} title="Dependencies" description="Version freshness and vulnerability posture across the dependency graph." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={Boxes} label="Total packages" value="664" detail="202 direct · 462 transitive" />
        <MetricCard icon={CircleAlert} label="Vulnerabilities" value="7" detail="2 high severity findings" tone="warning" />
        <MetricCard icon={PackageCheck} label="Current" value="91%" detail="54 updates are available" tone="positive" />
      </section>
      <div className="dashboard-grid">
        <ChartPanel title="Dependency composition" description="Packages grouped by relationship">
          <div className="chart-container">
            <ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={dependencyGroups} dataKey="count" innerRadius={54} nameKey="name" outerRadius={82} paddingAngle={3}>{['#207355', '#ed5b35', '#82948a'].map((color) => <Cell fill={color} key={color} />)}</Pie><Tooltip /></PieChart></ResponsiveContainer>
          </div>
        </ChartPanel>
        <ChartPanel title="Code composition" description="Languages in the selected repository">
          <div className="composition-list">{languageMix.map((item) => <div key={item.name}><span><strong>{item.name}</strong><small>{item.value}%</small></span><div><i style={{ width: `${item.value}%` }} /></div></div>)}</div>
        </ChartPanel>
      </div>
      <section className="table-panel"><div className="panel-header"><div><h2>Dependency inventory</h2><p>Packages requiring review are shown first</p></div></div><DataTable columns={columns} getRowKey={(row) => row.name} rows={dependencies} /></section>
    </div>
  )
}