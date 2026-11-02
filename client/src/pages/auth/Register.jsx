import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useSession } from "../../context/SessionContext.jsx";
import { branchOptions } from "../../data/companies.js";

// F1 — student registration with college email, academics and password (U1)
export default function Register() {
  const { register } = useSession();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    rollNo: "",
    branch: "CSE",
    batch: "2026",
    cgpa: "",
    backlogs: 0,
    password: "",
    confirm: "",
  });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (!form.name.trim() || !form.email.trim() || !form.rollNo.trim() || !form.password) {
      setError("All fields are required.");
      return;
    }
    if (!form.email.includes("@")) {
      setError("Enter a valid college email address.");
      return;
    }
    if (form.password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }
    if (form.password !== form.confirm) {
      setError("Passwords do not match.");
      return;
    }
    setBusy(true);
    try {
      await register({
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
        rollNo: form.rollNo.trim(),
        branch: form.branch,
        batch: form.batch,
        cgpa: Number(form.cgpa) || 0,
        backlogs: Number(form.backlogs) || 0,
      });
      navigate("/student", { replace: true });
    } catch (err) {
      setError(err.message || "Registration failed.");
      setBusy(false);
    }
  };

  const input =
    "mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500";
  const label = "block text-xs font-semibold text-slate-600 uppercase tracking-wide";

  return (
    <div>
      <h1 className="text-xl font-bold text-slate-900">Student registration</h1>
      <p className="mt-1 text-sm text-slate-500">
        Only valid college email addresses can register.
      </p>

      <form onSubmit={submit} className="mt-6 space-y-4">
        <div>
          <label htmlFor="name" className={label}>Full name</label>
          <input id="name" value={form.name} onChange={set("name")} placeholder="Your name" className={input} />
        </div>

        <div>
          <label htmlFor="remail" className={label}>College email</label>
          <input id="remail" type="email" value={form.email} onChange={set("email")} placeholder="you@nit.edu" className={input} />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label htmlFor="roll" className={label}>Roll number</label>
            <input id="roll" value={form.rollNo} onChange={set("rollNo")} placeholder="21CS1001" className={input} />
          </div>
          <div>
            <label htmlFor="branch" className={label}>Branch</label>
            <select id="branch" value={form.branch} onChange={set("branch")} className={input}>
              {branchOptions.map((b) => (
                <option key={b} value={b}>{b}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-3">
          <div>
            <label htmlFor="batch" className={label}>Batch</label>
            <select id="batch" value={form.batch} onChange={set("batch")} className={input}>
              <option value="2026">2026</option>
              <option value="2027">2027</option>
              <option value="2028">2028</option>
            </select>
          </div>
          <div>
            <label htmlFor="cgpa" className={label}>CGPA</label>
            <input id="cgpa" type="number" step="0.01" min="0" max="10" value={form.cgpa} onChange={set("cgpa")} placeholder="8.0" className={input} />
          </div>
          <div>
            <label htmlFor="backlogs" className={label}>Backlogs</label>
            <input id="backlogs" type="number" min="0" value={form.backlogs} onChange={set("backlogs")} className={input} />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label htmlFor="pw" className={label}>Password</label>
            <input id="pw" type="password" value={form.password} onChange={set("password")} placeholder="Min 8 chars" className={input} />
          </div>
          <div>
            <label htmlFor="cpw" className={label}>Confirm</label>
            <input id="cpw" type="password" value={form.confirm} onChange={set("confirm")} placeholder="Repeat" className={input} />
          </div>
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
          {busy ? "Creating account…" : "Create account"}
        </button>
      </form>

      <p className="mt-4 text-center text-sm text-slate-600">
        Already registered?{" "}
        <Link to="/login" className="font-medium text-indigo-600 hover:text-indigo-700">
          Sign in
        </Link>
      </p>
    </div>
  );
}
