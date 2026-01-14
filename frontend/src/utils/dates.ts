export function parseBackendDate(value: unknown): Date | null {
  if (value === null || value === undefined) return null

  // Backend sometimes returns epoch seconds with fraction (e.g. 1770934657.488)
  if (typeof value === 'number' && Number.isFinite(value)) {
    // heuristics: seconds are ~1e9..1e10, millis are ~1e12..1e13
    const ms = value < 1e12 ? value * 1000 : value
    const d = new Date(ms)
    return Number.isNaN(d.getTime()) ? null : d
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (trimmed.length === 0) return null
    // numeric string (seconds or millis)
    if (/^-?\d+(\.\d+)?$/.test(trimmed)) {
      const n = Number(trimmed)
      if (!Number.isFinite(n)) return null
      const ms = n < 1e12 ? n * 1000 : n
      const d = new Date(ms)
      return Number.isNaN(d.getTime()) ? null : d
    }
    const d = new Date(trimmed)
    return Number.isNaN(d.getTime()) ? null : d
  }

  // Jackson LocalDateTime can come as array: [yyyy,MM,dd,HH,mm,ss,nano]
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0, nano = 0] = value as number[]
    if (![y, m, d].every((x) => typeof x === 'number')) return null
    const ms = typeof nano === 'number' ? Math.floor(nano / 1_000_000) : 0
    const dt = new Date(Date.UTC(y, m - 1, d, hh, mm, ss, ms))
    return Number.isNaN(dt.getTime()) ? null : dt
  }

  return null
}

export function formatBackendDate(value: unknown): string {
  const d = parseBackendDate(value)
  if (!d) return '—'
  return d.toLocaleString()
}


