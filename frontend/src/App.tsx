import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { AccountPage } from './pages/AccountPage'
import { HomePage } from './pages/HomePage'
import { MyUrlsPage } from './pages/MyUrlsPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { RegisterPage } from './pages/RegisterPage'

function App() {
  return (
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
  )
}

export default App
