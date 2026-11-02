import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../../api/client.js";
import { mapCompany } from "../../api/mappers.js";
import { fmtDate, isClosed } from "../../utils/dates.js";
import Badge from "../../components/Badge.jsx";
import PageHeader from "../../components/PageHeader.jsx";
import ConfirmModal from "../../components/ConfirmModal.jsx";

// F4/F5/U2 — coordinator manages announcements: add, edit, delete, archive
export default function CoordinatorList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [toDelete, setToDelete] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = () => {
    setLoading(true);
    api
      .get("/api/announcements?archived=true")
      .then((data) => {
        setItems(data.map(mapCompany));
        setError("");
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const toggleArchive = async (c) => {
    setBusy(true);
    try {
      const updated = await api.patch(`/api/announcements/${c.id}/archive`, {
        archived: !c.isArchived,
      });
      setItems((list) =>
        list.map((x) => (x.id === c.id ? mapCompany(updated) : x))
      );
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const remove = async () => {
    if (!toDelete) return;
    setBusy(true);
    try {
      await api.del(`/api/announcements/${toDelete.id}`);
      setItems((list) => list.filter((x) => x.id !== toDelete.id));
      setToDelete(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Manage Announcements"
        description="Add, edit, archive or remove company drives. Eligible students get notified on every change."
        action={
          <Link
            to="/coordinator/new"
            className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700"
          >
            + New Announcement
          </Link>
        }
      />

      {error && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {error}
        </p>
      )}

      <div className="bg-white rounded-xl border border-slate-200 overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-3 font-semibold">Company</th>
              <th className="px-4 py-3 font-semibold">Role</th>
              <th className="px-4 py-3 font-semibold">CTC</th>
              <th className="px-4 py-3 font-semibold">Last date</th>
              <th className="px-4 py-3 font-semibold">Status</th>
              <th className="px-4 py-3 font-semibold text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-slate-500">
                  Loading announcements…
                </td>
              </tr>
            ) : items.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-10 text-center text-slate-500">
                  No announcements yet — publish your first drive.
                </td>
              </tr>
            ) : (
              items.map((c) => {
                const closed = isClosed(c.lastDate);
                return (
                  <tr key={c.id} className="border-b border-slate-100 last:border-0 hover:bg-slate-50/60">
                    <td className="px-4 py-3">
                      <Link to={`/coordinator/company/${c.id}`} className="font-medium text-slate-900 hover:text-indigo-700">
                        {c.companyName}
                      </Link>
                    </td>
                    <td className="px-4 py-3 text-slate-600">{c.role}</td>
                    <td className="px-4 py-3 text-slate-600">₹{c.ctc} LPA</td>
                    <td className="px-4 py-3 text-slate-600">{fmtDate(c.lastDate)}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-1.5 flex-wrap">
                        {c.isArchived ? (
                          <Badge color="slate">Archived</Badge>
                        ) : closed ? (
                          <Badge color="amber">Closed</Badge>
                        ) : (
                          <Badge color="green">Active</Badge>
                        )}
                        {c.result?.posted && <Badge color="blue">Result posted</Badge>}
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1.5">
                        <Link
                          to={`/coordinator/edit/${c.id}`}
                          className="px-2.5 py-1 rounded-md text-xs font-medium border border-slate-300 text-slate-700 hover:bg-white"
                        >
                          Edit
                        </Link>
                        <Link
                          to={`/coordinator/company/${c.id}`}
                          className="px-2.5 py-1 rounded-md text-xs font-medium border border-slate-300 text-slate-700 hover:bg-white"
                        >
                          Schedule
                        </Link>
                        <button
                          onClick={() => toggleArchive(c)}
                          disabled={busy}
                          className="px-2.5 py-1 rounded-md text-xs font-medium border border-slate-300 text-slate-700 hover:bg-white disabled:opacity-50"
                        >
                          {c.isArchived ? "Restore" : "Archive"}
                        </button>
                        <button
                          onClick={() => setToDelete(c)}
                          className="px-2.5 py-1 rounded-md text-xs font-medium border border-red-200 text-red-600 hover:bg-red-50"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      <ConfirmModal
        open={!!toDelete}
        title="Delete announcement?"
        message={`"${toDelete?.companyName}" will be permanently removed. Students will no longer see it.`}
        confirmLabel="Delete"
        danger
        onConfirm={remove}
        onCancel={() => setToDelete(null)}
      />
    </div>
  );
}
