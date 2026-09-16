import { useState } from 'react'
import {
  Boxes,
  ChevronDown,
  Clock3,
  Flame,
  GitBranch,
  GitPullRequest,
  LayoutDashboard,
  Menu,
  Network,
  Radar,
  Users,
  X,
} from 'lucide-react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { RepositoryContext } from '../context/RepositoryContext'
import { repositories } from '../data/mockData'

const navigation = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/repositories', label: 'Repositories', icon: GitBranch },
  { to: '/hotspots', label: 'Code Hotspots', icon: Flame },
  { to: '/contributors', label: 'Contributors', icon: Users },
  { to: '/pull-requests', label: 'Pull Requests', icon: GitPullRequest },
  { to: '/dependencies', label: 'Dependencies', icon: Network },
  { to: '/analysis-history', label: 'Analysis History', icon: Clock3 },
]

const pageNames: Record<string, string> = {
  '/': 'Dashboard',
  '/repositories': 'Repositories',
  '/hotspots': 'Code Hotspots',
  '/contributors': 'Contributors',
  '/pull-requests': 'Pull Requests',
  '/dependencies': 'Dependencies',
  '/analysis-history': 'Analysis History',
}

export function AppShell() {
  const location = useLocation()
  const [repositoryId, setRepositoryId] = useState(repositories[0].id)
  const [mobileNavOpen, setMobileNavOpen] = useState(false)
  const repository = repositories.find((item) => item.id === repositoryId) ?? repositories[0]

  return (
    <RepositoryContext.Provider value={{ repository, repositoryId, setRepositoryId }}>
      <div className="app-shell">
        <aside className={`sidebar ${mobileNavOpen ? 'sidebar--open' : ''}`}>
          <NavLink className="brand" to="/" onClick={() => setMobileNavOpen(false)}>
            <span className="brand-mark"><Radar size={19} /></span>
            <span>Repo Intelligence</span>
          </NavLink>

          <nav aria-label="Primary navigation">
            <span className="nav-label">Workspace</span>
            {navigation.map(({ to, label, icon: Icon }) => (
              <NavLink
                className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                end={to === '/'}
                key={to}
                onClick={() => setMobileNavOpen(false)}
                to={to}
              >
                <Icon size={17} />
                <span>{label}</span>
              </NavLink>
            ))}
          </nav>

          <div className="sidebar-status">
            <span className="status-dot" />
            <div>
              <strong>Analysis engine</strong>
              <span>Mock workspace ready</span>
            </div>
          </div>
        </aside>

        <div className="workspace">
          <header className="topbar">
            <button
              className="icon-button mobile-menu-button"
              onClick={() => setMobileNavOpen((open) => !open)}
              title={mobileNavOpen ? 'Close navigation' : 'Open navigation'}
              type="button"
            >
              {mobileNavOpen ? <X size={19} /> : <Menu size={19} />}
            </button>
            <div className="topbar-context">
              <span className="eyebrow">Engineering intelligence</span>
              <strong>{pageNames[location.pathname] ?? 'Workspace'}</strong>
            </div>

            <label className="repository-selector">
              <span className="repository-selector__icon"><Boxes size={16} /></span>
              <span className="repository-selector__copy">
                <small>Repository</small>
                <select value={repositoryId} onChange={(event) => setRepositoryId(event.target.value)}>
                  {repositories.map((item) => (
                    <option value={item.id} key={item.id}>{item.organization}/{item.name}</option>
                  ))}
                </select>
              </span>
              <ChevronDown size={15} />
            </label>
          </header>

          <main className="page-content">
            <Outlet />
          </main>
        </div>

        {mobileNavOpen && (
          <button className="nav-scrim" aria-label="Close navigation" onClick={() => setMobileNavOpen(false)} type="button" />
        )}
      </div>
    </RepositoryContext.Provider>
  )
}