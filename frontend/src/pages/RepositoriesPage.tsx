import { ArrowUpRight, GitBranch, Search } from 'lucide-react'
import { useDeferredValue, useState } from 'react'
import { DataTable } from '../components/DataTable'
import type { TableColumn } from '../components/DataTable'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import { useRepository } from '../context/RepositoryContext'
import { repositories } from '../data/mockData'
import type { RepositorySummary } from '../data/mockData'

export function RepositoriesPage() {
  const { setRepositoryId } = useRepository()
  const [query, setQuery] = useState('')
  const deferredQuery = useDeferredValue(query)
  const filtered = repositories.filter((repository) =>
    `${repository.organization}/${repository.name}`.toLowerCase().includes(deferredQuery.toLowerCase()),
  )
  const columns: TableColumn<RepositorySummary>[] = [
    {
      key: 'repository',
      header: 'Repository',
      render: (row) => (
        <div className="primary-cell">
          <span className="table-icon"><GitBranch size={16} /></span>
          <span><strong>{row.organization}/{row.name}</strong><small>{row.language} · {row.branch}</small></span>
        </div>
      ),
    },
    { key: 'health', header: 'Health', render: (row) => <strong className="score-cell">{row.healthScore}</strong> },
    { key: 'commits', header: 'Commits', align: 'right', render: (row) => row.commits.toLocaleString() },
    { key: 'contributors', header: 'Contributors', align: 'right', render: (row) => row.contributors },
    {
      key: 'status',
      header: 'Status',
      align: 'right',
      render: (row) => <StatusBadge tone={row.status === 'Healthy' ? 'positive' : row.status === 'Attention' ? 'warning' : 'danger'}>{row.status}</StatusBadge>,
    },
    {
      key: 'select',
      header: '',
      align: 'right',
      render: (row) => (
        <button className="table-icon-button" onClick={() => setRepositoryId(row.id)} title={`Select ${row.name}`} type="button">
          <ArrowUpRight size={16} />
        </button>
      ),
    },
  ]

  return (
    <div className="page-stack">
      <PageHeader
        eyebrow="Portfolio"
        title="Repositories"
        description="Browse connected codebases and compare their latest engineering health signals."
      />

      <section className="table-panel">
        <div className="panel-header panel-header--toolbar">
          <div>
            <h2>Connected repositories</h2>
            <p>{repositories.length} codebases across 1 organization</p>
          </div>
          <label className="search-control">
            <Search size={15} />
            <input aria-label="Search repositories" onChange={(event) => setQuery(event.target.value)} placeholder="Search repositories" value={query} />
          </label>
        </div>
        <DataTable columns={columns} getRowKey={(row) => row.id} rows={filtered} />
      </section>
    </div>
  )
}