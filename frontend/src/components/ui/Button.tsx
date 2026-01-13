type Props = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger'
}

export function Button({ className = '', variant = 'primary', ...props }: Props) {
  const base =
    'inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition focus:outline-none focus:ring-2 focus:ring-indigo-400/40 disabled:cursor-not-allowed disabled:opacity-60'
  const styles: Record<NonNullable<Props['variant']>, string> = {
    primary: 'bg-indigo-500 text-white hover:bg-indigo-400',
    secondary: 'bg-white/10 text-white hover:bg-white/15',
    ghost: 'bg-transparent text-slate-200 hover:bg-white/5',
    danger: 'bg-rose-500/90 text-white hover:bg-rose-500',
  }
  return <button className={[base, styles[variant], className].join(' ')} {...props} />
}


