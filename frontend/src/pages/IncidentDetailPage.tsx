import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, ApiError } from "../api";
import type { Incident, IncidentStatus, SecurityTask } from "../types";
import { SOURCE_LABELS, STATUS_LABELS, TYPE_LABELS } from "../types";

// ===========================================================================
// Inline accessible SVG icons
// ===========================================================================

function ShieldIcon() {
  return (
    <svg aria-hidden="true" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
    </svg>
  );
}

function AlertTriangleIcon() {
  return (
    <svg aria-hidden="true" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  );
}

function CheckCircleIcon() {
  return (
    <svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
      <polyline points="22 4 12 14.01 9 11.01" />
    </svg>
  );
}

function CopyIcon() {
  return (
    <svg aria-hidden="true" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
    </svg>
  );
}

function ArrowLeftIcon() {
  return (
    <svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <line x1="19" y1="12" x2="5" y2="12" />
      <polyline points="12 19 5 12 12 5" />
    </svg>
  );
}

function PlusIcon() {
  return (
    <svg aria-hidden="true" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="12" y1="5" x2="12" y2="19" />
      <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
  );
}

function ChevronDownIcon({ open }: { open: boolean }) {
  return (
    <svg
      aria-hidden="true"
      width="13"
      height="13"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      style={{
        transform: open ? "rotate(180deg)" : "rotate(0deg)",
        transition: "transform 0.2s ease",
      }}
    >
      <polyline points="6 9 12 15 18 9" />
    </svg>
  );
}

// ===========================================================================
// Helper logic: recommendations & risk explanations
// ===========================================================================

export function isImmediateRecommendation(rec: string): boolean {
  const lower = rec.toLowerCase();
  return (
    lower.includes("immediately") ||
    lower.includes("never share") ||
    lower.includes("never disclose") ||
    lower.includes("do not open") ||
    lower.includes("do not click") ||
    lower.includes("do not react") ||
    lower.includes("hang up") ||
    lower.includes("block the sender") ||
    lower.includes("run a full antivirus") ||
    lower.includes("change password") ||
    lower.includes("contact your bank") ||
    lower.includes("file a report") ||
    lower.includes("report the url") ||
    lower.includes("delete the message")
  );
}

export function cleanRecommendationText(rec: string): string {
  const lower = rec.toLowerCase();
  if (lower.includes("never share immediately")) {
    return "Do not react to urgent threats or artificial time pressure; verify your account status directly through official channels.";
  }
  if (lower.includes("never share urgent")) {
    return "Do not react to urgent demands; verify the request through trusted official channels.";
  }
  if (lower.includes("never share limited time")) {
    return "Do not succumb to artificial time pressure; verify directly with the organization.";
  }
  if (lower.includes("be sceptical")) {
    return rec.replace(/be sceptical/i, "Be skeptical");
  }
  return rec;
}

export function deriveTaskTitle(recommendation: string): string {
  const r = recommendation.trim();
  const lower = r.toLowerCase();
  if (lower.includes("contact your bank")) return "Contact bank to secure accounts";
  if (lower.includes("antivirus") || lower.includes("scan")) return "Run device antivirus scan";
  if (lower.includes("change password")) return "Change exposed account passwords";
  if (lower.includes("two-factor") || lower.includes("2fa")) return "Enable 2FA on accounts";
  if (lower.includes("operating system") || lower.includes("apps up to date") || lower.includes("update"))
    return "Update OS and security software";
  if (lower.includes("block the sender")) return "Block sender and report spam";
  if (lower.includes("report the url") || lower.includes("cybercrime"))
    return "Report scam link to cybercrime helpline";
  if (lower.includes("never share your otp")) return "Review OTP and credential safety rules";
  if (lower.includes("typosquatting") || lower.includes("domain")) return "Inspect sender domain for spoofing";
  if (lower.includes("hover over")) return "Practice link destination previewing";
  if (lower.includes("official website") || lower.includes("verify requests"))
    return "Verify request via official website";
  const firstSentence = r.split(".")[0];
  if (firstSentence.length <= 48) return firstSentence;
  return firstSentence.slice(0, 45) + "...";
}

export interface IndicatorDetails {
  category: string;
  badge: string;
  badgeType: "danger" | "warning" | "info";
  whyItMatters: string;
}

export function getIndicatorContext(reason: string): IndicatorDetails {
  const r = reason.toLowerCase();
  if (r.includes("critical") || r.includes("already been disclosed")) {
    return {
      category: "Exposure",
      badge: "Active Exposure",
      badgeType: "danger",
      whyItMatters:
        "Sensitive information has reached unauthorized parties, creating an immediate window for fraudulent transactions or account takeover.",
    };
  }
  if (r.includes("already clicked")) {
    return {
      category: "Exposure",
      badge: "Link Visited",
      badgeType: "danger",
      whyItMatters:
        "Interacting with untrusted destinations can expose session tokens, transmit browser fingerprints, or trigger background downloads.",
    };
  }
  if (r.includes("attachment")) {
    return {
      category: "Delivery",
      badge: "Payload Risk",
      badgeType: "danger",
      whyItMatters:
        "Email attachments frequently conceal malware, weaponized macros, or scripts designed to execute upon opening.",
    };
  }
  if (r.includes("otp") || r.includes("one-time password")) {
    return {
      category: "Auth Security",
      badge: "OTP Intercept",
      badgeType: "danger",
      whyItMatters:
        "One-time passwords are the primary authentication checkpoint; sharing them grants attackers instant account takeover.",
    };
  }
  if (r.includes("financial") || r.includes("bank account") || r.includes("credit card") || r.includes("cvv")) {
    return {
      category: "Financial",
      badge: "Asset Risk",
      badgeType: "danger",
      whyItMatters:
        "Banking details and card numbers are monetized immediately through illicit transfers, card fraud, or black market bundling.",
    };
  }
  if (r.includes("identity") || r.includes("aadhaar") || r.includes("social security")) {
    return {
      category: "Identity",
      badge: "PII Target",
      badgeType: "danger",
      whyItMatters:
        "National identifiers enable long-term identity theft, SIM swaps, and fraudulent line-of-credit registrations.",
    };
  }
  if (r.includes("urgency") || r.includes("threat") || r.includes("suspended") || r.includes("blocked") || r.includes("immediately")) {
    return {
      category: "Social Engineering",
      badge: "Coercive Pressure",
      badgeType: "warning",
      whyItMatters:
        "Artificial deadlines and threats of penalties are psychological tactics designed to cause panic and rush victims past scrutiny.",
    };
  }
  if (r.includes("impersonate") || r.includes("brand") || r.includes("authority")) {
    return {
      category: "Social Engineering",
      badge: "Brand Spoofing",
      badgeType: "warning",
      whyItMatters:
        "Impersonating recognized institutions exploits trust and authority to make deceptive requests seem routine and legitimate.",
    };
  }
  if (r.includes("social-engineering") || r.includes("action prompt") || r.includes("reward") || r.includes("prize")) {
    return {
      category: "Social Engineering",
      badge: "Lure Prompt",
      badgeType: "warning",
      whyItMatters:
        "Enticing reward claims and unexpected notifications entice targets to click suspicious links or enter personal information.",
    };
  }
  if (r.includes("suspicious url") || r.includes("link")) {
    return {
      category: "Delivery",
      badge: "Web Infrastructure",
      badgeType: "warning",
      whyItMatters:
        "Unverified URLs often direct to fake login interfaces hosted on spoofed domains that closely resemble real services.",
    };
  }
  if (r.includes("phone call") || r.includes("vishing")) {
    return {
      category: "Delivery",
      badge: "Voice Vector",
      badgeType: "warning",
      whyItMatters:
        "Live voice calls allow experienced scammers to counter hesitation in real time and socially engineer verification codes.",
    };
  }
  if (r.includes("sms") || r.includes("smishing")) {
    return {
      category: "Delivery",
      badge: "SMS Vector",
      badgeType: "warning",
      whyItMatters:
        "SMS communications lack origin certificates and frequently employ masked alphanumeric senders to simulate authentic alerts.",
    };
  }
  if (r.includes("prevalent credential-theft") || r.includes("phishing")) {
    return {
      category: "Threat Pattern",
      badge: "Credential Harvesting",
      badgeType: "info",
      whyItMatters:
        "Phishing attacks disguise malicious communication as authentic to manipulate individuals into willingly revealing sensitive credentials.",
    };
  }
  return {
    category: "Finding",
    badge: "Indicator",
    badgeType: "info",
    whyItMatters:
      "This pattern was identified by automated heuristic evaluation as a characteristic of potential cyber risk.",
  };
}

// ===========================================================================
// Main Component: IncidentDetailPage
// ===========================================================================

const INITIAL_REC_COUNT = 3;

export function IncidentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [incident, setIncident] = useState<Incident | null>(null);
  const [tasks, setTasks] = useState<SecurityTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [addingTaskRec, setAddingTaskRec] = useState<string | null>(null);
  const [copiedUrl, setCopiedUrl] = useState(false);
  const [copiedId, setCopiedId] = useState(false);

  // Expand / collapse for risk indicator rows
  const [expandedIndicators, setExpandedIndicators] = useState<Record<number, boolean>>({});

  // Show more / show less for recommendations
  const [showAllImmediate, setShowAllImmediate] = useState(false);
  const [showAllPreventive, setShowAllPreventive] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError("");

    Promise.all([
      api.get<Incident>(`/api/incidents/${id}`),
      api.get<SecurityTask[]>("/api/tasks").catch(() => [] as SecurityTask[]),
    ])
      .then(([incData, tasksData]) => {
        setIncident(incData);
        setTasks(tasksData);
      })
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "Could not load incident details.");
      })
      .finally(() => setLoading(false));
  }, [id]);

  async function handleStatusUpdate(newStatus: IncidentStatus) {
    if (!incident || incident.status === newStatus) return;
    setUpdatingStatus(true);
    setSuccessMsg("");
    setError("");
    try {
      const updated = await api.patch<Incident>(`/api/incidents/${incident.incidentId}/status`, {
        status: newStatus,
      });
      setIncident(updated);
      setSuccessMsg(`Status updated to "${STATUS_LABELS[newStatus]}".`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to update incident status.");
    } finally {
      setUpdatingStatus(false);
    }
  }

  async function handleDeleteIncident() {
    if (!incident) return;
    setDeleting(true);
    setError("");
    try {
      await api.delete(`/api/incidents/${incident.incidentId}`);
      navigate("/incidents");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to delete incident.");
      setDeleting(false);
      setShowDeleteModal(false);
    }
  }

  async function handleCreateTask(rawRec: string) {
    if (!incident) return;
    const cleanRec = cleanRecommendationText(rawRec);
    const title = deriveTaskTitle(cleanRec);
    setAddingTaskRec(rawRec);
    setError("");
    setSuccessMsg("");

    try {
      const created = await api.post<SecurityTask>("/api/tasks", {
        title,
        description: cleanRec,
        relatedIncidentId: incident.incidentId,
      });
      setTasks((prev) => [...prev, created]);
      setSuccessMsg(`Safety task added: "${title}". Track it on the Tasks page.`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not convert recommendation to task.");
    } finally {
      setAddingTaskRec(null);
    }
  }

  function handleCopyUrl() {
    if (!incident?.suspiciousUrl) return;
    navigator.clipboard.writeText(incident.suspiciousUrl).then(() => {
      setCopiedUrl(true);
      setTimeout(() => setCopiedUrl(false), 2000);
    });
  }

  function handleCopyId() {
    if (!incident?.incidentId) return;
    navigator.clipboard.writeText(incident.incidentId).then(() => {
      setCopiedId(true);
      setTimeout(() => setCopiedId(false), 2000);
    });
  }

  function toggleIndicator(index: number) {
    setExpandedIndicators((prev) => ({
      ...prev,
      [index]: !prev[index],
    }));
  }

  function toggleAllIndicators(expand: boolean, count: number) {
    const next: Record<number, boolean> = {};
    for (let i = 0; i < count; i++) {
      next[i] = expand;
    }
    setExpandedIndicators(next);
  }

  if (loading) {
    return (
      <div className="incident-detail-loading" role="status" aria-live="polite">
        <div className="loading-spinner" />
        <p className="muted">Loading incident record…</p>
      </div>
    );
  }

  if (error && !incident) {
    return (
      <div className="incident-detail-container">
        <div className="banner error" role="alert">{error}</div>
        <Link to="/incidents" className="btn btn-secondary" style={{ marginTop: 12 }}>
          <ArrowLeftIcon /> Back to history
        </Link>
      </div>
    );
  }

  if (!incident) {
    return (
      <div className="incident-detail-container">
        <p className="empty">Incident record not found.</p>
        <Link to="/incidents" className="btn btn-secondary">
          <ArrowLeftIcon /> Back to history
        </Link>
      </div>
    );
  }

  // Deduplicate and sanitize recommendations
  const rawRecommendations = incident.recommendations || [];
  const cleanRecs = Array.from(new Set(rawRecommendations.map(cleanRecommendationText)));
  const immediateActions = cleanRecs.filter(isImmediateRecommendation);
  const preventiveActions = cleanRecs.filter((r) => !isImmediateRecommendation(r));

  const visibleImmediate = showAllImmediate
    ? immediateActions
    : immediateActions.slice(0, INITIAL_REC_COUNT);

  const visiblePreventive = showAllPreventive
    ? preventiveActions
    : preventiveActions.slice(0, INITIAL_REC_COUNT);

  // Determine which recommendations already have tasks
  function findExistingTask(recText: string): SecurityTask | undefined {
    const derivedTitle = deriveTaskTitle(recText).toLowerCase();
    const cleanLower = recText.toLowerCase();
    return tasks.find(
      (t) =>
        t.relatedIncidentId === incident?.incidentId &&
        (t.description?.toLowerCase() === cleanLower ||
          t.title?.toLowerCase() === derivedTitle)
    );
  }

  // Type-specific flags check
  const hasTypeSpecificFlags =
    incident.type === "PHISHING" ||
    incident.type === "SUSPICIOUS_LINK" ||
    incident.type === "OTP_SCAM";

  const reasons = incident.assessmentReasons || [];
  const allExpanded = reasons.length > 0 && reasons.every((_, i) => expandedIndicators[i]);

  return (
    <div className="incident-detail-container">
      {/* ── Breadcrumb ── */}
      <nav className="detail-breadcrumb" aria-label="Breadcrumb navigation">
        <Link to="/incidents" className="back-link">
          <ArrowLeftIcon /> Back to incident history
        </Link>
      </nav>

      {/* ── Feedback Banners ── */}
      {error && (
        <div className="banner error" role="alert">
          {error}
        </div>
      )}
      {successMsg && (
        <div className="banner success" role="status" aria-live="polite">
          <CheckCircleIcon /> {successMsg}
        </div>
      )}

      {/* ── Compact Header ── */}
      <header className="incident-header-compact card">
        <div className="header-top-row">
          <div className="header-title-block">
            <div className="header-badges">
              <span
                className={`risk-badge ${incident.riskLevel}`}
                aria-label={`Risk level: ${incident.riskLevel}`}
              >
                <span className={`indicator-dot dot-${incident.riskLevel.toLowerCase()}`} />
                {incident.riskLevel} RISK
              </span>
              <span
                className={`status-badge status-${incident.status.toLowerCase()}`}
                aria-label={`Status: ${STATUS_LABELS[incident.status]}`}
              >
                {STATUS_LABELS[incident.status]}
              </span>
            </div>
            <h1 className="incident-title-compact">{incident.title}</h1>
          </div>

          <div className="header-actions-compact">
            <div className="status-dropdown-wrap">
              <label htmlFor="status-select" className="status-label-compact">
                Status:
              </label>
              <select
                id="status-select"
                value={incident.status}
                disabled={updatingStatus}
                onChange={(e) => void handleStatusUpdate(e.target.value as IncidentStatus)}
                className="compact-status-select"
                aria-label="Change incident status"
              >
                <option value="REPORTED">Reported</option>
                <option value="UNDER_REVIEW">Under Review</option>
                <option value="RESOLVED">Resolved</option>
              </select>
              {updatingStatus && <span className="saving-indicator-dot" title="Saving...">…</span>}
            </div>

            <Link
              to={`/incidents/${incident.incidentId}/edit`}
              className="btn btn-secondary btn-compact"
            >
              Edit
            </Link>
            <button
              type="button"
              className="btn btn-danger btn-compact"
              onClick={() => setShowDeleteModal(true)}
            >
              Delete
            </button>
          </div>
        </div>

        {/* Compact Metadata Strip */}
        <div className="meta-strip-compact">
          <span className="meta-item">
            <span className="meta-lbl">Type:</span> {TYPE_LABELS[incident.type] || incident.type}
          </span>
          <span className="meta-sep">·</span>
          <span className="meta-item">
            <span className="meta-lbl">Source:</span> {SOURCE_LABELS[incident.source] || incident.source}
          </span>
          <span className="meta-sep">·</span>
          <span className="meta-item">
            <span className="meta-lbl">Occurred:</span> {incident.incidentDate}
          </span>
          {incident.reportedAt && (
            <>
              <span className="meta-sep">·</span>
              <span className="meta-item">
                <span className="meta-lbl">Reported:</span> {incident.reportedAt.replace("T", " ").slice(0, 16)}
              </span>
            </>
          )}
          <span className="meta-sep">·</span>
          <span className="meta-item id-item">
            <span className="meta-lbl">ID:</span>
            <code className="id-code-compact">{incident.incidentId}</code>
            <button
              type="button"
              className="btn-copy-tiny"
              onClick={handleCopyId}
              title="Copy Incident ID"
              aria-label="Copy Incident ID"
            >
              <CopyIcon />
              {copiedId && <span className="copy-toast">Copied</span>}
            </button>
          </span>
        </div>
      </header>

      {/* ── Balanced Two-Column Overview on Desktop ── */}
      <div className="overview-two-col">
        {/* Left Column: Description, Recorded URL, Type Indicators */}
        <div className="overview-col-left">
          <section className="card compact-card" aria-labelledby="desc-heading">
            <h2 id="desc-heading" className="card-section-title-compact">
              <ShieldIcon /> Incident Overview
            </h2>

            <div className="incident-description-compact">
              <p>{incident.description}</p>
            </div>

            {/* Suspicious URL (Plain text only — never opened or fetched) */}
            <div className="url-box-compact">
              <div className="url-box-header">
                <span className="url-box-title">
                  <AlertTriangleIcon /> Suspicious Web Link
                </span>
                <span className="url-box-safe-tag">Plain text only — not opened</span>
              </div>
              {incident.suspiciousUrl ? (
                <div className="url-box-content">
                  <code
                    className="url-code-compact"
                    tabIndex={0}
                    aria-label="Suspicious URL in plain text"
                  >
                    {incident.suspiciousUrl}
                  </code>
                  <button
                    type="button"
                    className="btn btn-secondary btn-tiny"
                    onClick={handleCopyUrl}
                    title="Copy plain text URL for reporting"
                    aria-label="Copy plain text URL"
                  >
                    <CopyIcon /> {copiedUrl ? "Copied" : "Copy"}
                  </button>
                </div>
              ) : (
                <p className="muted empty-note-compact">No URL recorded with this incident.</p>
              )}
            </div>

            {/* Type-Specific Indicators (shown only when relevant) */}
            {hasTypeSpecificFlags && (
              <div className="type-flags-compact">
                <h3 className="type-flags-title">Type Indicators ({TYPE_LABELS[incident.type]})</h3>
                <div className="type-pills-row">
                  {incident.type === "PHISHING" && (
                    <>
                      <span className={`pill-compact ${incident.claimedToBeLegitOrg ? "active" : "inactive"}`}>
                        Impersonation: {incident.claimedToBeLegitOrg ? "Yes" : "No"}
                      </span>
                      <span className={`pill-compact ${incident.containedAttachment ? "danger" : "inactive"}`}>
                        Attachment: {incident.containedAttachment ? "Yes" : "None"}
                      </span>
                    </>
                  )}
                  {incident.type === "SUSPICIOUS_LINK" && (
                    <>
                      <span className={`pill-compact ${incident.linkWasClicked ? "danger" : "safe"}`}>
                        Link clicked: {incident.linkWasClicked ? "Yes" : "No"}
                      </span>
                      <span className={`pill-compact ${incident.requestedCredentials ? "danger" : "inactive"}`}>
                        Credentials requested: {incident.requestedCredentials ? "Yes" : "No"}
                      </span>
                    </>
                  )}
                  {incident.type === "OTP_SCAM" && (
                    <>
                      <span className={`pill-compact ${incident.otpWasShared ? "danger" : "safe"}`}>
                        OTP shared: {incident.otpWasShared ? "Yes (Critical)" : "No"}
                      </span>
                      <span className={`pill-compact ${incident.financialInfoShared ? "danger" : "safe"}`}>
                        Financial details: {incident.financialInfoShared ? "Disclosed" : "Protected"}
                      </span>
                    </>
                  )}
                </div>
                {incident.typeSpecificDetails && (
                  <p className="type-guidance-compact">{incident.typeSpecificDetails}</p>
                )}
              </div>
            )}
          </section>
        </div>

        {/* Right Column: Risk Assessment with Compact Rows & Expand/Collapse */}
        <div className="overview-col-right">
          <section className="card compact-card" aria-labelledby="risk-heading">
            <div className="risk-header-compact">
              <h2 id="risk-heading" className="card-section-title-compact">
                <AlertTriangleIcon /> Risk Assessment
              </h2>
              <div className="risk-header-actions">
                <span className={`risk-pill-sm ${incident.riskLevel}`}>
                  {incident.riskLevel} Level
                </span>
                {reasons.length > 1 && (
                  <button
                    type="button"
                    className="btn-text-subtle"
                    onClick={() => toggleAllIndicators(!allExpanded, reasons.length)}
                    aria-label={allExpanded ? "Collapse all indicators" : "Expand all indicators"}
                  >
                    {allExpanded ? "Collapse all" : "Expand all"}
                  </button>
                )}
              </div>
            </div>

            <p className="risk-summary-text">
              {reasons.length} risk factor{reasons.length === 1 ? "" : "s"} identified from reported incident attributes.
            </p>

            {/* Compact Indicator Rows */}
            <div className="indicator-rows-list">
              {reasons.length > 0 ? (
                reasons.map((reason, index) => {
                  const ctx = getIndicatorContext(reason);
                  const isExpanded = Boolean(expandedIndicators[index]);

                  return (
                    <div
                      key={index}
                      className={`indicator-row-compact ${isExpanded ? "open" : ""}`}
                    >
                      <button
                        type="button"
                        className="indicator-row-summary"
                        onClick={() => toggleIndicator(index)}
                        aria-expanded={isExpanded}
                        aria-controls={`ind-detail-${index}`}
                      >
                        <div className="indicator-summary-left">
                          <span className={`ind-badge-tiny ${ctx.badgeType}`}>
                            {ctx.badge}
                          </span>
                          <span className="ind-category-tiny">{ctx.category}</span>
                          <span className="ind-statement-compact">{reason}</span>
                        </div>
                        <span className="ind-expand-icon">
                          <ChevronDownIcon open={isExpanded} />
                        </span>
                      </button>

                      {isExpanded && (
                        <div id={`ind-detail-${index}`} className="indicator-detail-compact">
                          <span className="why-tag">Why this matters:</span>
                          <span className="why-desc">{ctx.whyItMatters}</span>
                        </div>
                      )}
                    </div>
                  );
                })
              ) : (
                <p className="muted empty-note-compact">No specific risk indicators recorded.</p>
              )}
            </div>

            {/* Compact Methodology Disclaimer */}
            <div className="disclaimer-compact">
              <span className="disclaimer-symbol">🛡️</span>
              <span className="disclaimer-msg">
                <strong>Rule-based assessment:</strong> Automated heuristic evaluation of reported parameters; does not constitute a malware or file scan.
              </span>
            </div>
          </section>
        </div>
      </div>

      {/* ── Recommendations Section Below Overview ── */}
      <section className="card compact-card recommendations-section" aria-labelledby="recs-heading">
        <div className="recs-header-row">
          <div>
            <h2 id="recs-heading" className="card-section-title-compact">
              <CheckCircleIcon /> Recommended Safety Actions
            </h2>
            <p className="recs-sub-compact">
              Practical guidance tailored to this incident. Convert recommendations to tracked safety tasks.
            </p>
          </div>
          <Link to="/tasks" className="btn btn-secondary btn-tiny">
            Open Tasks Dashboard →
          </Link>
        </div>

        {/* Side-by-Side Immediate & Preventive Columns */}
        <div className="recs-two-col">
          {/* Immediate Actions */}
          <div className="recs-sub-col">
            <div className="recs-sub-header immediate">
              <span className="recs-tag immediate">Immediate Actions ({immediateActions.length})</span>
              <span className="recs-tag-sub">Urgent containment steps</span>
            </div>

            {immediateActions.length > 0 ? (
              <div className="recs-compact-list">
                {visibleImmediate.map((rec) => {
                  const task = findExistingTask(rec);
                  const isAdding = addingTaskRec === rec;

                  return (
                    <div key={rec} className="rec-item-compact immediate">
                      <div className="rec-item-text">
                        <span className="rec-bullet-tiny immediate">!</span>
                        <span>{rec}</span>
                      </div>
                      <div className="rec-item-action">
                        {task ? (
                          <Link
                            to="/tasks"
                            className={`task-pill-tiny ${task.completed ? "done" : "pending"}`}
                            title="View task on Safety Tasks page"
                          >
                            <CheckCircleIcon /> {task.completed ? "Completed" : "Tracked in Tasks"}
                          </Link>
                        ) : (
                          <button
                            type="button"
                            className="btn-add-task-tiny"
                            onClick={() => void handleCreateTask(rec)}
                            disabled={isAdding}
                            aria-label={`Add as safety task: ${rec}`}
                          >
                            <PlusIcon /> {isAdding ? "Adding…" : "Add as task"}
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}

                {immediateActions.length > INITIAL_REC_COUNT && (
                  <button
                    type="button"
                    className="btn-show-more"
                    onClick={() => setShowAllImmediate(!showAllImmediate)}
                    aria-expanded={showAllImmediate}
                  >
                    {showAllImmediate
                      ? "Show fewer actions"
                      : `+ Show ${immediateActions.length - INITIAL_REC_COUNT} more immediate actions`}
                    <ChevronDownIcon open={showAllImmediate} />
                  </button>
                )}
              </div>
            ) : (
              <p className="muted empty-note-compact">No immediate containment actions required.</p>
            )}
          </div>

          {/* Preventive Actions */}
          <div className="recs-sub-col">
            <div className="recs-sub-header preventive">
              <span className="recs-tag preventive">Preventive Actions ({preventiveActions.length})</span>
              <span className="recs-tag-sub">Long-term cyber safety habits</span>
            </div>

            {preventiveActions.length > 0 ? (
              <div className="recs-compact-list">
                {visiblePreventive.map((rec) => {
                  const task = findExistingTask(rec);
                  const isAdding = addingTaskRec === rec;

                  return (
                    <div key={rec} className="rec-item-compact preventive">
                      <div className="rec-item-text">
                        <span className="rec-bullet-tiny preventive">✓</span>
                        <span>{rec}</span>
                      </div>
                      <div className="rec-item-action">
                        {task ? (
                          <Link
                            to="/tasks"
                            className={`task-pill-tiny ${task.completed ? "done" : "pending"}`}
                            title="View task on Safety Tasks page"
                          >
                            <CheckCircleIcon /> {task.completed ? "Completed" : "Tracked in Tasks"}
                          </Link>
                        ) : (
                          <button
                            type="button"
                            className="btn-add-task-tiny"
                            onClick={() => void handleCreateTask(rec)}
                            disabled={isAdding}
                            aria-label={`Add as safety task: ${rec}`}
                          >
                            <PlusIcon /> {isAdding ? "Adding…" : "Add as task"}
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}

                {preventiveActions.length > INITIAL_REC_COUNT && (
                  <button
                    type="button"
                    className="btn-show-more"
                    onClick={() => setShowAllPreventive(!showAllPreventive)}
                    aria-expanded={showAllPreventive}
                  >
                    {showAllPreventive
                      ? "Show fewer actions"
                      : `+ Show ${preventiveActions.length - INITIAL_REC_COUNT} more preventive actions`}
                    <ChevronDownIcon open={showAllPreventive} />
                  </button>
                )}
              </div>
            ) : (
              <p className="muted empty-note-compact">No additional preventive actions needed.</p>
            )}
          </div>
        </div>
      </section>

      {/* ── Accessible Delete Confirmation Modal ── */}
      {showDeleteModal && (
        <div
          className="modal-backdrop"
          role="presentation"
          onClick={() => !deleting && setShowDeleteModal(false)}
        >
          <div
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-dialog-title"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <h2 id="delete-dialog-title" className="modal-title">
                Confirm Incident Deletion
              </h2>
            </div>
            <div className="modal-body">
              <p>
                Are you sure you want to permanently delete incident{" "}
                <strong>"{incident.title}"</strong> (ID: <code>{incident.incidentId}</code>)?
              </p>
              <p className="muted" style={{ fontSize: "0.85rem", marginTop: 8 }}>
                This record will be permanently deleted from your personal incident history and
                cannot be recovered.
              </p>
            </div>
            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-secondary btn-compact"
                disabled={deleting}
                onClick={() => setShowDeleteModal(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-danger btn-compact"
                disabled={deleting}
                onClick={() => void handleDeleteIncident()}
              >
                {deleting ? "Deleting…" : "Confirm Delete"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
