import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api } from "../../api/client.js";
import { mapCompany } from "../../api/mappers.js";
import { fmtDate } from "../../utils/dates.js";
import Badge from "../../components/Badge.jsx";

// U6 — update schedule, test links and results (F7) for one company
export default function CoordinatorCompany() {
  const { id } = useParams();
  const [c, setC] = useState(null);
  const [applicants, setApplicants] = useState([]);
  const [sched, setSched] = useState({
    lastDate: "",
    testDate: "",
    interviewDate: "",
    venue: "",
    testLink: "",
  });
  const [saved, setSaved] = useState(false);
  const [shortlisted, setShortlisted] = useState([]); // student ids (numbers)
  const [selected, setSelected] = useState([]); // student ids (numbers)
  const [pick, setPick] = useState("");
  const [resultMsg, setResultMsg] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;
    setLoading(true);
    Promise.all([
      api.get(`/api/announcements/${id}`),
      api.get(`/api/announcements/${id}/applicants`),
      api.get(`/api/announcements/${id}/results`),
    ])
      .then(([announcement, rows, results]) => {
        if (!alive) return;
        const mapped = mapCompany(announcement);
        setC(mapped);
        setSched({
          lastDate: mapped.lastDate,
          testDate: mapped.testDate,
          interviewDate: mapped.interviewDate,
          venue: mapped.venue,
          testLink: mapped.testLink,
        });
        setApplicants(rows);
        setShortlisted((results.shortlisted ?? []).map(Number));
        setSelected((results.selected ?? []).map(Number));
        setError("");
      })
      .catch((err) => alive && setError(err.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [id]);

  if (loading) {
    return <p className="text-center py-16 text-sm text-slate-500">Loading drive…</p>;
  }

  if (!c || error) {
    return (
      <div className="text-center py-16">
        <p className="text-4xl">🤔</p>
        <p className="mt-3 font-medium text-slate-700">{error || "Company not found"}</p>
        <Link to="/coordinator" className="text-sm text-indigo-600 hover:underline">Back to announcements</Link>
      </div>
    );
  }

  const set = (k) => (e) => {
    setSched({ ...sched, [k]: e.target.value });
    setSaved(false);
  };

  const saveSchedule = async () => {
    setBusy(true);
    try {
      const updated = await api.patch(`/api/announcements/${id}/schedule`, {
        lastDate: sched.lastDate,
        testDate: sched.testDate,
        interviewDate: sched.interviewDate,
        venue: sched.venue,
        testLink: sched.testLink,
      });
      setC(mapCompany(updated));
      setSaved(true);
      setError("");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const addShortlist = () => {
    if (!pick) return;
    const studentId = Number(pick);
    if (shortlisted.includes(studentId)) return;
    setShortlisted([...shortlisted, studentId]);
    setPick("");
    setResultMsg("");
  };
  const promote = (studentId) => {
    if (!selected.includes(studentId)) setSelected([...selected, studentId]);
  };
  const demote = (studentId) => setSelected(selected.filter((s) => s !== studentId));
  const removeShort = (studentId) => {
    setShortlisted(shortlisted.filter((s) => s !== studentId));
    setSelected(selected.filter((s) => s !== studentId));
  };

  const postResult = async () => {
    setBusy(true);
    try {
      await api.post(`/api/announcements/${id}/results`, {
        type: "SHORTLISTED",
        userIds: shortlisted,
      });
      await api.post(`/api/announcements/${id}/results`, {
        type: "SELECTED",
        userIds: selected,
      });
      setResultMsg(
        `Result posted — ${shortlisted.length} shortlisted, ${selected.length} selected. Students notified ✓`
      );
      setError("");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const input = "mt-1.5 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500";
  const label = "block text-xs font-semibold text-slate-600 uppercase tracking-wide";
  const byId = (studentId) => applicants.find((s) => Number(s.id) === studentId);

  return (
    <div>
      <Link to="/coordinator" className="text-sm text-slate-500 hover:text-slate-700">← Back to announcements</Link>

      <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-slate-900">{c.companyName}</h1>
          <p className="text-sm text-slate-500">{c.role} • ₹{c.ctc} LPA</p>
        </div>
        <Link
          to={`/coordinator/edit/${c.id}`}
          className="px-3 py-1.5 rounded-lg text-sm font-medium border border-slate-300 text-slate-700 hover:bg-white"
        >
          Edit details
        </Link>
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        {/* Schedule / test link editor (U6) */}
        <section className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="font-semibold text-slate-900">Schedule & test link</h2>
          <div className="mt-3 grid gap-4 sm:grid-cols-2">
            <div>
              <label className={label} htmlFor="s-ld">Last date</label>
              <input id="s-ld" type="date" className={input} value={sched.lastDate} onChange={set("lastDate")} />
            </div>
            <div>
              <label className={label} htmlFor="s-td">Test date</label>
              <input id="s-td" type="date" className={input} value={sched.testDate} onChange={set("testDate")} />
            </div>
            <div>
              <label className={label} htmlFor="s-id">Interview date</label>
              <input id="s-id" type="date" className={input} value={sched.interviewDate} onChange={set("interviewDate")} />
            </div>
            <div>
              <label className={label} htmlFor="s-ven">Venue</label>
              <input id="s-ven" className={input} value={sched.venue} onChange={set("venue")} />
            </div>
            <div className="sm:col-span-2">
              <label className={label} htmlFor="s-tl">Test link</label>
              <input id="s-tl" type="url" className={input} value={sched.testLink} onChange={set("testLink")} placeholder="https://…" />
            </div>
          </div>

          {saved && (
            <p className="mt-3 text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg px-3 py-2">
              Schedule updated — applied students notified ✓
            </p>
          )}

          <div className="mt-4 flex justify-end">
            <button
              onClick={saveSchedule}
              disabled={busy}
              className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-400"
            >
              {busy ? "Saving…" : "Save schedule"}
            </button>
          </div>

          <div className="mt-4 border-t border-slate-100 pt-3 text-sm text-slate-600 space-y-1">
            <p>Current rounds: {c.rounds.map((r) => `${r.name} (${fmtDate(r.date)})`).join(" → ") || "—"}</p>
            <p>Applicants: {applicants.length}</p>
          </div>
        </section>

        {/* Results (F7) */}
        <section className="bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="font-semibold text-slate-900">Post results</h2>

          {applicants.length === 0 ? (
            <p className="mt-3 text-sm text-slate-400">
              No students have applied to this drive yet. Results can be posted once applications arrive.
            </p>
          ) : (
            <>
              <div className="mt-3 flex gap-2">
                <select className={input} value={pick} onChange={(e) => setPick(e.target.value)}>
                  <option value="">Select applicant to shortlist…</option>
                  {applicants
                    .filter((s) => !shortlisted.includes(Number(s.id)))
                    .map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name} — {s.rollNo} ({s.branch}, CGPA {s.cgpa})
                      </option>
                    ))}
                </select>
                <button
                  onClick={addShortlist}
                  disabled={!pick}
                  className="shrink-0 px-3 py-2 rounded-lg text-sm font-semibold border border-slate-300 text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                >
                  Add
                </button>
              </div>

              <div className="mt-4">
                <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
                  Shortlisted ({shortlisted.length})
                </h3>
                {shortlisted.length === 0 ? (
                  <p className="mt-2 text-sm text-slate-400">No students shortlisted yet.</p>
                ) : (
                  <ul className="mt-2 space-y-1.5">
                    {shortlisted.map((studentId) => {
                      const s = byId(studentId);
                      const isSel = selected.includes(studentId);
                      return (
                        <li key={studentId} className="flex items-center justify-between gap-2 bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-sm">
                          <span className="min-w-0 truncate">
                            <span className="font-medium text-slate-800">{s?.name ?? `Student #${studentId}`}</span>
                            <span className="text-slate-500"> • {s?.rollNo ?? "—"}</span>
                          </span>
                          <span className="flex gap-1.5 shrink-0">
                            {isSel ? (
                              <>
                                <Badge color="green">Selected</Badge>
                                <button onClick={() => demote(studentId)} className="text-xs text-slate-500 hover:text-red-600">undo</button>
                              </>
                            ) : (
                              <button
                                onClick={() => promote(studentId)}
                                className="px-2 py-0.5 rounded-md text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100"
                              >
                                Mark selected
                              </button>
                            )}
                            <button onClick={() => removeShort(studentId)} className="text-xs text-slate-400 hover:text-red-600" aria-label="Remove">
                              ✕
                            </button>
                          </span>
                        </li>
                      );
                    })}
                  </ul>
                )}
              </div>

              {resultMsg && (
                <p className="mt-3 text-sm text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg px-3 py-2">
                  {resultMsg}
                </p>
              )}

              <div className="mt-4 flex justify-end">
                <button
                  onClick={postResult}
                  disabled={shortlisted.length === 0 || busy}
                  className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300"
                >
                  {busy ? "Publishing…" : "Publish result"}
                </button>
              </div>
            </>
          )}
        </section>
      </div>

      {error && (
        <p className="mt-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}
    </div>
  );
}
