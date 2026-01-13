type Props = React.InputHTMLAttributes<HTMLInputElement> & {
  label?: string
  hint?: string
}

export function Input({ label, hint, className = '', ...props }: Props) {
  return (
    <label className="block">
      {label ? <div className="mb-1 text-sm text-slate-200">{label}</div> : null}
      <input
        className={[
          'w-full rounded-lg border border-white/10 bg-white/5 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500',
          'focus:border-indigo-400/40 focus:outline-none focus:ring-2 focus:ring-indigo-400/20',
          className,
        ].join(' ')}
        {...props}
      />
      {hint ? <div className="mt-1 text-xs text-slate-400">{hint}</div> : null}
    </label>
  )
}


