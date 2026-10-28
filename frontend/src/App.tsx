import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./AuthContext";
import { Layout } from "./components/Layout";
import { DashboardPage } from "./pages/DashboardPage";
import { AssessmentPage } from "./pages/AssessmentPage";
import { IncidentDetailPage } from "./pages/IncidentDetailPage";
import { IncidentFormPage } from "./pages/IncidentFormPage";
import { IncidentHistoryPage } from "./pages/IncidentHistoryPage";
import { LoginPage } from "./pages/LoginPage";
import { PasswordPage } from "./pages/PasswordPage";
import { RecommendationsPage } from "./pages/RecommendationsPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ReportsPage } from "./pages/ReportsPage";
import { TasksPage } from "./pages/TasksPage";
import type { ReactNode } from "react";

function Guard({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="muted" style={{ padding: 32 }}>Loading session…</p>;
  if (!user) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

function Guest({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="muted" style={{ padding: 32 }}>Loading session…</p>;
  if (user) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Guest><LoginPage /></Guest>} />
      <Route path="/register" element={<Guest><RegisterPage /></Guest>} />
      <Route element={<Guard><Layout /></Guard>}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/assessment" element={<AssessmentPage />} />
        <Route path="/assessment/:id" element={<AssessmentPage />} />
        <Route path="/incidents/new" element={<IncidentFormPage />} />
        <Route path="/incidents/:id/edit" element={<IncidentFormPage />} />
        <Route path="/incidents/:id" element={<IncidentDetailPage />} />
        <Route path="/incidents" element={<IncidentHistoryPage />} />
        <Route path="/recommendations" element={<RecommendationsPage />} />
        <Route path="/tasks" element={<TasksPage />} />
        <Route path="/password" element={<PasswordPage />} />
        <Route path="/reports" element={<ReportsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
