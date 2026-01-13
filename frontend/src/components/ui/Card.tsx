export function Card({
  title,
  subtitle,
  children,
}: {
  title?: string
  subtitle?: string
  children: React.ReactNode
}) {
  return (
    <section className="rounded-2xl border border-white/10 bg-white/5 p-5 shadow-sm">
      {(title || subtitle) && (
        <header className="mb-4">
          {title ? <h2 className="text-base font-semibold tracking-tight">{title}</h2> : null}
          {subtitle ? <p className="mt-1 text-sm text-slate-400">{subtitle}</p> : null}
        </header>
      )}
      {children}
    </section>
  )
}


