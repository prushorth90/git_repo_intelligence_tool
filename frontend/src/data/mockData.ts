export type TrendPoint = {
  label: string
  commits: number
  churn: number
}

export type RepositorySummary = {
  id: string
  name: string
  organization: string
  language: string
  branch: string
  healthScore: number
  commits: number
  contributors: number
  lastAnalyzed: string
  status: 'Healthy' | 'Attention' | 'At risk'
}

export type Hotspot = {
  path: string
  language: string
  churn: number
  complexity: number
  contributors: number
  risk: 'Low' | 'Medium' | 'High' | 'Critical'
}

export type Contributor = {
  name: string
  handle: string
  commits: number
  additions: number
  deletions: number
  ownership: number
  focus: string
}

export type PullRequest = {
  number: number
  title: string
  author: string
  status: 'Open' | 'Merged' | 'Closed'
  reviewTime: string
  comments: number
  changedFiles: number
}

export const repositories: RepositorySummary[] = [
  {
    id: 'atlas-web',
    name: 'atlas-web',
    organization: 'northstar-labs',
    language: 'TypeScript',
    branch: 'main',
    healthScore: 82,
    commits: 2847,
    contributors: 24,
    lastAnalyzed: '12 minutes ago',
    status: 'Healthy',
  },
  {
    id: 'payments-api',
    name: 'payments-api',
    organization: 'northstar-labs',
    language: 'Java',
    branch: 'main',
    healthScore: 68,
    commits: 1932,
    contributors: 16,
    lastAnalyzed: '1 hour ago',
    status: 'Attention',
  },
  {
    id: 'event-pipeline',
    name: 'event-pipeline',
    organization: 'northstar-labs',
    language: 'Go',
    branch: 'trunk',
    healthScore: 54,
    commits: 1108,
    contributors: 11,
    lastAnalyzed: 'Yesterday',
    status: 'At risk',
  },
]

export const activityTrend: TrendPoint[] = [
  { label: 'Apr', commits: 188, churn: 12 },
  { label: 'May', commits: 232, churn: 18 },
  { label: 'Jun', commits: 204, churn: 15 },
  { label: 'Jul', commits: 286, churn: 24 },
  { label: 'Aug', commits: 318, churn: 27 },
  { label: 'Sep', commits: 274, churn: 21 },
]

export const hotspots: Hotspot[] = [
  { path: 'src/modules/checkout/CheckoutService.ts', language: 'TypeScript', churn: 482, complexity: 34, contributors: 9, risk: 'Critical' },
  { path: 'src/platform/auth/session.ts', language: 'TypeScript', churn: 316, complexity: 27, contributors: 7, risk: 'High' },
  { path: 'src/components/DataGrid/DataGrid.tsx', language: 'TSX', churn: 289, complexity: 22, contributors: 11, risk: 'High' },
  { path: 'src/lib/featureFlags.ts', language: 'TypeScript', churn: 174, complexity: 18, contributors: 5, risk: 'Medium' },
  { path: 'src/routes/settings.tsx', language: 'TSX', churn: 132, complexity: 12, contributors: 4, risk: 'Low' },
]

export const contributors: Contributor[] = [
  { name: 'Maya Chen', handle: '@mayac', commits: 418, additions: 18240, deletions: 7690, ownership: 23, focus: 'Platform' },
  { name: 'Jon Bell', handle: '@jonbell', commits: 352, additions: 14980, deletions: 6210, ownership: 19, focus: 'Frontend' },
  { name: 'Rina Patel', handle: '@rpatel', commits: 296, additions: 12140, deletions: 5840, ownership: 16, focus: 'Checkout' },
  { name: 'Owen Brooks', handle: '@obrooks', commits: 241, additions: 10220, deletions: 4730, ownership: 13, focus: 'Infrastructure' },
  { name: 'Sam Rivera', handle: '@srivera', commits: 187, additions: 8460, deletions: 3180, ownership: 10, focus: 'Design system' },
]

export const pullRequests: PullRequest[] = [
  { number: 1842, title: 'Refactor checkout orchestration', author: 'Maya Chen', status: 'Open', reviewTime: '3h 18m', comments: 12, changedFiles: 18 },
  { number: 1837, title: 'Add regional tax configuration', author: 'Rina Patel', status: 'Merged', reviewTime: '5h 42m', comments: 8, changedFiles: 11 },
  { number: 1831, title: 'Reduce dashboard bundle size', author: 'Jon Bell', status: 'Merged', reviewTime: '2h 14m', comments: 5, changedFiles: 7 },
  { number: 1828, title: 'Update deployment health gates', author: 'Owen Brooks', status: 'Closed', reviewTime: '1d 4h', comments: 19, changedFiles: 23 },
  { number: 1824, title: 'Normalize feature flag evaluation', author: 'Sam Rivera', status: 'Merged', reviewTime: '4h 06m', comments: 7, changedFiles: 9 },
]

export const dependencyGroups = [
  { name: 'Production', count: 128, outdated: 9, vulnerable: 2 },
  { name: 'Development', count: 74, outdated: 14, vulnerable: 1 },
  { name: 'Transitive', count: 462, outdated: 31, vulnerable: 4 },
]

export const analysisHistory = [
  { id: 'run-1048', started: 'Today, 09:42', duration: '4m 18s', trigger: 'Push to main', commit: '8d41a0f', status: 'Completed' },
  { id: 'run-1047', started: 'Yesterday, 16:08', duration: '4m 51s', trigger: 'Manual', commit: '6ab20de', status: 'Completed' },
  { id: 'run-1046', started: 'Sep 13, 11:24', duration: '2m 03s', trigger: 'Pull request', commit: '154dc3b', status: 'Failed' },
  { id: 'run-1045', started: 'Sep 12, 08:16', duration: '4m 29s', trigger: 'Push to main', commit: '9fbd221', status: 'Completed' },
]

export const languageMix = [
  { name: 'TypeScript', value: 58 },
  { name: 'TSX', value: 24 },
  { name: 'CSS', value: 11 },
  { name: 'Other', value: 7 },
]