import { Outlet, Link } from "react-router-dom";

// Layout for /login and /register — split screen with branding panel
export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-slate-100 flex items-stretch justify-center p-4 md:p-8">
      {/* Branding panel */}
      <div className="hidden md:flex w-1/2 max-w-xl rounded-3xl bg-gradient-to-br from-indigo-600 via-indigo-700 to-slate-900 text-white p-10 flex-col justify-between">
        <div>
          <div className="flex items-center gap-3">
            <div className="h-11 w-11 rounded-xl bg-white/15 border border-white/25 grid place-items-center text-xl font-bold">
              PN
            </div>
            <span className="text-xl font-semibold tracking-tight">Placement NIT</span>
          </div>
          <h1 className="mt-12 text-3xl font-bold leading-snug">
            All placement news.
            <br />
            One place. Zero missed deadlines.
          </h1>
          <p className="mt-4 text-indigo-100/90 leading-relaxed">
            Company announcements, eligibility, schedules, test links and results —
            posted by coordinators, seen by every eligible student, with in-app and
            email notifications.
          </p>
        </div>
        <ul className="space-y-2 text-sm text-indigo-100/90">
          <li>• Smart eligibility filtering per branch, CGPA and backlogs</li>
          <li>• Deadline reminders before last dates and interviews</li>
          <li>• Full history of old announcements, searchable anytime</li>
        </ul>
      </div>

      {/* Form panel */}
      <div className="w-full max-w-md flex flex-col justify-center">
        <Link to="/login" className="md:hidden mb-6 flex items-center gap-2">
          <div className="h-9 w-9 rounded-lg bg-indigo-600 text-white grid place-items-center text-sm font-bold">
            PN
          </div>
          <span className="font-semibold text-slate-800">Placement NIT</span>
        </Link>
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 sm:p-8">
          <Outlet />
        </div>
        <p className="mt-4 text-center text-xs text-slate-500">
          OOP Lab — Group 17 • Java 21 · Spring Boot · Oracle
        </p>
      </div>
    </div>
  );
}
