import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="min-h-screen grid place-items-center bg-slate-50 p-6">
      <div className="text-center">
        <p className="text-6xl font-black text-indigo-600">404</p>
        <h1 className="mt-3 text-xl font-bold text-slate-900">Page not found</h1>
        <p className="mt-1 text-sm text-slate-500">
          The page you're looking for doesn't exist or you don't have access.
        </p>
        <Link
          to="/"
          className="mt-5 inline-block px-4 py-2 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700"
        >
          Go home
        </Link>
      </div>
    </div>
  );
}
