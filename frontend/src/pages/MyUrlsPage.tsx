import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { myUrls } from '../api/endpoints'
import { formatApiError } from '../api/http'
import type { UserLinkDto } from '../api/types'
import { loadCredentials } from '../auth/credentials'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'

function formatDate(iso: string) {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString()
}

export function MyUrlsPage() {
  const [items, setItems] = useState<UserLinkDto[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const creds = loadCredentials()

  async function refresh() {
    const current = loadCredentials()
    if (!current) {
      setError('Нужно войти (HTTP Basic), чтобы посмотреть список ссылок.')
      setItems([])
      return
    }
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
    refresh()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function copy(text: string) {
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
        subtitle="Берётся с сервера: GET /api/v1/url/me (требует авторизацию). Сортировка — как отдаёт бэкенд (desc по createdAt)."
      >
        <div className="flex flex-wrap items-center gap-2">
          <Button onClick={refresh} disabled={loading}>
            {loading ? 'Обновляем…' : 'Обновить'}
          </Button>
          <Link to="/account">
            <Button variant="secondary">Войти/сменить пользователя</Button>
          </Link>
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
                  <div className="text-xs text-slate-500">создано: {formatDate(it.createdAt)}</div>
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

                <div className="mt-2 text-xs text-slate-500">
                  hash: <span className="font-mono">{it.hash}</span>
                </div>
              </div>
            ))}
          </div>
        ) : null}
      </Card>
    </div>
  )
}


