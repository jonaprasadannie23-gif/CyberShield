import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError } from "../api";
import type { Incident, IncidentStatus, IncidentType, RiskLevel } from "../types";
import { STATUS_LABELS, TYPE_LABELS } from "../types";

export function IncidentHistoryPage() {
  const [items, setItems] = useState<Incident[]>([]);
  const [keyword, setKeyword] = useState("");
  const [type, setType] = useState("");
  const [risk, setRisk] = useState("");
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    setError("");
    const params = new URLSearchParams();
    if (keyword) params.set("keyword", keyword);
    if (type) params.set("type", type);
    if (risk) params.set("risk", risk);
    if (status) params.set("status", status);
    try {
      const data = await api.get<Incident[]>(`/api/incidents?${params.toString()}`);
      setItems(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not load incidents.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  async function remove(id: string) {
    if (!confirm("Delete this incident?")) return;
    try {
      await api.delete(`/api/incidents/${id}`);
      setItems((current) => current.filter((item) => item.incidentId !== id));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Delete failed.");
    }
  }

  return (
    <>
      <h1 className="page-title">Incident history</h1>
      <p className="page-sub">Search and filter your own records only.</p>
      <div className="filters">
        <input placeholder="Search title or description" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">All types</option>
          {(Object.keys(TYPE_LABELS) as IncidentType[]).map((key) => <option key={key} value={key}>{TYPE_LABELS[key]}</option>)}
        </select>
        <select value={risk} onChange={(e) => setRisk(e.target.value)}>
          <option value="">All risk</option>
          {(["LOW", "MEDIUM", "HIGH"] as RiskLevel[]).map((key) => <option key={key} value={key}>{key}</option>)}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All status</option>
          {(Object.keys(STATUS_LABELS) as IncidentStatus[]).map((key) => <option key={key} value={key}>{STATUS_LABELS[key]}</option>)}
        </select>
        <button className="btn btn-primary" onClick={() => void load()}>Apply</button>
      </div>
      {error && <div className="banner error">{error}</div>}
      {loading ? <p className="muted">Loading…</p> : items.length === 0 ? (
        <p className="empty">No matching incidents.</p>
      ) : (
        <div className="card" style={{ overflowX: "auto" }}>
          <table className="table">
            <thead>
              <tr>
                <th>Title</th><th>Type</th><th>Risk</th><th>Status</th><th>Date</th><th></th>
              </tr>
            </thead>
            <tbody>
              {items.map((incident) => (
                <tr key={incident.incidentId}>
                  <td><Link to={`/incidents/${incident.incidentId}`}>{incident.title}</Link></td>
                  <td>{TYPE_LABELS[incident.type]}</td>
                  <td><span className={`badge ${incident.riskLevel}`}>{incident.riskLevel}</span></td>
                  <td><span className="badge status">{STATUS_LABELS[incident.status]}</span></td>
                  <td>{incident.incidentDate}</td>
                  <td>
                    <Link to={`/incidents/${incident.incidentId}/edit`}>Edit</Link>
                    {" · "}
                    <button className="btn btn-danger" onClick={() => void remove(incident.incidentId)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
