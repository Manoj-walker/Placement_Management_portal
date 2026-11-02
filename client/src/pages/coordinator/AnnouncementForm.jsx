import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { api } from "../../api/client.js";
import { mapCompany, toAnnouncementPayload } from "../../api/mappers.js";
import { branchOptions } from "../../data/companies.js";
import PageHeader from "../../components/PageHeader.jsx";

const EMPTY = {
  companyName: "",
  role: "",
  details: "",
  ctc: "",
  location: "",
  jdFileName: "",
  lastDate: "",
  testDate: "",
  interviewDate: "",
  venue: "",
  testLink: "",
  branches: [],
  minCgpa: "",
  maxBacklogs: "",
  batch: "2026",
  rounds: [{ name: "", date: "" }],
};

function blankForm() {
  return { ...EMPTY, branches: [], rounds: [{ name: "", date: "" }] };
}

// F4/F6/U2 — add/edit announcement: company info, eligibility, rounds, dates, JD file
export default function AnnouncementForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const editingId = id ?? null;

  const [form, setForm] = useState(blankForm);
  const [loaded, setLoaded] = useState(!editingId);
  const [jdFile, setJdFile] = useState(null);
  const [error, setError] = useState("");
  const [saved, setSaved] = useState(false);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!editingId) return undefined;
    let alive = true;
    api
      .get(`/api/announcements/${editingId}`)
      .then((data) => {
        if (!alive) return;
        const c = mapCompany(data);
        setForm({
          companyName: c.companyName,
          role: c.role,
          details: c.details,
          ctc: c.ctc,
          location: c.location,
          jdFileName: c.jdFileName,
          lastDate: c.lastDate,
          testDate: c.testDate,
          interviewDate: c.interviewDate,
          venue: c.venue,
          testLink: c.testLink,
          branches: [...c.eligibility.branches],
          minCgpa: c.eligibility.minCgpa,
          maxBacklogs: c.eligibility.maxBacklogs,
          batch: c.eligibility.batch,
          rounds: c.rounds.length ? c.rounds.map((r) => ({ ...r })) : [{ name: "", date: "" }],
        });
        setLoaded(true);
      })
      .catch((err) => {
        if (alive) {
          setError(err.message);
          setLoaded(true);
        }
      });
    return () => {
      alive = false;
    };
  }, [editingId]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const toggleBranch = (b) =>
    setForm({
      ...form,
      branches: form.branches.includes(b)
        ? form.branches.filter((x) => x !== b)
        : [...form.branches, b],
    });

  const setRound = (i, k, v) => {
    const rounds = form.rounds.map((r, idx) => (idx === i ? { ...r, [k]: v } : r));
    setForm({ ...form, rounds });
  };
  const addRound = () => setForm({ ...form, rounds: [...form.rounds, { name: "", date: "" }] });
  const removeRound = (i) => setForm({ ...form, rounds: form.rounds.filter((_, idx) => idx !== i) });

  const onFile = (e) => {
    const f = e.target.files?.[0];
    if (!f) return;
    // U2 exception: reject wrong type or oversized file
    if (!/\.(pdf|docx?)$/i.test(f.name)) {
      setError("Only PDF or Word files are allowed for the JD.");
      return;
    }
    if (f.size > 5 * 1024 * 1024) {
      setError("File too big — JD must be under 5 MB.");
      return;
    }
    setError("");
    setJdFile(f);
    setForm({ ...form, jdFileName: f.name });
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!form.companyName.trim() || !form.role.trim()) {
      setError("Company name and job role are required.");
      return;
    }
    if (!form.lastDate) {
      setError("Last date is required.");
      return;
    }
    if (form.branches.length === 0) {
      setError("Select at least one eligible branch.");
      return;
    }
    setError("");
    setBusy(true);
    try {
      const payload = toAnnouncementPayload(form);
      const saved = editingId
        ? await api.put(`/api/announcements/${editingId}`, payload)
        : await api.post("/api/announcements", payload);

      if (jdFile && saved?.id) {
        const body = new FormData();
        body.append("file", jdFile);
        await api.upload(`/api/announcements/${saved.id}/jd`, body);
      }

      setSaved(true);
      setTimeout(() => navigate("/coordinator"), 700);
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  };

  const input = "mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500";
  const label = "block text-xs font-semibold text-slate-600 uppercase tracking-wide";

  if (!loaded) {
    return <p className="text-center py-16 text-sm text-slate-500">Loading announcement…</p>;
  }

  return (
    <div className="max-w-3xl">
      <Link to="/coordinator" className="text-sm text-slate-500 hover:text-slate-700">
        ← Back to announcements
      </Link>
      <div className="mt-3">
        <PageHeader
          title={editingId ? `Edit: ${form.companyName || "announcement"}` : "New Announcement"}
          description="Eligible students receive an in-app notification and email as soon as you publish (U5)."
        />
      </div>

      <form onSubmit={submit} className="space-y-4">
        {/* Company details (F4) */}
        <section className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="font-semibold text-slate-900 mb-3">Company details</h2>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className={label} htmlFor="cname">Company name *</label>
              <input id="cname" className={input} value={form.companyName} onChange={set("companyName")} placeholder="e.g. TCS" />
            </div>
            <div>
              <label className={label} htmlFor="crole">Job role *</label>
              <input id="crole" className={input} value={form.role} onChange={set("role")} placeholder="e.g. Graduate Engineer" />
            </div>
            <div>
              <label className={label} htmlFor="ctc">CTC (₹ LPA) *</label>
              <input id="ctc" type="number" step="0.1" min="0" className={input} value={form.ctc} onChange={set("ctc")} placeholder="e.g. 9" />
            </div>
            <div>
              <label className={label} htmlFor="loc">Location</label>
              <input id="loc" className={input} value={form.location} onChange={set("location")} placeholder="e.g. Bangalore" />
            </div>
          </div>
          <div className="mt-4">
            <label className={label} htmlFor="details">Role details</label>
            <textarea id="details" rows="3" className={input} value={form.details} onChange={set("details")} placeholder="About the role, training, work culture…" />
          </div>
          <div className="mt-4">
            <label className={label} htmlFor="jd">JD file (PDF/DOC, max 5 MB)</label>
            <input id="jd" type="file" accept=".pdf,.doc,.docx" onChange={onFile} className="mt-1.5 block w-full text-sm text-slate-600 file:mr-3 file:rounded-lg file:border-0 file:bg-indigo-50 file:px-3 file:py-1.5 file:text-xs file:font-semibold file:text-indigo-700 hover:file:bg-indigo-100" />
            {form.jdFileName && <p className="mt-1 text-xs text-emerald-700">Attached: {form.jdFileName}</p>}
          </div>
        </section>

        {/* Eligibility (F6) */}
        <section className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="font-semibold text-slate-900 mb-3">Eligibility criteria</h2>
          <div>
            <span className={label}>Eligible branches *</span>
            <div className="mt-1.5 flex flex-wrap gap-2">
              {branchOptions.map((b) => (
                <button
                  type="button"
                  key={b}
                  onClick={() => toggleBranch(b)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium border ${
                    form.branches.includes(b)
                      ? "bg-indigo-50 border-indigo-400 text-indigo-700"
                      : "bg-white border-slate-300 text-slate-600 hover:bg-slate-50"
                  }`}
                >
                  {b}
                </button>
              ))}
            </div>
          </div>
          <div className="mt-4 grid gap-4 sm:grid-cols-3">
            <div>
              <label className={label} htmlFor="cgpa">Minimum CGPA</label>
              <input id="cgpa" type="number" step="0.1" min="0" max="10" className={input} value={form.minCgpa} onChange={set("minCgpa")} placeholder="e.g. 6.5" />
            </div>
            <div>
              <label className={label} htmlFor="bl">Max backlogs</label>
              <input id="bl" type="number" min="0" className={input} value={form.maxBacklogs} onChange={set("maxBacklogs")} placeholder="e.g. 0" />
            </div>
            <div>
              <label className={label} htmlFor="batch">Batch</label>
              <select id="batch" className={input} value={form.batch} onChange={set("batch")}>
                <option value="2026">2026</option>
                <option value="2027">2027</option>
                <option value="2028">2028</option>
              </select>
            </div>
          </div>
        </section>

        {/* Rounds + dates (F5/F6) */}
        <section className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="font-semibold text-slate-900 mb-3">Rounds & schedule</h2>

          <div className="space-y-2">
            {form.rounds.map((r, i) => (
              <div key={i} className="flex gap-2 items-center">
                <input className={input} value={r.name} onChange={(e) => setRound(i, "name", e.target.value)} placeholder={`Round ${i + 1} name (e.g. Online Test)`} />
                <input type="date" className={`${input} w-40 shrink-0`} value={r.date} onChange={(e) => setRound(i, "date", e.target.value)} />
                {form.rounds.length > 1 && (
                  <button type="button" onClick={() => removeRound(i)} className="shrink-0 h-8 w-8 rounded-lg border border-red-200 text-red-500 hover:bg-red-50" aria-label="Remove round">
                    ✕
                  </button>
                )}
              </div>
            ))}
          </div>
          <button type="button" onClick={addRound} className="mt-2 text-xs font-semibold text-indigo-600 hover:text-indigo-700">
            + Add round
          </button>

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <div>
              <label className={label} htmlFor="ld">Last date (registration) *</label>
              <input id="ld" type="date" className={input} value={form.lastDate} onChange={set("lastDate")} />
            </div>
            <div>
              <label className={label} htmlFor="td">Test date</label>
              <input id="td" type="date" className={input} value={form.testDate} onChange={set("testDate")} />
            </div>
            <div>
              <label className={label} htmlFor="id2">Interview date</label>
              <input id="id2" type="date" className={input} value={form.interviewDate} onChange={set("interviewDate")} />
            </div>
            <div>
              <label className={label} htmlFor="ven">Venue</label>
              <input id="ven" className={input} value={form.venue} onChange={set("venue")} placeholder="e.g. Block A, Seminar Hall" />
            </div>
            <div className="sm:col-span-2">
              <label className={label} htmlFor="tl">Test link</label>
              <input id="tl" type="url" className={input} value={form.testLink} onChange={set("testLink")} placeholder="https://…" />
            </div>
          </div>
        </section>

        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">{error}</p>
        )}
        {saved && (
          <p className="text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg px-3 py-2">
            Announcement saved — notifications sent to eligible students ✓
          </p>
        )}

        <div className="flex justify-end gap-2">
          <Link to="/coordinator" className="px-4 py-2 rounded-lg text-sm font-semibold border border-slate-300 text-slate-700 hover:bg-white">
            Cancel
          </Link>
          <button type="submit" disabled={busy} className="px-5 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-400">
            {busy ? "Saving…" : editingId ? "Save changes" : "Publish announcement"}
          </button>
        </div>
      </form>
    </div>
  );
}
