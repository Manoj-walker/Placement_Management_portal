import { useEffect, useState } from "react";
import { useSession } from "../../context/SessionContext.jsx";
import { api } from "../../api/client.js";
import { mapUser } from "../../api/mappers.js";
import Badge from "../../components/Badge.jsx";
import PageHeader from "../../components/PageHeader.jsx";
import ConfirmModal from "../../components/ConfirmModal.jsx";

// F13/U8 — admin creates, blocks, deletes users and changes roles.
// Admins may also inspect a user's stored (bcrypt) password hash.
export default function AdminUsers() {
  const { user: me } = useSession();
  const [items, setItems] = useState([]);
  const [search, setSearch] = useState("");
  const [confirm, setConfirm] = useState(null); // {kind, user}
  const [adding, setAdding] = useState(false);
  const [hashView, setHashView] = useState({ id: null, hash: "", loading: false, error: "" });
  const [newUser, setNewUser] = useState({
    name: "",
    email: "",
    password: "Welcome@123",
    role: "student",
    rollNo: "",
    branch: "CSE",
    batch: "2026",
    cgpa: "",
    backlogs: 0,
    department: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  const load = () => {
    setLoading(true);
    api
      .get("/api/admin/users")
      .then((data) => {
        setItems(data.map(mapUser));
        setError("");
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const list = items.filter((u) => {
    const q = search.trim().toLowerCase();
    if (!q) return true;
    return (
      u.name.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      (u.rollNo || "").toLowerCase().includes(q)
    );
  });

  const isSelf = (u) => String(u.id) === String(me.id) || u.email === me.email;

  const runConfirmed = async () => {
    if (!confirm) return;
    const { kind, user: target } = confirm;
    setBusy(true);
    try {
      if (kind === "block") {
        const updated = await api.patch(`/api/admin/users/${target.id}/status`, {
          status: target.status === "blocked" ? "ACTIVE" : "BLOCKED",
        });
        setItems((rows) => rows.map((u) => (u.id === target.id ? mapUser(updated) : u)));
      } else if (kind === "delete") {
        await api.del(`/api/admin/users/${target.id}`);
        setItems((rows) => rows.filter((u) => u.id !== target.id));
      } else if (kind === "role") {
        const updated = await api.patch(`/api/admin/users/${target.id}/role`, {
          role: target.newRole.toUpperCase(),
        });
        setItems((rows) => rows.map((u) => (u.id === target.id ? mapUser(updated) : u)));
      }
      setError("");
      setConfirm(null);
    } catch (err) {
      setError(err.message);
      setConfirm(null);
    } finally {
      setBusy(false);
    }
  };

  const showHash = async (u) => {
    if (hashView.id === u.id) {
      setHashView({ id: null, hash: "", loading: false, error: "" });
      return;
    }
    setHashView({ id: u.id, hash: "", loading: true, error: "" });
    try {
      const data = await api.get(`/api/admin/users/${u.id}/password-hash`);
      setHashView({ id: u.id, hash: data.passwordHash, loading: false, error: "" });
    } catch (err) {
      setHashView({ id: u.id, hash: "", loading: false, error: err.message });
    }
  };

  const changeRole = (u, newRole) => {
    // U8 exception: admin cannot remove their own admin role
    if (isSelf(u) && newRole !== "admin") {
      setError("You cannot change your own admin role.");
      return;
    }
    setError("");
    if (newRole === u.role) return;
    setConfirm({ kind: "role", user: { ...u, newRole } });
  };

  const addUser = async () => {
    if (!newUser.name.trim() || !newUser.email.includes("@")) {
      setError("Name and a valid email are required.");
      return;
    }
    if (newUser.password.length < 8) {
      setError("Password must be at least 8 characters.");
      return;
    }
    setBusy(true);
    try {
      const created = await api.post("/api/admin/users", {
        name: newUser.name.trim(),
        email: newUser.email.trim(),
        password: newUser.password,
        role: newUser.role.toUpperCase(),
        rollNo: newUser.role === "student" ? newUser.rollNo : null,
        branch: newUser.role === "student" ? newUser.branch : null,
        batch: newUser.role === "student" ? newUser.batch : null,
        cgpa: newUser.role === "student" && newUser.cgpa !== "" ? Number(newUser.cgpa) : null,
        backlogs: newUser.role === "student" ? Number(newUser.backlogs) || 0 : null,
        department: newUser.role === "coordinator" ? newUser.department : null,
      });
      const full = await api.get(`/api/admin/users/${created.id}`);
      setItems((rows) => [mapUser(full), ...rows]);
      setNewUser({
        name: "",
        email: "",
        password: "Welcome@123",
        role: "student",
        rollNo: "",
        branch: "CSE",
        batch: "2026",
        cgpa: "",
        backlogs: 0,
        department: "",
      });
      setAdding(false);
      setError("");
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const CONFIRM_COPY = {
    block: (u) => ({
      title: u.status === "blocked" ? "Unblock account?" : "Block account?",
      message:
        u.status === "blocked"
          ? `${u.name} will be able to sign in again.`
          : `${u.name} will not be able to sign in until unblocked.`,
      label: u.status === "blocked" ? "Unblock" : "Block",
      danger: u.status !== "blocked",
    }),
    delete: (u) => ({
      title: "Delete account?",
      message: `${u.name} (${u.email}) will be permanently removed.`,
      label: "Delete",
      danger: true,
    }),
    role: (u) => ({
      title: "Change role?",
      message: `${u.name} will become ${u.newRole}.`,
      label: "Change role",
      danger: false,
    }),
  };

  const input = "rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500";
  const label = "block text-xs font-semibold text-slate-600 uppercase tracking-wide";

  return (
    <div>
      <PageHeader
        title="Manage Users"
        description="Create, block, delete accounts and change roles."
        action={
          <button
            onClick={() => { setError(""); setAdding((v) => !v); }}
            className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700"
          >
            {adding ? "Cancel" : "+ Create user"}
          </button>
        }
      />

      {adding && (
        <div className="mb-4 bg-white rounded-xl border border-indigo-200 p-4 space-y-3">
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <label htmlFor="nu-name" className={label}>Name</label>
              <input id="nu-name" className={`${input} mt-1 w-full`} value={newUser.name} onChange={(e) => setNewUser({ ...newUser, name: e.target.value })} />
            </div>
            <div>
              <label htmlFor="nu-email" className={label}>Email</label>
              <input id="nu-email" type="email" className={`${input} mt-1 w-full`} value={newUser.email} onChange={(e) => setNewUser({ ...newUser, email: e.target.value })} />
            </div>
            <div>
              <label htmlFor="nu-password" className={label}>Temp password</label>
              <input id="nu-password" className={`${input} mt-1 w-full`} value={newUser.password} onChange={(e) => setNewUser({ ...newUser, password: e.target.value })} />
            </div>
            <div>
              <label htmlFor="nu-role" className={label}>Role</label>
              <select id="nu-role" className={`${input} mt-1 w-full`} value={newUser.role} onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}>
                <option value="student">Student</option>
                <option value="coordinator">Coordinator</option>
                <option value="admin">Admin</option>
              </select>
            </div>
          </div>

          {newUser.role === "student" && (
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
              <div>
                <label htmlFor="nu-roll" className={label}>Roll no</label>
                <input id="nu-roll" className={`${input} mt-1 w-full`} value={newUser.rollNo} onChange={(e) => setNewUser({ ...newUser, rollNo: e.target.value })} placeholder="21CS1001" />
              </div>
              <div>
                <label htmlFor="nu-branch" className={label}>Branch</label>
                <select id="nu-branch" className={`${input} mt-1 w-full`} value={newUser.branch} onChange={(e) => setNewUser({ ...newUser, branch: e.target.value })}>
                  {["CSE", "IT", "ECE", "EEE", "MECH", "CIVIL"].map((b) => (
                    <option key={b} value={b}>{b}</option>
                  ))}
                </select>
              </div>
              <div>
                <label htmlFor="nu-batch" className={label}>Batch</label>
                <select id="nu-batch" className={`${input} mt-1 w-full`} value={newUser.batch} onChange={(e) => setNewUser({ ...newUser, batch: e.target.value })}>
                  {["2026", "2027", "2028"].map((b) => (
                    <option key={b} value={b}>{b}</option>
                  ))}
                </select>
              </div>
              <div>
                <label htmlFor="nu-cgpa" className={label}>CGPA</label>
                <input id="nu-cgpa" type="number" step="0.01" min="0" max="10" className={`${input} mt-1 w-full`} value={newUser.cgpa} onChange={(e) => setNewUser({ ...newUser, cgpa: e.target.value })} />
              </div>
              <div>
                <label htmlFor="nu-back" className={label}>Backlogs</label>
                <input id="nu-back" type="number" min="0" className={`${input} mt-1 w-full`} value={newUser.backlogs} onChange={(e) => setNewUser({ ...newUser, backlogs: e.target.value })} />
              </div>
            </div>
          )}

          {newUser.role === "coordinator" && (
            <div>
              <label htmlFor="nu-dept" className={label}>Department</label>
              <input id="nu-dept" className={`${input} mt-1 w-full sm:max-w-sm`} value={newUser.department} onChange={(e) => setNewUser({ ...newUser, department: e.target.value })} placeholder="Training & Placement" />
            </div>
          )}

          <div className="flex justify-end">
            <button onClick={addUser} disabled={busy} className="px-4 py-2 rounded-lg text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 disabled:bg-slate-400">
              {busy ? "Creating…" : "Create account"}
            </button>
          </div>
        </div>
      )}

      {error && (
        <p className="mb-4 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">{error}</p>
      )}

      <div className="mb-4">
        <input
          type="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email or roll number…"
          className="w-full sm:max-w-md rounded-lg border border-slate-300 px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
        />
      </div>

      <div className="bg-white rounded-xl border border-slate-200 overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
              <th className="px-4 py-3 font-semibold">Name</th>
              <th className="px-4 py-3 font-semibold">Email</th>
              <th className="px-4 py-3 font-semibold">Roll no</th>
              <th className="px-4 py-3 font-semibold">Role</th>
              <th className="px-4 py-3 font-semibold">Status</th>
              <th className="px-4 py-3 font-semibold text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="6" className="px-4 py-10 text-center text-slate-400">Loading users…</td>
              </tr>
            ) : list.length === 0 ? (
              <tr>
                <td colSpan="6" className="px-4 py-10 text-center text-slate-400">
                  No users found
                </td>
              </tr>
            ) : (
              list.map((u) => {
                const self = isSelf(u);
                return (
                  <>
                  <tr key={u.id} className="border-b border-slate-100 last:border-0 hover:bg-slate-50/60">
                    <td className="px-4 py-3">
                      <span className="font-medium text-slate-900">{u.name}</span>
                      {self && <span className="ml-1.5 text-[10px] text-indigo-600 font-semibold">YOU</span>}
                    </td>
                    <td className="px-4 py-3 text-slate-600">{u.email}</td>
                    <td className="px-4 py-3 text-slate-600">{u.rollNo || "—"}</td>
                    <td className="px-4 py-3">
                      <select
                        aria-label={`Role for ${u.name}`}
                        value={u.role}
                        disabled={self}
                        onChange={(e) => changeRole(u, e.target.value)}
                        className="rounded-md border border-slate-300 px-2 py-1 text-xs bg-white disabled:bg-slate-100 disabled:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                      >
                        <option value="student">student</option>
                        <option value="coordinator">coordinator</option>
                        <option value="admin">admin</option>
                      </select>
                    </td>
                    <td className="px-4 py-3">
                      {u.status === "blocked" ? <Badge color="red">Blocked</Badge> : <Badge color="green">Active</Badge>}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1.5">
                        <button
                          onClick={() => showHash(u)}
                          className="px-2.5 py-1 rounded-md text-xs font-medium border border-indigo-200 text-indigo-700 hover:bg-indigo-50"
                        >
                          {hashView.id === u.id ? "Hide hash" : "Hash"}
                        </button>
                        <button
                          onClick={() => setConfirm({ kind: "block", user: u })}
                          disabled={self || busy}
                          title={self ? "You cannot block your own account" : undefined}
                          className="px-2.5 py-1 rounded-md text-xs font-medium border border-slate-300 text-slate-700 hover:bg-white disabled:opacity-40 disabled:cursor-not-allowed"
                        >
                          {u.status === "blocked" ? "Unblock" : "Block"}
                        </button>
                        <button
                          onClick={() => setConfirm({ kind: "delete", user: u })}
                          disabled={self || busy}
                          title={self ? "You cannot delete your own account" : undefined}
                          className="px-2.5 py-1 rounded-md text-xs font-medium border border-red-200 text-red-600 hover:bg-red-50 disabled:opacity-40 disabled:cursor-not-allowed"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                  {hashView.id === u.id && (
                    <tr>
                      <td colSpan="6" className="px-4 py-3 bg-slate-50 border-b border-slate-100">
                        <p className="text-[10px] font-semibold text-slate-500 uppercase tracking-wide mb-1">
                          Stored password hash (bcrypt) — this is not the real password
                        </p>
                        {hashView.loading ? (
                          <span className="text-xs text-slate-500">Loading…</span>
                        ) : hashView.error ? (
                          <span className="text-xs text-red-600">{hashView.error}</span>
                        ) : (
                          <div className="flex items-start gap-2">
                            <code className="break-all text-[11px] font-mono text-slate-700 bg-white border border-slate-200 rounded px-2 py-1">
                              {hashView.hash}
                            </code>
                            <button
                              onClick={() => navigator.clipboard?.writeText(hashView.hash)}
                              className="shrink-0 text-xs font-medium text-indigo-600 hover:underline"
                            >
                              Copy
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  )}
                  </>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      <ConfirmModal
        open={!!confirm}
        title={confirm ? CONFIRM_COPY[confirm.kind](confirm.user).title : ""}
        message={confirm ? CONFIRM_COPY[confirm.kind](confirm.user).message : ""}
        confirmLabel={confirm ? CONFIRM_COPY[confirm.kind](confirm.user).label : "Confirm"}
        danger={confirm ? CONFIRM_COPY[confirm.kind](confirm.user).danger : false}
        onConfirm={runConfirmed}
        onCancel={() => setConfirm(null)}
      />
    </div>
  );
}
