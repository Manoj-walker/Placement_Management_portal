import { useState } from "react";
import { useSession } from "../../context/SessionContext.jsx";
import { api } from "../../api/client.js";
import { branchOptions } from "../../data/companies.js";
import PageHeader from "../../components/PageHeader.jsx";

// F3 — student profile: branch, batch, CGPA, backlogs (used for eligibility)
export default function Profile() {
  const { user, refresh } = useSession();
  const [form, setForm] = useState({
    name: user.name ?? "",
    email: user.email ?? "",
    rollNo: user.rollNo ?? "",
    branch: user.branch || "CSE",
    batch: user.batch || "2026",
    cgpa: user.cgpa ?? "",
    backlogs: user.backlogs ?? 0,
  });
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => {
    setForm({ ...form, [k]: e.target.value });
    setSaved(false);
  };

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      await api.put("/api/me/profile", {
        name: form.name,
        rollNo: form.rollNo,
        branch: form.branch,
        batch: form.batch,
        cgpa: Number(form.cgpa) || 0,
        backlogs: Number(form.backlogs) || 0,
      });
      await refresh();
      setSaved(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const field = "mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500";
  const label = "block text-xs font-semibold text-slate-600 uppercase tracking-wide";

  return (
    <div className="max-w-2xl">
      <PageHeader
        title="Student Profile"
        description="Your academic details decide eligibility for each drive."
      />

      <form onSubmit={submit} className="bg-white rounded-xl border border-slate-200 p-5 space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className={label} htmlFor="pname">Full name</label>
            <input id="pname" className={field} value={form.name} onChange={set("name")} />
          </div>
          <div>
            <label className={label} htmlFor="pemail">College email</label>
            <input id="pemail" className={`${field} bg-slate-50 text-slate-500`} value={form.email} readOnly />
          </div>
          <div>
            <label className={label} htmlFor="proll">Roll number</label>
            <input id="proll" className={field} value={form.rollNo} onChange={set("rollNo")} />
          </div>
          <div>
            <label className={label} htmlFor="pbranch">Branch</label>
            <select id="pbranch" className={field} value={form.branch} onChange={set("branch")}>
              {branchOptions.map((b) => (
                <option key={b} value={b}>{b}</option>
              ))}
            </select>
          </div>
          <div>
            <label className={label} htmlFor="pbatch">Batch</label>
            <select id="pbatch" className={field} value={form.batch} onChange={set("batch")}>
              <option value="2026">2026</option>
              <option value="2027">2027</option>
              <option value="2028">2028</option>
            </select>
          </div>
          <div>
            <label className={label} htmlFor="pcgpa">CGPA</label>
            <input
              id="pcgpa"
              type="number"
              step="0.01"
              min="0"
              max="10"
              className={field}
              value={form.cgpa}
              onChange={set("cgpa")}
            />
          </div>
          <div>
            <label className={label} htmlFor="pback">Active backlogs</label>
            <input
              id="pback"
              type="number"
              min="0"
              className={field}
              value={form.backlogs}
              onChange={set("backlogs")}
            />
          </div>
        </div>

        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
            {error}
          </p>
        )}

        {saved && (
          <p className="text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg px-3 py-2">
            Profile saved ✓ Your eligibility has been recalculated.
          </p>
        )}

        <div className="flex justify-end">
          <button
            type="submit"
            disabled={busy}
            className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-400"
          >
            {busy ? "Saving…" : "Save changes"}
          </button>
        </div>
      </form>
    </div>
  );
}
