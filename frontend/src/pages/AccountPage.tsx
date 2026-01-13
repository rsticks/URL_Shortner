import { useEffect, useMemo, useState } from 'react'
import { purchaseSubscription, subscriptionMe } from '../api/endpoints'
import { formatApiError } from '../api/http'
import type { PurchaseResult, SubscriptionPlan } from '../api/types'
import { clearCredentials, loadCredentials, saveCredentials, type Credentials } from '../auth/credentials'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'

function formatDate(iso: string | null | undefined) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString()
}

export function AccountPage() {
  const initial = useMemo(() => loadCredentials(), [])
  const [creds, setCreds] = useState<Credentials | null>(initial)
  const [username, setUsername] = useState(initial?.username ?? '')
  const [password, setPassword] = useState(initial?.password ?? '')
  const [me, setMe] = useState<PurchaseResult | null>(null)
  const [plan, setPlan] = useState<SubscriptionPlan>('MONTHLY')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function refresh(current: Credentials) {
    setError(null)
    setLoading(true)
    try {
      const data = await subscriptionMe(current)
      setMe(data)
    } catch (e) {
      setMe(null)
      setError(formatApiError(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (creds) refresh(creds)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function onLogin() {
    const next = { username: username.trim(), password }
    saveCredentials(next)
    setCreds(next)
    await refresh(next)
  }

  async function onLogout() {
    clearCredentials()
    setCreds(null)
    setMe(null)
    setError(null)
    setUsername('')
    setPassword('')
  }

  async function onPurchase() {
    if (!creds) return
    setError(null)
    setLoading(true)
    try {
      const data = await purchaseSubscription(plan, creds)
      setMe(data)
    } catch (e) {
      setError(formatApiError(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card
        title="Логин (HTTP Basic)"
        subtitle="Пароль хранится в sessionStorage (только для демо). В реальном продукте лучше JWT/сессии."
      >
        <div className="grid gap-3">
          <Input label="Username" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
          <Input
            label="Password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
          <div className="flex flex-wrap items-center gap-2">
            <Button onClick={onLogin} disabled={loading || username.trim().length === 0 || password.length === 0}>
              {loading ? 'Проверяем…' : 'Войти'}
            </Button>
            <Button variant="secondary" onClick={() => creds && refresh(creds)} disabled={loading || !creds}>
              Обновить
            </Button>
            <Button variant="danger" onClick={onLogout} disabled={loading}>
              Выйти
            </Button>
          </div>
          {error ? (
            <div className="rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-100">
              {error}
            </div>
          ) : null}
        </div>
      </Card>

      <Card title="Подписка" subtitle="Эндпоинты: GET /api/v1/subscription/me, POST /api/v1/subscription/purchase">
        {!me ? (
          <div className="text-sm text-slate-400">Залогиньтесь, чтобы увидеть статус подписки.</div>
        ) : (
          <div className="grid gap-4">
            <div className="rounded-xl border border-white/10 bg-slate-950/40 p-3">
              <div className="text-xs text-slate-400">Пользователь</div>
              <div className="text-sm font-medium">{me.username}</div>
              <div className="mt-2 grid gap-1 text-sm">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-slate-400">Subscribed</span>
                  <span className={me.subscribed ? 'text-emerald-300' : 'text-rose-300'}>
                    {me.subscribed ? 'YES' : 'NO'}
                  </span>
                </div>
                <div className="flex items-center justify-between gap-2">
                  <span className="text-slate-400">Expires at</span>
                  <span className="text-slate-200">{formatDate(me.subscriptionExpiresAt)}</span>
                </div>
              </div>
            </div>

            <div className="rounded-xl border border-white/10 bg-slate-950/40 p-3">
              <div className="text-sm font-medium">Купить/продлить</div>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <select
                  className="rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-sm text-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-400/20"
                  value={plan}
                  onChange={(e) => setPlan(e.target.value as SubscriptionPlan)}
                  disabled={loading || !creds}
                >
                  <option value="MONTHLY">MONTHLY (30d)</option>
                  <option value="YEARLY">YEARLY (365d)</option>
                </select>
                <Button onClick={onPurchase} disabled={loading || !creds}>
                  {loading ? 'Покупаем…' : 'Купить'}
                </Button>
              </div>
            </div>
          </div>
        )}
      </Card>
    </div>
  )
}


