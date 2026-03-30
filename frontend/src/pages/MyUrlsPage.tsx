import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { myUrls, urlStats } from '../api/endpoints'
import { formatApiError } from '../api/http'
import type { DailyStatDto, DimStatDto, UrlStatsResponse, UserLinkDto } from '../api/types'
import type { Credentials } from '../auth/credentials'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { useAuthCredentials } from '../auth/useAuthCredentials'
import { formatBackendDate } from '../utils/dates'

function isoDate(d: Date): string {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}

function daysAgo(n: number): Date {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return d
}

function httpStatus(err: unknown): number | null {
  if (!axios.isAxiosError(err)) return null
  return err.response?.status ?? null
}

function prettyDim(items: DimStatDto[], empty = '—') {
  if (!items || items.length === 0) return <div className="text-xs text-slate-500">{empty}</div>
  return (
    <div className="grid gap-1">
      {items.map((x) => (
        <div key={x.value} className="flex items-center justify-between gap-2 text-xs">
          <div className="min-w-0 truncate text-slate-200" title={x.value}>
            {x.value}
          </div>
          <div className="shrink-0 font-mono text-slate-400">{x.clicks}</div>
        </div>
      ))}
    </div>
  )
}

function analyticsSections(stats: UrlStatsResponse): Array<{ title: string; items: DimStatDto[]; className?: string }> {
  return [
    { title: 'Top referrers', items: stats.topReferrers },
    { title: 'Referrer categories', items: stats.referrerCategories },
    { title: 'Languages', items: stats.topLanguages },
    { title: 'Device types', items: stats.deviceTypes },
    { title: 'OS families', items: stats.osFamilies },
    { title: 'OS versions', items: stats.osVersions },
    { title: 'Browser families', items: stats.browserFamilies },
    { title: 'Browser versions', items: stats.browserVersions },
    { title: 'Countries', items: stats.countries },
    { title: 'Regions', items: stats.regions },
    { title: 'Cities', items: stats.cities },
    { title: 'Time zones', items: stats.timezones },
    { title: 'ASNs', items: stats.asns },
    { title: 'Providers', items: stats.providers },
    { title: 'Network types', items: stats.networkTypes },
    { title: 'Proxy status', items: stats.proxyStatuses },
    { title: 'VPN status', items: stats.vpnStatuses },
    { title: 'Tor status', items: stats.torStatuses },
    { title: 'Hours of day', items: stats.hoursOfDay },
    { title: 'Days of week', items: stats.daysOfWeek },
    { title: 'CH platform', items: stats.clientHintPlatforms },
    { title: 'CH platform version', items: stats.clientHintPlatformVersions },
    { title: 'CH mobile', items: stats.clientHintMobiles },
    { title: 'CH model', items: stats.clientHintModels },
  ]
}

function MiniBarChart({ daily }: { daily: DailyStatDto[] }) {
  const maxClicks = Math.max(1, ...daily.map((d) => d.clicks || 0))
  const maxUniques = Math.max(1, ...daily.map((d) => d.uniqueVisitors || 0))

  return (
    <div className="mt-2 rounded-xl border border-white/10 bg-slate-950/40 p-3">
      <div className="flex items-center justify-between gap-2">
        <div className="text-xs text-slate-400">График (дни)</div>
        <div className="flex items-center gap-3 text-[11px] text-slate-400">
          <div className="flex items-center gap-1">
            <span className="inline-block h-2 w-2 rounded-sm bg-indigo-400/80" />
            клики
          </div>
          <div className="flex items-center gap-1">
            <span className="inline-block h-2 w-2 rounded-sm bg-emerald-400/80" />
            уникальные
          </div>
        </div>
      </div>

      {daily.length === 0 ? (
        <div className="mt-2 text-xs text-slate-500">Нет данных за выбранный период.</div>
      ) : (
        <div className="mt-3 overflow-x-auto">
          <div className="flex items-end gap-1">
            {daily.map((d) => {
              const clicksH = Math.max(2, Math.round((d.clicks / maxClicks) * 56))
              const uniqH = Math.max(2, Math.round((d.uniqueVisitors / maxUniques) * 56))
              const title = `${d.day}\nclicks: ${d.clicks}\nunique: ${d.uniqueVisitors}`
              return (
                <div key={d.day} className="group flex w-4 flex-col items-center gap-1" title={title}>
                  <div className="relative h-14 w-4">
                    <div
                      className="absolute bottom-0 left-0 right-0 rounded-sm bg-indigo-400/70 group-hover:bg-indigo-400/90"
                      style={{ height: `${clicksH}px` }}
                    />
                    <div
                      className="absolute bottom-0 left-0 right-0 rounded-sm bg-emerald-400/70 mix-blend-screen group-hover:bg-emerald-400/90"
                      style={{ height: `${uniqH}px` }}
                    />
                  </div>
                  <div className="select-none font-mono text-[10px] text-slate-500">
                    {d.day.slice(8, 10)}
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

export function MyUrlsPage() {
  const [items, setItems] = useState<UserLinkDto[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const [openHash, setOpenHash] = useState<string | null>(null)
  const [statsByHash, setStatsByHash] = useState<Record<string, UrlStatsResponse | undefined>>({})
  const [statsErrorByHash, setStatsErrorByHash] = useState<Record<string, string | undefined>>({})
  const [statsLoadingHash, setStatsLoadingHash] = useState<string | null>(null)
  const [periodByHash, setPeriodByHash] = useState<Record<string, { from: string; to: string; top: number }>>({})

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
      setError('Нужно войти, чтобы посмотреть список ссылок.')
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

  function getPeriod(hash: string) {
    const existing = periodByHash[hash]
    if (existing) return existing
    const next = { from: isoDate(daysAgo(6)), to: isoDate(new Date()), top: 10 }
    setPeriodByHash((m) => ({ ...m, [hash]: next }))
    return next
  }

  async function loadStats(hash: string) {
    if (!creds) {
      setStatsErrorByHash((m) => ({ ...m, [hash]: 'Нужно войти, чтобы смотреть статистику.' }))
      return
    }
    const p = getPeriod(hash)
    setStatsLoadingHash(hash)
    setStatsErrorByHash((m) => ({ ...m, [hash]: undefined }))
    try {
      const data = await urlStats(hash, p.from, p.to, p.top, creds)
      setStatsByHash((m) => ({ ...m, [hash]: data }))
    } catch (e) {
      const status = httpStatus(e)
      if (status === 402) {
        setStatsErrorByHash((m) => ({ ...m, [hash]: 'Нужна активная подписка, чтобы смотреть статистику.' }))
      } else if (status === 403) {
        setStatsErrorByHash((m) => ({ ...m, [hash]: 'Нет доступа к статистике этой ссылки.' }))
      } else if (status === 401) {
        setStatsErrorByHash((m) => ({ ...m, [hash]: 'Нужно войти, чтобы смотреть статистику.' }))
      } else {
        setStatsErrorByHash((m) => ({ ...m, [hash]: formatApiError(e) }))
      }
    } finally {
      setStatsLoadingHash((cur) => (cur === hash ? null : cur))
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
                setError('Нужно войти, чтобы посмотреть список ссылок.')
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
                    <Button
                      variant="secondary"
                      onClick={() => {
                        const nextOpen = openHash === it.hash ? null : it.hash
                        setOpenHash(nextOpen)
                        if (nextOpen && !statsByHash[it.hash] && !statsLoadingHash) {
                          void loadStats(it.hash)
                        }
                      }}
                    >
                      {openHash === it.hash ? 'Скрыть статистику' : 'Статистика'}
                    </Button>
                    <Button variant="ghost" onClick={() => copy(it.shortUrl)}>
                      Копировать
                    </Button>
                  </div>
                </div>

                {openHash === it.hash ? (
                  <div className="mt-3 rounded-xl border border-white/10 bg-white/5 p-3">
                    <div className="flex flex-wrap items-end justify-between gap-2">
                      <div>
                        <div className="text-sm font-medium text-slate-100">Статистика</div>
                        <div className="text-xs text-slate-400">Клики и уникальные посетители по дням</div>
                      </div>
                      <div className="flex flex-wrap items-center gap-2">
                        <label className="text-xs text-slate-400">
                          from
                          <input
                            type="date"
                            className="ml-2 rounded-lg border border-white/10 bg-white/5 px-2 py-1 text-xs text-slate-100"
                            value={getPeriod(it.hash).from}
                            onChange={(e) =>
                              setPeriodByHash((m) => ({ ...m, [it.hash]: { ...getPeriod(it.hash), from: e.target.value } }))
                            }
                          />
                        </label>
                        <label className="text-xs text-slate-400">
                          to
                          <input
                            type="date"
                            className="ml-2 rounded-lg border border-white/10 bg-white/5 px-2 py-1 text-xs text-slate-100"
                            value={getPeriod(it.hash).to}
                            onChange={(e) =>
                              setPeriodByHash((m) => ({ ...m, [it.hash]: { ...getPeriod(it.hash), to: e.target.value } }))
                            }
                          />
                        </label>
                        <Button
                          variant="ghost"
                          disabled={statsLoadingHash === it.hash}
                          onClick={() => void loadStats(it.hash)}
                        >
                          {statsLoadingHash === it.hash ? 'Загрузка…' : 'Обновить'}
                        </Button>
                        <Button
                          variant="ghost"
                          onClick={() =>
                            setPeriodByHash((m) => ({ ...m, [it.hash]: { ...getPeriod(it.hash), from: isoDate(daysAgo(6)), to: isoDate(new Date()) } }))
                          }
                        >
                          7д
                        </Button>
                        <Button
                          variant="ghost"
                          onClick={() =>
                            setPeriodByHash((m) => ({ ...m, [it.hash]: { ...getPeriod(it.hash), from: isoDate(daysAgo(29)), to: isoDate(new Date()) } }))
                          }
                        >
                          30д
                        </Button>
                      </div>
                    </div>

                    {statsErrorByHash[it.hash] ? (
                      <div className="mt-3 rounded-xl border border-rose-500/30 bg-rose-500/10 p-3 text-sm text-rose-100">
                        <div>{statsErrorByHash[it.hash]}</div>
                        {statsErrorByHash[it.hash]?.includes('подписка') ? (
                          <div className="mt-2">
                            <Link to="/account" className="text-indigo-200 underline">
                              Перейти к подписке
                            </Link>
                          </div>
                        ) : null}
                      </div>
                    ) : null}

                    {statsByHash[it.hash] ? (
                      <div className="mt-3 grid gap-4">
                        <div className="rounded-xl border border-white/10 bg-slate-950/40 p-3">
                          <div className="text-xs text-slate-400">Daily</div>
                          <MiniBarChart daily={statsByHash[it.hash]!.daily} />
                          {statsByHash[it.hash]?.daily?.length ? (
                            <div className="mt-3 grid gap-1">
                              {statsByHash[it.hash]!.daily.map((d) => (
                                <div key={d.day} className="flex items-center justify-between gap-2 text-xs">
                                  <div className="font-mono text-slate-200">{d.day}</div>
                                  <div className="flex items-center gap-4">
                                    <div className="text-slate-400">
                                      клики: <span className="font-mono text-slate-200">{d.clicks}</span>
                                    </div>
                                    <div className="text-slate-400">
                                      уник.: <span className="font-mono text-slate-200">{d.uniqueVisitors}</span>
                                    </div>
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : null}
                        </div>

                        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
                          {analyticsSections(statsByHash[it.hash]!).map((section) => (
                            <div
                              key={section.title}
                              className={`rounded-xl border border-white/10 bg-slate-950/40 p-3 ${section.className ?? ''}`}
                            >
                              <div className="text-xs text-slate-400">{section.title}</div>
                              <div className="mt-2">{prettyDim(section.items)}</div>
                            </div>
                          ))}
                        </div>
                      </div>
                    ) : statsLoadingHash === it.hash ? (
                      <div className="mt-3 text-sm text-slate-400">Загрузка…</div>
                    ) : null}
                  </div>
                ) : null}

              </div>
            ))}
          </div>
        ) : null}
      </Card>
    </div>
  )
}


