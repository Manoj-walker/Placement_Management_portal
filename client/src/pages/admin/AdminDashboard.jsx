import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../api/client.js";
import { mapCompany, mapUser } from "../../api/mappers.js";
import PageHeader from "../../components/PageHeader.jsx";
import Badge from "../../components/Badge.jsx";

// Admin home — system overview stats (GET /api/admin/stats)
export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [blocked, setBlocked] = useState([]);
  const [drives, setDrives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;
    Promise.all([
      api.get("/api/admin/stats"),
      api.get("/api/admin/users"),
      api.get("/api/announcements?archived=true"),
    ])
      .then(([statsData, usersData, drivesData]) => {
        if (!alive) return;
        setStats(statsData);
        setBlocked(usersData.map(mapUser).filter((u) => u.status === "blocked"));
        setDrives(drivesData.map(mapCompany));
        setError("");
      })
      .catch((err) => alive && setError(err.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, []);

  const activeDrives = drives.filter((c) => !c.isArchived);

  const cards = stats
    ? [
        {
          label: "Total users",
          value: stats.totalStudents + stats.totalCoordinators + stats.totalAdmins,
          color: "text-slate-900",
        },
        { label: "Students", value: stats.totalStudents, color: "text-indigo-700" },
        { label: "Coordinators", value: stats.totalCoordinators, color: "text-sky-700" },
        { label: "Blocked accounts", value: stats.blockedUsers, color: "text-red-600" },
        { label: "Active drives", value: stats.activeDrives, color: "text-emerald-700" },
        { label: "Applications received", value: stats.applications, color: "text-violet-700" },
        { label: "Emails sent", value: stats.emailsSent, color: "text-amber-600" },
        { label: "Unread notifications", value: stats.unreadNotifications, color: "text-slate-700" },
      ]
    : [];

  return (
    <div>
      <PageHeader
        title="Admin Dashboard"
        description="Overview of accounts, drives and notifications."
        action={
          <Link
            to="/admin/users"
            className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700"
          >
            Manage users →
          </Link>
        }
      />

      {error && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      {loading ? (
        <p className="text-center py-10 text-sm text-slate-500">Loading dashboard…</p>
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {cards.map((s) => (
              <div key={s.label} className="bg-white rounded-xl border border-slate-200 p-5">
                <div className={`text-3xl font-bold ${s.color}`}>{s.value}</div>
                <div className="mt-1 text-sm text-slate-500">{s.label}</div>
              </div>
            ))}
          </div>

          <div className="mt-6 grid gap-4 lg:grid-cols-2">
            <section className="bg-white rounded-xl border border-slate-200 p-5">
              <h2 className="font-semibold text-slate-900">Blocked accounts</h2>
              {blocked.length === 0 ? (
                <p className="mt-2 text-sm text-slate-400">No blocked accounts.</p>
              ) : (
                <ul className="mt-3 space-y-2">
                  {blocked.map((u) => (
                    <li key={u.id} className="flex items-center justify-between text-sm bg-slate-50 border border-slate-200 rounded-lg px-3 py-2">
                      <span>
                        <span className="font-medium text-slate-800">{u.name}</span>
                        <span className="text-slate-500"> • {u.email}</span>
                      </span>
                      <Badge color="red">Blocked</Badge>
                    </li>
                  ))}
                </ul>
              )}
            </section>

            <section className="bg-white rounded-xl border border-slate-200 p-5">
              <h2 className="font-semibold text-slate-900">Latest drives</h2>
              <ul className="mt-3 space-y-2">
                {[...activeDrives]
                  .sort((a, b) => new Date(b.lastDate) - new Date(a.lastDate))
                  .slice(0, 4)
                  .map((c) => (
                    <li key={c.id} className="flex items-center justify-between text-sm bg-slate-50 border border-slate-200 rounded-lg px-3 py-2">
                      <span className="font-medium text-slate-800">{c.companyName}</span>
                      <span className="text-slate-500">₹{c.ctc} LPA</span>
                    </li>
                  ))}
                {activeDrives.length === 0 && (
                  <li className="text-sm text-slate-400">No active drives.</li>
                )}
              </ul>
            </section>
          </div>
        </>
      )}
    </div>
  );
}
