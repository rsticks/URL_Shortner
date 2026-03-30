import { useState } from 'react'
import { createShortUrl } from '../api/endpoints'
import { formatApiError } from '../api/http'
import type { ResponseDto } from '../api/types'
import { useAuthCredentials } from '../auth/useAuthCredentials'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'
import { addToShortenHistory, clearShortenHistory, useShortenHistory } from '../state/shortenHistoryStore'

export function HomePage() {
  const [url, setUrl] = useState('')
  const [result, setResult] = useState<ResponseDto | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const history = useShortenHistory()
  const creds = useAuthCredentials()

  async function onShorten() {
    setError(null)
    setResult(null)
    setLoading(true)
    try {
      const data = await createShortUrl(url.trim(), creds)
      setResult(data)
      addToShortenHistory({ originalUrl: url.trim(), shortUrl: data.shortUrl, createdAt: new Date().toISOString() })
    } catch (e) {
      setError(formatApiError(e))
    } finally {
      setLoading(false)
    }
  }

  async function copy(text: string | undefined | null) {
    if (!text) return
    try {
      await navigator.clipboard.writeText(text)
    } catch {
      // ignore
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card
        title="Сокращение ссылки"
        subtitle="Введите длинный URL — получите короткую ссылку. Если вы залогинены, связь сохранится за пользователем."
      >
        <div className="grid gap-3">
          <Input
            label="Длинная ссылка"
            placeholder="https://example.com/some/very/long/url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
          />
          <div className="flex flex-wrap items-center gap-2">
            <Button onClick={onShorten} disabled={loading || url.trim().length === 0}>
              {loading ? 'Сокращаем…' : 'Сократить'}
            </Button>
            <Button
              variant="secondary"
              onClick={() => {
                setUrl('')
                setResult(null)
                setError(null)
              }}
              disabled={loading}
            >
              Очистить
            </Button>
          </div>

          {error ? (
            <div className="rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-100">
              {error}
            </div>
          ) : null}

          {result ? (
            <div className="rounded-xl border border-indigo-400/30 bg-indigo-500/10 p-3">
              <div className="text-xs text-slate-300">Короткая ссылка</div>
              <div className="mt-1 flex flex-wrap items-center justify-between gap-2">
                <a className="break-all text-sm font-medium text-indigo-200 hover:underline" href={result.shortUrl}>
                  {result.shortUrl}
                </a>
                <div className="flex items-center gap-2">
                  <Button variant="ghost" onClick={() => copy(result.shortUrl)}>
                    Копировать
                  </Button>
                </div>
              </div>
            </div>
          ) : null}
        </div>
      </Card>

      <Card
        title="История (в этой сессии)"
      >
        {history.length === 0 ? (
          <div className="text-sm text-slate-400">Пока пусто.</div>
        ) : (
          <div className="grid gap-3">
            {history.map((h) => (
              <div key={h.createdAt + h.shortUrl} className="rounded-xl border border-white/10 bg-slate-950/40 p-3">
                <div className="text-xs text-slate-400">Оригинал</div>
                <div className="break-all text-sm text-slate-200">{h.originalUrl}</div>
                <div className="mt-2 flex flex-wrap items-center justify-between gap-2">
                  <a className="break-all text-sm font-medium text-indigo-200 hover:underline" href={h.shortUrl}>
                    {h.shortUrl}
                  </a>
                  <Button variant="ghost" onClick={() => copy(h.shortUrl)}>
                    Копировать
                  </Button>
                </div>
              </div>
            ))}
            <div className="pt-1">
              <Button
                variant="secondary"
                onClick={() => {
                  clearShortenHistory()
                }}
              >
                Очистить историю
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  )
}


