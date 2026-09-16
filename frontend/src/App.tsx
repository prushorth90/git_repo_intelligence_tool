import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { AuthProvider } from './context/AuthProvider'
import { RepositoryProvider } from './context/RepositoryProvider'
import './App.css'

const DashboardPage = lazy(() => import('./pages/DashboardPage').then((module) => ({ default: module.DashboardPage })))
const RepositoriesPage = lazy(() => import('./pages/RepositoriesPage').then((module) => ({ default: module.RepositoriesPage })))
const RepositoryDetailsPage = lazy(() => import('./pages/RepositoryDetailsPage').then((module) => ({ default: module.RepositoryDetailsPage })))
const HotspotsPage = lazy(() => import('./pages/HotspotsPage').then((module) => ({ default: module.HotspotsPage })))
const ContributorsPage = lazy(() => import('./pages/ContributorsPage').then((module) => ({ default: module.ContributorsPage })))
const ComplexityPage = lazy(() => import('./pages/ComplexityPage').then((module) => ({ default: module.ComplexityPage })))
const PullRequestsPage = lazy(() => import('./pages/PullRequestsPage').then((module) => ({ default: module.PullRequestsPage })))
const DependenciesPage = lazy(() => import('./pages/DependenciesPage').then((module) => ({ default: module.DependenciesPage })))
const AnalysisHistoryPage = lazy(() => import('./pages/AnalysisHistoryPage').then((module) => ({ default: module.AnalysisHistoryPage })))

function App() {
  return (
    <AuthProvider>
      <RepositoryProvider>
        <Suspense fallback={<div className="route-loading">Loading workspace...</div>}>
        <Routes>
          <Route element={<AppShell />}>
            <Route index element={<DashboardPage />} />
            <Route path="repositories" element={<RepositoriesPage />} />
            <Route path="repositories/:repositoryId" element={<RepositoryDetailsPage />} />
            <Route path="hotspots" element={<HotspotsPage />} />
            <Route path="contributors" element={<ContributorsPage />} />
            <Route path="complexity" element={<ComplexityPage />} />
            <Route path="pull-requests" element={<PullRequestsPage />} />
            <Route path="dependencies" element={<DependenciesPage />} />
            <Route path="analysis-history" element={<AnalysisHistoryPage />} />
          </Route>
          <Route path="*" element={<Navigate replace to="/" />} />
        </Routes>
        </Suspense>
      </RepositoryProvider>
    </AuthProvider>
  )
}

export default App