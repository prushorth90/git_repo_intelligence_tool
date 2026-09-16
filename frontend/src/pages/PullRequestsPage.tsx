import { Clock3, GitMerge, GitPullRequest } from 'lucide-react'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { MetricCard } from '../components/MetricCard'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useRepository } from '../context/RepositoryContext'
import { pullRequests } from '../data/mockData'
import type { PullRequest } from '../data/mockData'

const columns: TableColumn<PullRequest>[] = [
  { key: 'pr', header: 'Pull request', render: (row) => <div className="primary-cell"><span className="table-icon"><GitPullRequest size={16} /></span><span><strong>#{row.number} {row.title}</strong><small>by {row.author}</small></span></div> },
  { key: 'status', header: 'Status', render: (row) => <StatusBadge tone={row.status === 'Merged' ? 'positive' : row.status === 'Open' ? 'warning' : 'neutral'}>{row.status}</StatusBadge> },
  { key: 'review', header: 'Review time', align: 'right', render: (row) => row.reviewTime },
  { key: 'comments', header: 'Comments', align: 'right', render: (row) => row.comments },
  { key: 'files', header: 'Files', align: 'right', render: (row) => row.changedFiles },
]

export function PullRequestsPage() {
  const { repository } = useRepository()
  if (!repository) return null
  return (
    <div className="page-stack">
      <PageHeader eyebrow={`${repository.name} / Delivery`} title="Pull requests" description="Review throughput, collaboration load, and cycle-time signals." />
      <section className="metrics-grid metrics-grid--three">
        <MetricCard icon={Clock3} label="Avg. review time" value="4h 36m" detail="18% faster month over month" tone="positive" />
        <MetricCard icon={GitMerge} label="Merge rate" value="86%" detail="124 merged this month" />
        <MetricCard icon={GitPullRequest} label="Open requests" value="14" detail="3 awaiting first review" tone="warning" />
      </section>
      <section className="flow-strip" aria-label="Pull request flow">
        <div><span>Opened</span><strong>148</strong></div><i />
        <div><span>Reviewed</span><strong>136</strong></div><i />
        <div><span>Approved</span><strong>129</strong></div><i />
        <div><span>Merged</span><strong>124</strong></div>
      </section>
      <section className="table-panel"><div className="panel-header"><div><h2>Recent pull requests</h2><p>Latest collaboration activity on the selected repository</p></div></div><DataTable columns={columns} getRowKey={(row) => row.number} rows={pullRequests} /></section>
    </div>
  )
}