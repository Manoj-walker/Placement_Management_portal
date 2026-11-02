// Small date helpers shared across pages (static fixtures use ISO "YYYY-MM-DD")

export function fmtDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso + (iso.length === 10 ? "T00:00:00" : ""));
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
}

export function fmtTime(iso) {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function startOfToday() {
  const t = new Date();
  t.setHours(0, 0, 0, 0);
  return t;
}

export function isClosed(lastDate) {
  if (!lastDate) return false;
  const d = new Date(lastDate + "T23:59:59");
  return d.getTime() < startOfToday().getTime();
}

export function daysLeft(lastDate) {
  if (!lastDate) return null;
  const d = new Date(lastDate + "T23:59:59");
  return Math.ceil((d.getTime() - startOfToday().getTime()) / 86400000);
}
