import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, register } from '../api/endpoints'
import { formatApiError } from '../api/http'
import type { RegisterResult } from '../api/types'
import { saveCredentials } from '../auth/credentials'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { isLatinAuth } from '../utils/validation'

export function RegisterPage() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [password2, setPassword2] = useState('')
  const [result, setResult] = useState<RegisterResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function onRegister() {
    setError(null)
    setResult(null)
    const u = username.trim()
    if (!isLatinAuth(u)) {
      setError('Логин: только латиница, цифры, ".", "_" или "-"')
      return
    }
    if (!isLatinAuth(password)) {
      setError('Пароль: только латиница, цифры, ".", "_" или "-"')
      return
    }
    if (password !== password2) {
      setError('Пароли не совпадают')
      return
    }
    setLoading(true)
    try {
      const data = await register(u, password)
      setResult(data)
      // удобство: сразу залогиниться и сохранить JWT
      const auth = await login(u, password)
      saveCredentials({ username: auth.username, token: auth.accessToken })
    } catch (e) {
      setError(formatApiError(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto max-w-xl">
      <Card title="Регистрация" subtitle="Создаёт пользователя. Дальше можно войти через JWT (access token).">
        <div className="grid gap-3">
          <Input label="Username" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
          <Input
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
          />
          <Input
            label="Повторите пароль"
            type="password"
            value={password2}
            onChange={(e) => setPassword2(e.target.value)}
            autoComplete="new-password"
          />
          <div className="flex flex-wrap items-center gap-2">
            <Button onClick={onRegister} disabled={loading || username.trim().length === 0 || password.length === 0}>
              {loading ? 'Регистрируем…' : 'Зарегистрироваться'}
            </Button>
            <Button variant="secondary" onClick={() => navigate('/account')}>
              Перейти в аккаунт
            </Button>
          </div>

          {error ? (
            <div className="rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-100">
              {error}
            </div>
          ) : null}

          {result ? (
            <div className="rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-3 text-sm text-emerald-100">
              Пользователь <span className="font-semibold">{result.username}</span> создан (id={result.id}).
            </div>
          ) : null}
        </div>
      </Card>
    </div>
  )
}


