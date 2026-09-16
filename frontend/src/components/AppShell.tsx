import { useState } from 'react'
import {
  Boxes,
  ChevronDown,
  Clock3,
  Flame,
  GitBranch,
  GitPullRequest,
  LayoutDashboard,
  Gauge,
  Menu,
  Code2,
  Network,
  Radar,
  Users,
  X,
} from 'lucide-react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useRepository } from '../context/RepositoryContext'
import { useAuth } from '../context/AuthContext'
import { WorkspaceState } from './WorkspaceState'

const navigation = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/repositories', label: 'Repositories', icon: GitBranch },
  { to: '/hotspots', label: 'Code Hotspots', icon: Flame },
  { to: '/contributors', label: 'Contributors', icon: Users },
  { to: '/complexity', label: 'Complexity', icon: Gauge },
  { to: '/pull-requests', label: 'Pull Requests', icon: GitPullRequest },
  { to: '/dependencies', label: 'Dependencies', icon: Network },
  { to: '/analysis-history', label: 'Analysis History', icon: Clock3 },
]

const pageNames: Record<string, string> = {
  '/': 'Dashboard',
  '/repositories': 'Repositories',
  '/hotspots': 'Code Hotspots',
  '/contributors': 'Contributors',
  '/complexity': 'Complexity',
  '/pull-requests': 'Pull Requests',
  '/dependencies': 'Dependencies',
  '/analysis-history': 'Analysis History',
}

export function AppShell() {
  const location = useLocation()
  const auth = useAuth()
  const { repositories, repositoryId, setRepositoryId, loadState, error, reload } = useRepository()
  const [mobileNavOpen, setMobileNavOpen] = useState(false)
  const emptyWorkspace = loadState === 'success' && repositories.length === 0 && location.pathname !== '/repositories'
  const currentPageName = location.pathname.startsWith('/repositories/')
    ? 'Repository Details'
    : (pageNames[location.pathname] ?? 'Workspace')

  return (
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
              <span>{loadState === 'success' ? 'API connected' : loadState === 'error' ? 'API unavailable' : 'Connecting...'}</span>
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
              <strong>{currentPageName}</strong>
            </div>

            <label className="repository-selector">
              <span className="repository-selector__icon"><Boxes size={16} /></span>
              <span className="repository-selector__copy">
                <small>Repository</small>
                <select disabled={loadState !== 'success' || repositories.length === 0} value={repositoryId} onChange={(event) => setRepositoryId(event.target.value)}>
                  {loadState === 'loading' && <option value="">Loading repositories...</option>}
                  {loadState === 'error' && <option value="">Repositories unavailable</option>}
                  {loadState === 'success' && repositories.length === 0 && <option value="">No repositories</option>}
                  {repositories.map((item) => <option value={item.id} key={item.id}>{item.fullName}</option>)}
                </select>
              </span>
              <ChevronDown size={15} />
            </label>
            <div className="github-account">
              {auth.state === 'loading' ? (
                <span className="github-account__loading">Checking GitHub...</span>
              ) : auth.user?.connected ? (
                <>
                  {auth.user.avatarUrl ? <img alt="" src={auth.user.avatarUrl} /> : <span className="github-avatar-fallback"><Code2 size={15} /></span>}
                  <span className="github-account__identity"><small>GitHub</small><strong>@{auth.user.login}</strong></span>
                  <button className="account-action" disabled={auth.state === 'disconnecting'} onClick={() => void auth.disconnect()} type="button">Disconnect</button>
                </>
              ) : (
                <button className="connect-github-button" disabled={!auth.user?.configured || auth.state === 'error'} onClick={auth.connect} title={auth.user?.configured ? 'Connect GitHub account' : 'Configure GitHub OAuth on the server'} type="button"><Code2 size={15} /> Connect GitHub</button>
              )}
            </div>
          </header>

          <main className="page-content">
            {loadState === 'loading' && <WorkspaceState state="loading" />}
            {loadState === 'error' && <WorkspaceState message={error} onRetry={reload} state="error" />}
            {emptyWorkspace && <WorkspaceState state="empty" />}
            {loadState === 'success' && !emptyWorkspace && <Outlet />}
          </main>
        </div>

        {mobileNavOpen && (
          <button className="nav-scrim" aria-label="Close navigation" onClick={() => setMobileNavOpen(false)} type="button" />
        )}
    </div>
  )
}