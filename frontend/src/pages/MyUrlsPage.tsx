import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { myUrls } from '../api/endpoints'
import { formatApiError } from '../api/http'
import type { UserLinkDto } from '../api/types'
import type { Credentials } from '../auth/credentials'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { useAuthCredentials } from '../auth/useAuthCredentials'
import { formatBackendDate } from '../utils/dates'

export function MyUrlsPage() {
  const [items, setItems] = useState<UserLinkDto[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const creds = useAuthCredentials()

  async function refreshWith(current: Credentials) {
    setError(null)
    setLoading(true)
    try {
      const data = await myUrls(current)
      setItems(data)
    } catch (e) {
      setItems([])
      setError(formatApiError(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!creds) {
      setError('Нужно войти (JWT), чтобы посмотреть список ссылок.')
      setItems([])
      return
    }
    void refreshWith(creds)
  }, [creds])

  async function copy(text: string | undefined | null) {
    if (!text) return
    try {
      await navigator.clipboard.writeText(text)
    } catch {
      // ignore
    }
  }

  return (
    <div className="grid gap-6">
      <Card
        title="Мои ссылки"
      >
        <div className="flex flex-wrap items-center gap-2">
          <Button
            onClick={() => {
              if (!creds) {
                setError('Нужно войти (JWT), чтобы посмотреть список ссылок.')
                setItems([])
                return
              }
              void refreshWith(creds)
            }}
            disabled={loading}
          >
            {loading ? 'Обновляем…' : 'Обновить'}
          </Button>
          {!creds ? (
            <Link to="/account">
              <Button variant="secondary">Войти/сменить пользователя</Button>
            </Link>
          ) : null}
        </div>

        {creds ? (
          <div className="mt-3 text-xs text-slate-400">
            Текущий пользователь: <span className="font-mono text-slate-300">{creds.username}</span>
          </div>
        ) : null}

        {error ? (
          <div className="mt-4 rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-100">
            {error}
          </div>
        ) : null}

        {!error && items.length === 0 ? <div className="mt-4 text-sm text-slate-400">Пока пусто.</div> : null}

        {items.length > 0 ? (
          <div className="mt-4 grid gap-3">
            {items.map((it) => (
              <div key={it.hash} className="rounded-xl border border-white/10 bg-slate-950/40 p-3">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div className="min-w-0">
                    <div className="text-xs text-slate-400">Оригинал</div>
                    <div className="break-all text-sm text-slate-200">{it.originalUrl}</div>
                  </div>
                  <div className="text-xs text-slate-500">создано: {formatBackendDate(it.createdAt)}</div>
                </div>

                <div className="mt-2 flex flex-wrap items-center justify-between gap-2">
                  <a className="break-all text-sm font-medium text-indigo-200 hover:underline" href={it.shortUrl}>
                    {it.shortUrl}
                  </a>
                  <div className="flex items-center gap-2">
                    <Button variant="ghost" onClick={() => copy(it.shortUrl)}>
                      Копировать
                    </Button>
                  </div>
                </div>

              </div>
            ))}
          </div>
        ) : null}
      </Card>
    </div>
  )
}


