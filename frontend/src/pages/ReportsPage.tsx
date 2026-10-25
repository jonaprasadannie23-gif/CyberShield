import { useEffect, useState } from "react";
import { api, ApiError } from "../api";
import type { ReportData } from "../types";
import { STATUS_LABELS, TYPE_LABELS } from "../types";

function Bars({ data }: { data: Record<string, number> }) {
  const max = Math.max(1, ...Object.values(data));
  return (
    <>
      {Object.entries(data).map(([key, value]) => (
        <div className="bar-row" key={key}>
          <span style={{ width: 140 }}>{key}</span>
          <div className="bar-track"><div className="bar-fill" style={{ width: `${(value / max) * 100}%` }} /></div>
          <span>{value}</span>
        </div>
      ))}
    </>
  );
}

export function ReportsPage() {
  const [data, setData] = useState<ReportData | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get<ReportData>("/api/reports")
      .then(setData)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load reports."));
  }, []);

  if (error) return <div className="banner error">{error}</div>;
  if (!data) return <p className="muted">Loading reports…</p>;

  const byType = Object.fromEntries(Object.entries(data.byType).map(([k, v]) => [TYPE_LABELS[k as keyof typeof TYPE_LABELS] ?? k, v]));
  const byStatus = Object.fromEntries(Object.entries(data.byStatus).map(([k, v]) => [STATUS_LABELS[k as keyof typeof STATUS_LABELS] ?? k, v]));

  return (
    <>
      <h1 className="page-title">Reports</h1>
      <p className="page-sub">All figures are computed from your persisted records.</p>
      <div className="grid stats">
        <div className="card"><div className="stat-value">{data.totalIncidents}</div><div className="stat-label">Incidents</div></div>
        <div className="card"><div className="stat-value">{data.resolvedCount}</div><div className="stat-label">Resolved</div></div>
        <div className="card"><div className="stat-value">{data.unresolvedCount}</div><div className="stat-label">Unresolved</div></div>
        <div className="card"><div className="stat-value">{Math.round(data.taskCompletionPercent)}%</div><div className="stat-label">Tasks complete</div></div>
      </div>
      <div className="grid two-col" style={{ marginTop: 16 }}>
        <div className="card"><h3>By risk</h3><Bars data={data.byRisk} /></div>
        <div className="card"><h3>By type</h3><Bars data={byType} /></div>
      </div>
      <div className="card" style={{ marginTop: 16 }}><h3>By status</h3><Bars data={byStatus} /></div>
    </>
  );
}
