import { Navigate, Outlet } from "react-router-dom";
import { useSession, ROLE_HOME } from "../context/SessionContext.jsx";

// Guards nested routes by role: no session -> /login, wrong role -> own home
export default function ProtectedRoute({ role, children }) {
  const { user, loading } = useSession();

  if (loading) {
    return (
      <div className="min-h-screen grid place-items-center bg-slate-50">
        <p className="text-sm text-slate-500">Restoring your session…</p>
      </div>
    );
  }
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== role) return <Navigate to={ROLE_HOME[user.role]} replace />;

  return children ?? <Outlet />;
}
