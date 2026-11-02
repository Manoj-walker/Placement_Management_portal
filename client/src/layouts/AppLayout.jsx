import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate, Link, useLocation } from "react-router-dom";
import { useSession, ROLE_HOME } from "../context/SessionContext.jsx";
import { api } from "../api/client.js";

const NAV = {
  student: [
    { to: "/student", label: "Dashboard", end: true },
    { to: "/student/deadlines", label: "My Deadlines" },
    { to: "/student/notifications", label: "Notifications" },
    { to: "/student/profile", label: "Profile" },
  ],
  coordinator: [
    { to: "/coordinator", label: "Announcements", end: true },
    { to: "/coordinator/new", label: "New Announcement" },
  ],
  admin: [
    { to: "/admin", label: "Dashboard", end: true },
    { to: "/admin/users", label: "Users" },
  ],
};

const ROLE_LABEL = { student: "Student", coordinator: "Coordinator", admin: "Admin" };

function linkClass({ isActive }) {
  return [
    "px-3 py-1.5 rounded-lg text-sm font-medium transition-colors whitespace-nowrap",
    isActive
      ? "bg-indigo-50 text-indigo-700"
      : "text-slate-600 hover:bg-slate-100 hover:text-slate-900",
  ].join(" ");
}

// Main app chrome: role-based navigation and the signed-in account.
export default function AppLayout() {
  const { user, logout } = useSession();
  const navigate = useNavigate();
  const location = useLocation();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (user.role !== "student") return undefined;
    let alive = true;
    const loadUnread = () =>
      api
        .get("/api/notifications/unread-count")
        .then((data) => {
          if (alive) setUnread(Number(data.count) || 0);
        })
        .catch(() => {
          /* badge is optional */
        });
    loadUnread();
    const onChange = () => loadUnread();
    window.addEventListener("pn:notifications-changed", onChange);
    return () => {
      alive = false;
      window.removeEventListener("pn:notifications-changed", onChange);
    };
  }, [user.role, location.pathname]);

  const links = NAV[user.role] ?? [];

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="sticky top-0 z-20 bg-white border-b border-slate-200">
        <div className="mx-auto max-w-7xl px-4 h-14 flex items-center gap-3">
          {/* Brand */}
          <Link to={ROLE_HOME[user.role]} className="flex items-center gap-2 shrink-0">
            <div className="h-8 w-8 rounded-lg bg-indigo-600 text-white grid place-items-center text-xs font-bold">
              PN
            </div>
            <span className="font-semibold text-slate-800 hidden sm:block">
              Placement NIT
            </span>
          </Link>

          {/* Role nav */}
          <nav className="flex-1 flex items-center gap-1 overflow-x-auto">
            {links.map((l) => (
              <NavLink key={l.to} to={l.to} end={l.end} className={linkClass}>
                <span className="relative">
                  {l.label}
                  {l.label === "Notifications" && unread > 0 && (
                    <span className="absolute -top-1.5 -right-3 h-4 min-w-4 px-1 rounded-full bg-red-500 text-white text-[10px] grid place-items-center">
                      {unread}
                    </span>
                  )}
                </span>
              </NavLink>
            ))}
          </nav>

          {/* Signed-in user */}
          <div className="flex items-center gap-2 shrink-0">
            <div className="hidden sm:flex items-center gap-2">
              <div className="text-right leading-tight">
                <div className="text-sm font-medium text-slate-800">{user.name}</div>
                <div className="text-[11px] text-slate-500">{ROLE_LABEL[user.role]}</div>
              </div>
              <div className="h-8 w-8 rounded-full bg-indigo-100 text-indigo-700 grid place-items-center text-xs font-bold">
                {user.name.slice(0, 1).toUpperCase()}
              </div>
            </div>
            <button
              onClick={() => {
                logout();
                navigate("/login");
              }}
              className="text-xs font-medium text-slate-600 border border-slate-300 rounded-lg px-2.5 py-1.5 hover:bg-slate-50"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
