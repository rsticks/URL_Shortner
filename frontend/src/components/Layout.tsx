import { NavLink } from 'react-router-dom'

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-white/10 bg-slate-950/70 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between gap-4 px-4 py-3">
          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-indigo-500/20 ring-1 ring-indigo-400/30" />
            <div className="leading-tight">
              <div className="text-sm font-semibold tracking-tight">URL Shortener</div>
              <div className="text-xs text-slate-400">Frontend для вашего API</div>
            </div>
          </div>
          <nav className="flex items-center gap-1">
            <TopLink to="/">Сократить</TopLink>
            <TopLink to="/my-urls">Мои ссылки</TopLink>
            <TopLink to="/register">Регистрация</TopLink>
            <TopLink to="/account">Аккаунт</TopLink>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>

      <footer className="border-t border-white/10 py-6">
        <div className="mx-auto max-w-5xl px-4 text-xs text-slate-400">
          API: <span className="font-mono">/api/v1</span> · Auth: <span className="font-mono">HTTP Basic</span>
        </div>
      </footer>
    </div>
  )
}

function TopLink({ to, children }: { to: string; children: React.ReactNode }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        [
          'rounded-lg px-3 py-2 text-sm transition',
          isActive ? 'bg-white/10 text-white' : 'text-slate-300 hover:bg-white/5 hover:text-white',
        ].join(' ')
      }
    >
      {children}
    </NavLink>
  )
}


