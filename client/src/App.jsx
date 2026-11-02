import { Routes, Route, Navigate } from "react-router-dom";
import { useSession, ROLE_HOME } from "./context/SessionContext.jsx";

import AuthLayout from "./layouts/AuthLayout.jsx";
import AppLayout from "./layouts/AppLayout.jsx";
import ProtectedRoute from "./routes/ProtectedRoute.jsx";

import Login from "./pages/auth/Login.jsx";
import Register from "./pages/auth/Register.jsx";

import StudentDashboard from "./pages/student/StudentDashboard.jsx";
import CompanyDetails from "./pages/student/CompanyDetails.jsx";
import Deadlines from "./pages/student/Deadlines.jsx";
import Notifications from "./pages/student/Notifications.jsx";
import Profile from "./pages/student/Profile.jsx";

import CoordinatorList from "./pages/coordinator/CoordinatorList.jsx";
import AnnouncementForm from "./pages/coordinator/AnnouncementForm.jsx";
import CoordinatorCompany from "./pages/coordinator/CoordinatorCompany.jsx";

import AdminDashboard from "./pages/admin/AdminDashboard.jsx";
import AdminUsers from "./pages/admin/AdminUsers.jsx";

import NotFound from "./pages/shared/NotFound.jsx";

function HomeRedirect() {
  const { user } = useSession();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={ROLE_HOME[user.role]} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />

      <Route element={<AuthLayout />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Route>

      <Route
        element={
          <ProtectedRoute role="student">
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/student" element={<StudentDashboard />} />
        <Route path="/student/company/:id" element={<CompanyDetails />} />
        <Route path="/student/deadlines" element={<Deadlines />} />
        <Route path="/student/notifications" element={<Notifications />} />
        <Route path="/student/profile" element={<Profile />} />
      </Route>

      <Route
        element={
          <ProtectedRoute role="coordinator">
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/coordinator" element={<CoordinatorList />} />
        <Route path="/coordinator/new" element={<AnnouncementForm />} />
        <Route path="/coordinator/edit/:id" element={<AnnouncementForm />} />
        <Route path="/coordinator/company/:id" element={<CoordinatorCompany />} />
      </Route>

      <Route
        element={
          <ProtectedRoute role="admin">
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/users" element={<AdminUsers />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
