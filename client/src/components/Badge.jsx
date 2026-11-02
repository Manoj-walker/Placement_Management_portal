const STYLES = {
  green: "bg-emerald-50 text-emerald-700 border-emerald-200",
  red: "bg-red-50 text-red-700 border-red-200",
  amber: "bg-amber-50 text-amber-700 border-amber-200",
  slate: "bg-slate-100 text-slate-600 border-slate-200",
  indigo: "bg-indigo-50 text-indigo-700 border-indigo-200",
  blue: "bg-sky-50 text-sky-700 border-sky-200",
};

export default function Badge({ color = "slate", children, className = "" }) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-[11px] font-medium ${STYLES[color] ?? STYLES.slate} ${className}`}
    >
      {children}
    </span>
  );
}
