import { useEffect, useState } from "react";
import { api } from "../../api/client.js";
import { mapNotification } from "../../api/mappers.js";
import { fmtTime } from "../../utils/dates.js";
import PageHeader from "../../components/PageHeader.jsx";

const TYPE_ICON = {
  announcement: "📣",
  reminder: "⏰",
  result: "📋",
  update: "✏️",
};

// F11/U5 — in-app notifications for the student (persisted in Oracle/H2)
export default function Notifications() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;
    api
      .get("/api/notifications")
      .then((data) => {
        if (alive) setItems(data.map(mapNotification));
      })
      .catch((err) => alive && setError(err.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, []);

  const markAllRead = async () => {
    try {
      await api.patch("/api/notifications/read-all");
      setItems((list) => list.map((n) => ({ ...n, read: true })));
      window.dispatchEvent(new Event("pn:notifications-changed"));
    } catch (err) {
      setError(err.message);
    }
  };

  const toggleRead = async (id) => {
    const target = items.find((n) => n.id === id);
    if (!target) return;
    const nextRead = !target.read;
    setItems((list) => list.map((n) => (n.id === id ? { ...n, read: nextRead } : n)));
    try {
      await api.patch(`/api/notifications/${id}/read`, { read: nextRead });
      window.dispatchEvent(new Event("pn:notifications-changed"));
    } catch (err) {
      setItems((list) => list.map((n) => (n.id === id ? { ...n, read: target.read } : n)));
      setError(err.message);
    }
  };

  const unread = items.filter((n) => !n.read).length;

  return (
    <div>
      <PageHeader
        title="Notifications"
        description={`${unread} unread • announcements, reminders and results`}
        action={
          unread > 0 && (
            <button
              onClick={markAllRead}
              className="text-sm font-medium text-indigo-600 border border-indigo-300 rounded-lg px-3 py-1.5 hover:bg-indigo-50"
            >
              Mark all read
            </button>
          )
        }
      />

      {error && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      {loading ? (
        <p className="text-center py-10 text-sm text-slate-500">Loading notifications…</p>
      ) : items.length === 0 ? (
        <div className="text-center py-14 bg-white rounded-xl border border-dashed border-slate-300">
          <p className="text-4xl">🔔</p>
          <p className="mt-3 font-medium text-slate-700">No notifications yet</p>
        </div>
      ) : (
        <ul className="space-y-2">
          {items.map((n) => (
            <li
              key={n.id}
              onClick={() => toggleRead(n.id)}
              className={`cursor-pointer flex items-start gap-3 rounded-xl border px-4 py-3 transition-colors ${
                n.read
                  ? "bg-white border-slate-200"
                  : "bg-indigo-50/60 border-indigo-200"
              }`}
            >
              <span className="text-lg leading-none mt-0.5">{TYPE_ICON[n.type] ?? "🔔"}</span>
              <div className="min-w-0 flex-1">
                <p className={`text-sm ${n.read ? "text-slate-600" : "text-slate-900 font-medium"}`}>
                  {n.message}
                </p>
                <p className="mt-0.5 text-xs text-slate-400">{fmtTime(n.time)}</p>
              </div>
              {!n.read && <span className="mt-1.5 h-2 w-2 rounded-full bg-indigo-600 shrink-0" />}
            </li>
          ))}
        </ul>
      )}
      <p className="mt-4 text-xs text-slate-400">
        Tip: click a notification to toggle read/unread — changes are saved to the database.
      </p>
    </div>
  );
}
