import { useEffect, useState } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { refresh } from './api/endpoints'
import { formatApiError } from './api/http'
import { loadCredentials, saveCredentials } from './auth/credentials'
import { Layout } from './components/Layout'
import { AccountPage } from './pages/AccountPage'
import { HomePage } from './pages/HomePage'
import { MyUrlsPage } from './pages/MyUrlsPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { RegisterPage } from './pages/RegisterPage'

function App() {
  return (
    <AuthBootstrap>
      <Layout>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/my-urls" element={<MyUrlsPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/account" element={<AccountPage />} />
          <Route path="/404" element={<NotFoundPage />} />
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Routes>
      </Layout>
    </AuthBootstrap>
  )
}

export default App

function AuthBootstrap({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true
    async function init() {
      // If token already exists in this tab -> no work.
      if (!loadCredentials()) {
        try {
          // If refresh cookie exists (shared across tabs), we can silently mint a new access token for this tab.
          const res = await refresh()
          saveCredentials({ username: res.username, token: res.accessToken })
        } catch (e) {
          // Most commonly: user just has no refresh cookie yet.
          if (mounted) setError(formatApiError(e))
        }
      }
      if (mounted) setReady(true)
    }
    init()
    return () => {
      mounted = false
    }
  }, [])

  if (!ready) {
    return (
      <div className="min-h-screen bg-slate-950 text-slate-100">
        <div className="mx-auto flex min-h-screen max-w-5xl items-center justify-center px-4">
          <div className="text-sm text-slate-300">Восстанавливаем сессию…</div>
        </div>
      </div>
    )
  }

  // If refresh failed, it's fine — user can still use public pages and log in.
  // We don't show the error globally to avoid noise.
  void error
  return <>{children}</>
}
