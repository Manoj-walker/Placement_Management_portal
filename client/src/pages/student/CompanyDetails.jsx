import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api } from "../../api/client.js";
import { mapCompany } from "../../api/mappers.js";
import { fmtDate, isClosed, daysLeft } from "../../utils/dates.js";
import Badge from "../../components/Badge.jsx";

// U3 — full company details with eligibility, rounds and apply/bookmark actions
export default function CompanyDetails() {
  const { id } = useParams();
  const [c, setC] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let alive = true;
    setLoading(true);
    api
      .get(`/api/student/announcements/${id}`)
      .then((data) => {
        if (alive) {
          setC(mapCompany(data));
          setError("");
        }
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
        <Link to="/student" className="text-sm text-indigo-600 hover:underline">
          Back to dashboard
        </Link>
      </div>
    );
  }

  const closed = isClosed(c.lastDate);
  const left = daysLeft(c.lastDate);

  const apply = async () => {
    if (closed) return;
    setBusy(true);
    try {
      const data = await api.post(`/api/student/announcements/${c.id}/apply`);
      setC(mapCompany(data));
      setMsg("Application submitted ✓");
      window.dispatchEvent(new Event("pn:notifications-changed"));
    } catch (err) {
      setMsg(err.message);
    } finally {
      setBusy(false);
    }
  };

  const bookmark = async () => {
    setBusy(true);
    try {
      const data = c.bookmarked
        ? await api.del(`/api/student/announcements/${c.id}/bookmark`)
        : await api.post(`/api/student/announcements/${c.id}/bookmark`);
      setC((prev) => ({
        ...prev,
        bookmarked: !c.bookmarked,
        applied: data?.applied ?? prev.applied,
      }));
      setMsg(c.bookmarked ? "Bookmark removed" : "Bookmarked ✓");
    } catch (err) {
      setMsg(err.message);
    } finally {
      setBusy(false);
    }
  };

  const downloadJd = async () => {
    try {
      const response = await api.download(`/api/announcements/${c.id}/jd`);
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = c.jdFileName || "job-description";
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      setMsg(err.message);
    }
  };

  return (
    <div>
      <Link to="/student" className="text-sm text-slate-500 hover:text-slate-700">
        ← Back to dashboard
      </Link>

      {/* Header */}
      <div className="mt-3 bg-white rounded-xl border border-slate-200 p-5">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="text-xl font-bold text-slate-900">{c.companyName}</h1>
            <p className="text-slate-500">{c.role} • {c.location}</p>
          </div>
          <div className="text-right">
            <div className="text-lg font-bold text-indigo-700">₹{c.ctc} LPA</div>
            <div className="text-xs text-slate-500">Cost to company</div>
          </div>
        </div>

        <div className="mt-3 flex flex-wrap gap-1.5">
          {c.eligible ? <Badge color="green">✓ Eligible</Badge> : <Badge color="red">✕ Not eligible</Badge>}
          {c.applied && <Badge color="blue">Applied</Badge>}
          {closed && <Badge color="slate">Closed</Badge>}
          {!closed && left !== null && left <= 7 && <Badge color="amber">{left}d left</Badge>}
        </div>

        {closed && (
          <p className="mt-3 text-sm bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-slate-600">
            The last date has passed — this drive is closed. Applying is not allowed.
          </p>
        )}
        {msg && (
          <p className="mt-3 text-sm bg-indigo-50 border border-indigo-200 rounded-lg px-3 py-2 text-indigo-700">
            {msg}
          </p>
        )}

        <div className="mt-4 flex flex-wrap gap-2">
          <button
            onClick={apply}
            disabled={busy || closed || c.applied || !c.eligible}
            className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 disabled:cursor-not-allowed"
          >
            {c.applied ? "Applied ✓" : "Apply for this drive"}
          </button>
          <button
            onClick={bookmark}
            disabled={busy || c.applied}
            className="px-4 py-2 rounded-lg text-sm font-semibold border border-slate-300 text-slate-700 hover:bg-slate-50 disabled:opacity-50"
          >
            {c.bookmarked ? "★ Bookmarked" : "☆ Bookmark"}
          </button>
          {c.jdFileName && (
            <button
              onClick={downloadJd}
              className="px-4 py-2 rounded-lg text-sm font-semibold border border-slate-300 text-slate-700 hover:bg-slate-50"
            >
              📄 JD: {c.jdFileName}
            </button>
          )}
        </div>
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-3">
        {/* Description */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 p-5">
          <h2 className="font-semibold text-slate-900">About the role</h2>
          <p className="mt-2 text-sm leading-relaxed text-slate-600">{c.details || "—"}</p>

          <h2 className="mt-5 font-semibold text-slate-900">Recruitment rounds</h2>
          {c.rounds.length === 0 ? (
            <p className="mt-2 text-sm text-slate-400">Rounds will be announced soon.</p>
          ) : (
            <ol className="mt-2 space-y-2">
              {c.rounds.map((r, i) => (
                <li key={i} className="flex items-center justify-between text-sm bg-slate-50 border border-slate-200 rounded-lg px-3 py-2">
                  <span className="font-medium text-slate-700">
                    {i + 1}. {r.name}
                  </span>
                  <span className="text-slate-500">{fmtDate(r.date)}</span>
                </li>
              ))}
            </ol>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-4">
          <div className="bg-white rounded-xl border border-slate-200 p-5">
            <h2 className="font-semibold text-slate-900">Key dates</h2>
            <dl className="mt-2 space-y-2 text-sm">
              <div className="flex justify-between"><dt className="text-slate-500">Last date</dt><dd className="font-medium text-slate-800">{fmtDate(c.lastDate)}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Test date</dt><dd className="font-medium text-slate-800">{fmtDate(c.testDate)}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Interview</dt><dd className="font-medium text-slate-800">{fmtDate(c.interviewDate)}</dd></div>
              <div className="flex justify-between gap-2"><dt className="text-slate-500">Venue</dt><dd className="font-medium text-slate-800 text-right">{c.venue || "—"}</dd></div>
            </dl>
          </div>

          <div className="bg-white rounded-xl border border-slate-200 p-5">
            <h2 className="font-semibold text-slate-900">Eligibility</h2>
            <dl className="mt-2 space-y-2 text-sm">
              <div className="flex justify-between gap-2"><dt className="text-slate-500">Branches</dt><dd className="font-medium text-slate-800 text-right">{c.eligibility.branches.join(", ") || "All"}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Min CGPA</dt><dd className="font-medium text-slate-800">{c.eligibility.minCgpa}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Max backlogs</dt><dd className="font-medium text-slate-800">{c.eligibility.maxBacklogs}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Batch</dt><dd className="font-medium text-slate-800">{c.eligibility.batch || "Any"}</dd></div>
            </dl>
          </div>
        </div>
      </div>
    </div>
  );
}
