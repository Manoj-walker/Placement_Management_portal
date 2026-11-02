import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../api/client.js";
import { mapCompany } from "../../api/mappers.js";
import { fmtDate, isClosed, daysLeft } from "../../utils/dates.js";
import Badge from "../../components/Badge.jsx";
import PageHeader from "../../components/PageHeader.jsx";

// U4 — My Deadlines: nearest last date first, with applied/bookmarked status
export default function Deadlines() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;
    api
      .get("/api/student/dashboard")
      .then((data) => {
        if (alive) setItems(data.map(mapCompany));
      })
      .catch((err) => alive && setError(err.message))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, []);

  const list = items
    .filter((c) => !c.isArchived)
    .map((c) => ({ ...c, left: daysLeft(c.lastDate) }))
    .sort((a, b) => new Date(a.lastDate) - new Date(b.lastDate));

  const open = list.filter((c) => !isClosed(c.lastDate));
  const closed = list.filter((c) => isClosed(c.lastDate));

  const Row = ({ c }) => {
    const shut = isClosed(c.lastDate);
    return (
      <li className="flex flex-wrap items-center justify-between gap-3 bg-white border border-slate-200 rounded-xl px-4 py-3">
        <div className="min-w-0">
          <Link to={`/student/company/${c.id}`} className="font-semibold text-slate-900 hover:text-indigo-700">
            {c.companyName}
          </Link>
          <p className="text-sm text-slate-500 truncate">{c.role}</p>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          {c.applied ? <Badge color="blue">Applied</Badge> : shut ? null : <Badge color="slate">Not applied</Badge>}
          {c.bookmarked && <Badge color="amber">★ Bookmarked</Badge>}
          {shut ? (
            <Badge color="slate">Closed</Badge>
          ) : (
            <>
              {c.left <= 7 && <Badge color="amber">{c.left}d left</Badge>}
              <span className="text-sm text-slate-600 whitespace-nowrap">{fmtDate(c.lastDate)}</span>
            </>
          )}
        </div>
      </li>
    );
  };

  return (
    <div>
      <PageHeader title="My Deadlines" description="Nearest last date first — never miss a drive." />

      {error && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      {loading ? (
        <p className="text-center py-10 text-sm text-slate-500">Loading deadlines…</p>
      ) : open.length === 0 ? (
        <div className="text-center py-14 bg-white rounded-xl border border-dashed border-slate-300">
          <p className="text-4xl">🎉</p>
          <p className="mt-3 font-medium text-slate-700">No open deadlines right now</p>
        </div>
      ) : (
        <ul className="space-y-2">{open.map((c) => <Row key={c.id} c={c} />)}</ul>
      )}

      {closed.length > 0 && (
        <>
          <h2 className="mt-8 mb-3 text-sm font-semibold text-slate-500 uppercase tracking-wide">
            Closed
          </h2>
          <ul className="space-y-2 opacity-70">{closed.map((c) => <Row key={c.id} c={c} />)}</ul>
        </>
      )}
    </div>
  );
}
