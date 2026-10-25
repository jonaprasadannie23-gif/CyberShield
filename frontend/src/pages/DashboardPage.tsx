import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError } from "../api";
import type { DashboardData } from "../types";
import { TYPE_LABELS } from "../types";

export function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get<DashboardData>("/api/dashboard")
      .then(setData)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load dashboard."))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="muted">Loading dashboard…</p>;
  if (error) return <div className="banner error">{error}</div>;
  if (!data) return null;

  return (
    <>
      <h1 className="page-title">Overview</h1>
      <p className="page-sub">Live counts from your stored incidents and tasks. This tool does not provide real-time protection.</p>
      <div className="grid stats">
        <div className="card"><div className="stat-value">{data.totalIncidents}</div><div className="stat-label">Incidents</div></div>
        <div className="card"><div className="stat-value">{data.highRiskCount}</div><div className="stat-label">High risk</div></div>
        <div className="card"><div className="stat-value">{data.unresolvedCount}</div><div className="stat-label">Unresolved</div></div>
        <div className="card"><div className="stat-value">{data.pendingTasks}</div><div className="stat-label">Pending tasks</div></div>
      </div>
      <div style={{ margin: "18px 0" }}>
        <div className="muted">Task completion</div>
        <div className="progress"><span style={{ width: `${data.taskCompletionPercent}%` }} /></div>
      </div>
      <div className="grid two-col" style={{ marginTop: 16 }}>
        <div className="card">
          <h3>Recent incidents</h3>
          {data.recentIncidents.length === 0 && <p className="empty">No incidents yet. <Link to="/incidents/new">Report one</Link>.</p>}
          {data.recentIncidents.map((incident) => (
            <p key={incident.incidentId}>
              <Link to={`/incidents/${incident.incidentId}`}>{incident.title}</Link>
              <span className={`badge ${incident.riskLevel}`} style={{ marginLeft: 8 }}>{incident.riskLevel}</span>
              <span className="muted"> · {TYPE_LABELS[incident.type]}</span>
            </p>
          ))}
        </div>
        <div className="card">
          <h3>Pending tasks</h3>
          {data.pendingTaskList.length === 0 && <p className="empty">No pending tasks.</p>}
          {data.pendingTaskList.slice(0, 6).map((task) => (
            <p key={task.taskId}>{task.title}</p>
          ))}
          <Link to="/tasks">Manage tasks</Link>
        </div>
      </div>
    </>
  );
}
