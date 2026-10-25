import { FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, ApiError } from "../api";
import type { Incident, IncidentSource, IncidentStatus, IncidentType } from "../types";
import { SOURCE_LABELS, STATUS_LABELS, TYPE_LABELS } from "../types";

const empty = {
  title: "",
  type: "PHISHING" as IncidentType,
  description: "",
  source: "EMAIL" as IncidentSource,
  incidentDate: new Date().toISOString().slice(0, 10),
  suspiciousUrl: "",
  claimedToBeLegitOrg: false,
  containedAttachment: false,
  linkWasClicked: false,
  requestedCredentials: false,
  otpWasShared: false,
  financialInfoShared: false,
  status: "REPORTED" as IncidentStatus,
};

export function IncidentFormPage() {
  const { id } = useParams();
  const editing = Boolean(id);
  const navigate = useNavigate();
  const [form, setForm] = useState(empty);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!id) return;
    api.get<Incident>(`/api/incidents/${id}`)
      .then((incident) => setForm({
        title: incident.title,
        type: incident.type,
        description: incident.description,
        source: incident.source,
        incidentDate: incident.incidentDate,
        suspiciousUrl: incident.suspiciousUrl ?? "",
        claimedToBeLegitOrg: Boolean(incident.claimedToBeLegitOrg),
        containedAttachment: Boolean(incident.containedAttachment),
        linkWasClicked: Boolean(incident.linkWasClicked),
        requestedCredentials: Boolean(incident.requestedCredentials),
        otpWasShared: Boolean(incident.otpWasShared),
        financialInfoShared: Boolean(incident.financialInfoShared),
        status: incident.status,
      }))
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load incident."));
  }, [id]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess("");
    setBusy(true);
    try {
      if (editing && id) {
        const updated = await api.put<Incident>(`/api/incidents/${id}`, form);
        setSuccess(`Updated. Risk is now ${updated.riskLevel}.`);
        navigate(`/incidents/${id}`);
      } else {
        const created = await api.post<Incident>("/api/incidents", form);
        setSuccess(`Reported. Assessed as ${created.riskLevel}.`);
        navigate(`/incidents/${created.incidentId}`);
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not save incident.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <h1 className="page-title">{editing ? "Edit incident" : "Report a cyber incident"}</h1>
      <p className="page-sub">URLs are stored as text only and are never opened.</p>
      {error && <div className="banner error">{error}</div>}
      {success && <div className="banner success">{success}</div>}
      <form className="card" onSubmit={onSubmit}>
        <label>Title *</label>
        <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
        <div className="row">
          <div>
            <label>Type *</label>
            <select value={form.type} disabled={editing} onChange={(e) => setForm({ ...form, type: e.target.value as IncidentType })}>
              {Object.entries(TYPE_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </div>
          <div>
            <label>Source *</label>
            <select value={form.source} onChange={(e) => setForm({ ...form, source: e.target.value as IncidentSource })}>
              {Object.entries(SOURCE_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </div>
        </div>
        <label>Description *</label>
        <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        <div className="row">
          <div>
            <label>Date *</label>
            <input type="date" value={form.incidentDate} onChange={(e) => setForm({ ...form, incidentDate: e.target.value })} />
          </div>
          <div>
            <label>Suspicious URL (optional)</label>
            <input value={form.suspiciousUrl} onChange={(e) => setForm({ ...form, suspiciousUrl: e.target.value })} />
          </div>
        </div>
        {form.type === "PHISHING" && (
          <>
            <label className="checkbox"><input type="checkbox" checked={form.claimedToBeLegitOrg} onChange={(e) => setForm({ ...form, claimedToBeLegitOrg: e.target.checked })} /> Sender claimed to be a trusted organisation</label>
            <label className="checkbox"><input type="checkbox" checked={form.containedAttachment} onChange={(e) => setForm({ ...form, containedAttachment: e.target.checked })} /> Message contained an attachment</label>
          </>
        )}
        {form.type === "SUSPICIOUS_LINK" && (
          <>
            <label className="checkbox"><input type="checkbox" checked={form.linkWasClicked} onChange={(e) => setForm({ ...form, linkWasClicked: e.target.checked })} /> I already clicked the link</label>
            <label className="checkbox"><input type="checkbox" checked={form.requestedCredentials} onChange={(e) => setForm({ ...form, requestedCredentials: e.target.checked })} /> The page asked for credentials</label>
          </>
        )}
        {form.type === "OTP_SCAM" && (
          <>
            <label className="checkbox"><input type="checkbox" checked={form.otpWasShared} onChange={(e) => setForm({ ...form, otpWasShared: e.target.checked })} /> OTP was already shared</label>
            <label className="checkbox"><input type="checkbox" checked={form.financialInfoShared} onChange={(e) => setForm({ ...form, financialInfoShared: e.target.checked })} /> Financial details were disclosed</label>
          </>
        )}
        {editing && (
          <>
            <label>Status</label>
            <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value as IncidentStatus })}>
              {Object.entries(STATUS_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </>
        )}
        <div className="row" style={{ marginTop: 16 }}>
          <button className="btn btn-primary" disabled={busy}>{busy ? "Saving…" : editing ? "Save changes" : "Submit report"}</button>
        </div>
      </form>
    </>
  );
}
