import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../api/client.js";
import { mapCompany } from "../../api/mappers.js";
import { branchOptions } from "../../data/companies.js";
import { fmtDate, isClosed, daysLeft } from "../../utils/dates.js";
import Badge from "../../components/Badge.jsx";
import PageHeader from "../../components/PageHeader.jsx";

function outcomeBadge(outcome) {
  const parts = String(outcome || "").split(",");
  if (parts.includes("SELECTED")) return { label: "Selected", color: "green" };
  if (parts.includes("SHORTLISTED")) return { label: "Shortlisted", color: "blue" };
  return { label: "Not shortlisted", color: "slate" };
}

function CompanyCard({ c }) {
  const closed = isClosed(c.lastDate);
  const left = daysLeft(c.lastDate);

  return (
    <Link
      to={`/student/company/${c.id}`}
      className="group bg-white rounded-xl border border-slate-200 p-4 hover:shadow-md hover:border-indigo-300 transition-shadow flex flex-col gap-3"
    >
      <div className="flex items-start justify-between gap-2">
        <div>
          <h3 className="font-semibold text-slate-900 group-hover:text-indigo-700">
            {c.companyName}
          </h3>
          <p className="text-sm text-slate-500">{c.role}</p>
        </div>
        <Badge color="indigo">₹{c.ctc} LPA</Badge>
      </div>

      <div className="flex flex-wrap gap-1.5">
        {c.eligible ? (
          <Badge color="green">✓ Eligible</Badge>
        ) : (
          <Badge color="red">✕ Not eligible</Badge>
        )}
        {c.applied && <Badge color="blue">Applied</Badge>}
        {c.bookmarked && <Badge color="amber">★ Bookmarked</Badge>}
        {closed && <Badge color="slate">Closed</Badge>}
        {c.resultPosted && <Badge color="blue">Result posted</Badge>}
        {!closed && left !== null && left <= 7 && (
          <Badge color="amber">{left}d left</Badge>
        )}
      </div>

      <div className="mt-auto flex items-center justify-between text-xs text-slate-500 border-t border-slate-100 pt-2.5">
        <span>{c.location}</span>
        <span>
          Last date:{" "}
          <span className={closed ? "text-slate-400 line-through" : "font-medium text-slate-700"}>
            {fmtDate(c.lastDate)}
          </span>
        </span>
      </div>
    </Link>
  );
}

// F8/F9/F10 — student dashboard: live list from GET /api/student/dashboard
export default function StudentDashboard() {
  const [items, setItems] = useState([]);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [branch, setBranch] = useState("all");
  const [eligibleOnly, setEligibleOnly] = useState(false);
  const [minCtc, setMinCtc] = useState("all");
  const [status, setStatus] = useState("all"); // all | applied | not-applied | bookmarked

  useEffect(() => {
    let alive = true;
    setLoading(true);
    api
      .get("/api/student/dashboard")
      .then((data) => {
        if (alive) {
          setItems(data.map(mapCompany));
          setError("");
        }
      })
      .catch((err) => alive && setError(err.message))
      .finally(() => alive && setLoading(false));
    api
      .get("/api/student/results")
      .then((data) => {
        if (alive) setResults(data);
      })
      .catch(() => {
        /* results section is optional */
      });
    return () => {
      alive = false;
    };
  }, []);

  const list = useMemo(() => {
    let out = items.filter((c) => !c.isArchived);

    const q = search.trim().toLowerCase();
    if (q) {
      out = out.filter(
        (c) =>
          c.companyName.toLowerCase().includes(q) ||
          c.role.toLowerCase().includes(q) ||
          c.details.toLowerCase().includes(q)
      );
    }
    if (branch !== "all") out = out.filter((c) => c.eligibility.branches.includes(branch));
    if (eligibleOnly) out = out.filter((c) => c.eligible);
    if (minCtc !== "all") out = out.filter((c) => c.ctc >= Number(minCtc));
    if (status === "applied") out = out.filter((c) => c.applied);
    if (status === "not-applied") out = out.filter((c) => !c.applied);
    if (status === "bookmarked") out = out.filter((c) => c.bookmarked);

    return [...out].sort((a, b) => new Date(a.lastDate) - new Date(b.lastDate));
  }, [items, search, branch, eligibleOnly, minCtc, status]);

  const tabs = [
    { key: "all", label: "All" },
    { key: "applied", label: "Applied" },
    { key: "not-applied", label: "Not applied" },
    { key: "bookmarked", label: "Bookmarked" },
  ];

  return (
    <div>
      <PageHeader
        title="Placement Dashboard"
        description="Companies you can see — eligibility computed from your profile."
      />

      {results.length > 0 && (
        <section className="mb-4 bg-white rounded-xl border border-slate-200 p-4">
          <h2 className="text-sm font-semibold text-slate-700">My results</h2>
          <ul className="mt-2 flex flex-col gap-2 sm:flex-row sm:flex-wrap">
            {results.map((r) => {
              const outcome = outcomeBadge(r.outcome);
              return (
                <li key={r.id}>
                  <Link
                    to={`/student/company/${r.id}`}
                    className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-sm hover:bg-slate-50"
                  >
                    <span className="font-medium text-slate-800">{r.companyName}</span>
                    <span className="text-slate-500">{r.roleTitle}</span>
                    <Badge color={outcome.color}>{outcome.label}</Badge>
                  </Link>
                </li>
              );
            })}
          </ul>
        </section>
      )}

      {/* Search + filters (F9, F10) */}
      <div className="bg-white rounded-xl border border-slate-200 p-4 space-y-3">
        <input
          type="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search company, role or keyword…"
          className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />

        <div className="flex flex-wrap items-center gap-3">
          <select
            value={branch}
            onChange={(e) => setBranch(e.target.value)}
            className="text-sm border border-slate-300 rounded-lg px-2 py-1.5 bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="all">All branches</option>
            {branchOptions.map((b) => (
              <option key={b} value={b}>
                {b}
              </option>
            ))}
          </select>

          <select
            value={minCtc}
            onChange={(e) => setMinCtc(e.target.value)}
            className="text-sm border border-slate-300 rounded-lg px-2 py-1.5 bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="all">Any CTC</option>
            <option value="6">≥ ₹6 LPA</option>
            <option value="9">≥ ₹9 LPA</option>
            <option value="20">≥ ₹20 LPA</option>
            <option value="35">≥ ₹35 LPA</option>
          </select>

          <label className="flex items-center gap-2 text-sm text-slate-700 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={eligibleOnly}
              onChange={(e) => setEligibleOnly(e.target.checked)}
              className="h-4 w-4 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
            />
            Eligible only
          </label>

          <div className="flex rounded-lg border border-slate-300 overflow-hidden ml-auto">
            {tabs.map((t) => (
              <button
                key={t.key}
                onClick={() => setStatus(t.key)}
                className={`px-2.5 py-1.5 text-xs font-medium border-r border-slate-300 last:border-r-0 ${
                  status === t.key
                    ? "bg-indigo-600 text-white"
                    : "bg-white text-slate-600 hover:bg-slate-50"
                }`}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {error && (
        <p className="mt-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      {/* Results */}
      {loading ? (
        <p className="mt-8 text-center text-sm text-slate-500">Loading drives…</p>
      ) : list.length === 0 ? (
        <div className="mt-8 text-center py-14 bg-white rounded-xl border border-dashed border-slate-300">
          <p className="text-4xl">🔍</p>
          <p className="mt-3 font-medium text-slate-700">No results found</p>
          <p className="text-sm text-slate-500">Try changing your filters or search keyword.</p>
        </div>
      ) : (
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {list.map((c) => (
            <CompanyCard key={c.id} c={c} />
          ))}
        </div>
      )}
    </div>
  );
}
