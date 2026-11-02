import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useSession, ROLE_HOME } from "../../context/SessionContext.jsx";

const CATEGORIES = [
  { value: "student", label: "Student" },
  { value: "coordinator", label: "Placement Coordinator" },
  { value: "admin", label: "Administrator" },
];

// U1 — login with category selection; role is verified against the account.
export default function Login() {
  const { login } = useSession();
  const navigate = useNavigate();

  const [category, setCategory] = useState("student");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (!email.trim() || !password.trim()) {
      setError("Email and password are required.");
      return;
    }
    if (!email.includes("@")) {
      setError("Enter a valid email address.");
      return;
    }
    setBusy(true);
    try {
      const user = await login(email.trim(), password);
      if (user.role !== category) {
        setError(
          `This account belongs to the ${user.role} category. Select "${CATEGORIES.find((c) => c.value === user.role)?.label}" and try again.`
        );
        setBusy(false);
        return;
      }
      navigate(ROLE_HOME[user.role] || "/", { replace: true });
    } catch (err) {
      setError(err.message || "Sign in failed.");
      setBusy(false);
    }
  };

  return (
    <div>
      <h1 className="text-xl font-bold text-slate-900">Sign in</h1>
      <p className="mt-1 text-sm text-slate-500">
        Placement Information Management & Notification System
      </p>

      <form onSubmit={submit} className="mt-6 space-y-4">
        {/* Category (F2) */}
        <div>
          <label className="block text-xs font-semibold text-slate-600 uppercase tracking-wide">
            Category
          </label>
          <div className="mt-1.5 grid grid-cols-3 gap-2">
            {CATEGORIES.map((c) => (
              <button
                type="button"
                key={c.value}
                onClick={() => setCategory(c.value)}
                className={`px-2 py-2 rounded-lg text-xs font-medium border transition-colors ${
                  category === c.value
                    ? "bg-indigo-50 border-indigo-400 text-indigo-700"
                    : "bg-white border-slate-300 text-slate-600 hover:bg-slate-50"
                }`}
              >
                {c.label}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label htmlFor="email" className="block text-xs font-semibold text-slate-600 uppercase tracking-wide">
            College email
          </label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@nit.edu"
            className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div>
          <label htmlFor="password" className="block text-xs font-semibold text-slate-600 uppercase tracking-wide">
            Password
          </label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            className="mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={busy}
          className="w-full rounded-lg bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-400 text-white text-sm font-semibold py-2.5 transition-colors"
        >
          {busy ? "Signing in…" : "Sign in"}
        </button>
      </form>

      <p className="mt-4 text-center text-sm text-slate-600">
        New student?{" "}
        <Link to="/register" className="font-medium text-indigo-600 hover:text-indigo-700">
          Register
        </Link>
      </p>

      <div className="mt-4 text-xs text-slate-500 bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 space-y-1">
        <p className="font-semibold text-slate-600">Demo accounts (seeded on first run)</p>
        <p>student@nit.edu / Student@123</p>
        <p>coordinator@nit.edu / Coord@123</p>
        <p>admin@nit.edu / Admin@123</p>
      </div>
    </div>
  );
}
