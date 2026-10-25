import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError } from "../api";
import type { RecommendationsData, SecurityTask } from "../types";

// ===========================================================================
// Inline accessible SVG icons
// ===========================================================================

function ShieldIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
    </svg>
  );
}

function ZapIcon() {
  return (
    <svg aria-hidden="true" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg aria-hidden="true" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
    </svg>
  );
}

function GlobeIcon() {
  return (
    <svg aria-hidden="true" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <line x1="2" y1="12" x2="22" y2="12" />
      <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
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

function PlusIcon() {
  return (
    <svg aria-hidden="true" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="12" y1="5" x2="12" y2="19" />
      <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
  );
}

function AlertTriangleIcon() {
  return (
    <svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  );
}

function InfoIcon() {
  return (
    <svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="16" x2="12" y2="12" />
      <line x1="12" y1="8" x2="12.01" y2="8" />
    </svg>
  );
}

// ===========================================================================
// Classification, Normalization & Action Titles
// ===========================================================================

export type RecommendationCategory = "IMMEDIATE" | "ACCOUNT_SECURITY" | "SAFE_BROWSING";

export interface ProcessedRecommendation {
  id: string;
  rawText: string;
  cleanText: string;
  title: string;
  category: RecommendationCategory;
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
  return rec.trim();
}

export function categorizeRecommendation(rec: string): RecommendationCategory {
  const lower = rec.toLowerCase();

  // 1. Immediate Actions: urgent crisis response and containment
  if (
    lower.includes("immediately") ||
    lower.includes("contact your bank") ||
    lower.includes("change password") ||
    lower.includes("antivirus") ||
    lower.includes("malware scan") ||
    lower.includes("delete the message") ||
    lower.includes("file a report") ||
    lower.includes("hang up and call back") ||
    lower.includes("block the sender")
  ) {
    return "IMMEDIATE";
  }

  // 2. Safe Browsing: links, URLs, attachments, domains, typosquatting, browsing hygiene
  if (
    lower.includes("suspicious url") ||
    lower.includes("destination url") ||
    lower.includes("do not open, copy, or visit") ||
    lower.includes("report the url") ||
    lower.includes("typosquatting") ||
    lower.includes("email domain") ||
    lower.includes("hover over") ||
    lower.includes("unsolicited message") ||
    lower.includes("operating system and apps up to date") ||
    lower.includes("visit")
  ) {
    return "SAFE_BROWSING";
  }

  // 3. Account Security: 2FA, OTP, PINs, credentials, banking auth, identity numbers
  return "ACCOUNT_SECURITY";
}

export function deriveActionTitle(rec: string): string {
  const lower = rec.toLowerCase();
  if (lower.includes("contact your bank")) return "Contact Bank to Secure Accounts";
  if (lower.includes("change password")) return "Change Compromised Passwords";
  if (lower.includes("antivirus") || lower.includes("scan")) return "Run Full Antivirus Scan";
  if (lower.includes("delete the message") || lower.includes("attachment")) return "Delete Suspicious Attachment";
  if (lower.includes("file a report") || lower.includes("cybercrime")) return "File Report with Cybercrime Helpline";
  if (lower.includes("hang up")) return "Verify Caller via Official Website";
  if (lower.includes("block the sender")) return "Block Sender & Report Spam";
  if (lower.includes("do not open, copy, or visit")) return "Do Not Open or Visit URL";
  if (lower.includes("report the url")) return "Report Suspicious URL to Authority";
  if (lower.includes("hover over")) return "Preview Link Destination URLs";
  if (lower.includes("typosquatting") || lower.includes("email domain")) return "Inspect Sender Email Domain";
  if (lower.includes("unsolicited message") || lower.includes("skeptical")) return "Scrutinize Unsolicited Requests";
  if (lower.includes("operating system") || lower.includes("apps up to date")) return "Update Device OS & Apps";
  if (lower.includes("two-factor") || lower.includes("2fa")) return "Enable Two-Factor Authentication (2FA)";
  if (lower.includes("otp")) return "Never Disclose One-Time Passwords";
  if (lower.includes("passwords, pins")) return "Protect Passwords & PINs";
  if (lower.includes("banking credentials") || lower.includes("cvv")) return "Secure Banking & Card Credentials";
  if (lower.includes("government-issued") || lower.includes("identity numbers")) return "Protect National Identification Data";
  if (lower.includes("do not reuse passwords")) return "Use Unique Passwords Across Accounts";
  if (lower.includes("artificial time pressure") || lower.includes("urgent threats")) return "Resist Urgent Account Threats";
  if (lower.includes("verify the identity") || lower.includes("official channels")) return "Verify Sender via Official Channels";
  if (lower.includes("contacting")) return "Contact Organization Directly";

  const firstSentence = rec.split(".")[0];
  if (firstSentence.length <= 45) return firstSentence;
  return firstSentence.slice(0, 42) + "…";
}

// ===========================================================================
// Main Recommendations Page Component
// ===========================================================================

export function RecommendationsPage() {
  const [items, setItems] = useState<string[]>([]);
  const [disclaimer, setDisclaimer] = useState<string[]>([]);
  const [tasks, setTasks] = useState<SecurityTask[]>([]);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);
  const [addingRecId, setAddingRecId] = useState<string | null>(null);
  const [activeFilter, setActiveFilter] = useState<"ALL" | RecommendationCategory>("ALL");

  useEffect(() => {
    setLoading(true);
    setError("");

    Promise.all([
      api.get<RecommendationsData>("/api/recommendations"),
      api.get<SecurityTask[]>("/api/tasks").catch(() => [] as SecurityTask[]),
    ])
      .then(([recData, tasksData]) => {
        setItems(recData.recommendations || []);
        setDisclaimer(recData.disclaimer || []);
        setTasks(tasksData || []);
      })
      .catch((err) => {
        setError(err instanceof ApiError ? err.message : "Could not load safety recommendations.");
      })
      .finally(() => setLoading(false));
  }, []);

  // Process and deduplicate recommendations
  const allPreparedRecs = useMemo<ProcessedRecommendation[]>(() => {
    const seen = new Set<string>();
    const list: ProcessedRecommendation[] = [];

    for (const raw of items) {
      if (!raw || !raw.trim()) continue;
      const clean = cleanRecommendationText(raw);
      const title = deriveActionTitle(clean);
      const category = categorizeRecommendation(clean);

      const key = `${category}:${title.toLowerCase()}`;
      if (!seen.has(key) && !seen.has(clean.toLowerCase())) {
        seen.add(key);
        seen.add(clean.toLowerCase());
        list.push({
          id: key,
          rawText: raw,
          cleanText: clean,
          title,
          category,
        });
      }
    }

    return list;
  }, [items]);

  // Split into categories
  const immediateRecs = useMemo(
    () => allPreparedRecs.filter((r) => r.category === "IMMEDIATE"),
    [allPreparedRecs]
  );
  const accountSecurityRecs = useMemo(
    () => allPreparedRecs.filter((r) => r.category === "ACCOUNT_SECURITY"),
    [allPreparedRecs]
  );
  const safeBrowsingRecs = useMemo(
    () => allPreparedRecs.filter((r) => r.category === "SAFE_BROWSING"),
    [allPreparedRecs]
  );

  // Helper to determine if a recommendation is already tracked in Safety Tasks
  function findMatchingTask(rec: ProcessedRecommendation): SecurityTask | undefined {
    const normTitle = rec.title.trim().toLowerCase();
    const normClean = rec.cleanText.trim().toLowerCase();

    return tasks.find((t) => {
      const tTitle = t.title ? t.title.trim().toLowerCase() : "";
      const tDesc = t.description ? t.description.trim().toLowerCase() : "";
      return (
        tTitle === normTitle ||
        (tDesc && tDesc === normClean) ||
        (tTitle && normTitle.includes(tTitle)) ||
        (tDesc && normClean.includes(tDesc))
      );
    });
  }

  // Handle adding recommendation as a safety task
  async function handleAddTask(rec: ProcessedRecommendation) {
    setAddingRecId(rec.id);
    setError("");
    setSuccess("");

    try {
      const created = await api.post<SecurityTask>("/api/tasks", {
        title: rec.title,
        description: rec.cleanText,
      });
      setTasks((current) => [...current, created]);
      setSuccess(`Added "${rec.title}" to your Safety Tasks.`);
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setSuccess(`"${rec.title}" is already tracked in your Safety Tasks.`);
        void api.get<SecurityTask[]>("/api/tasks").then(setTasks).catch(() => {});
      } else {
        setError(err instanceof ApiError ? err.message : "Could not add recommendation to tasks.");
      }
    } finally {
      setAddingRecId(null);
    }
  }

  // Real statistics derived strictly from live data
  const totalRecs = allPreparedRecs.length;
  const trackedRecsCount = allPreparedRecs.filter((r) => Boolean(findMatchingTask(r))).length;
  const completedTasksCount = tasks.filter((t) => t.completed).length;
  const remediationProgress = totalRecs > 0 ? Math.round((trackedRecsCount / totalRecs) * 100) : 0;

  if (loading) {
    return (
      <div className="incident-detail-loading" role="status" aria-live="polite">
        <div className="loading-spinner" />
        <p className="muted">Evaluating incident assessments and loading recommendations…</p>
      </div>
    );
  }

  return (
    <div className="recs-page-container">
      {/* ── Page Header ── */}
      <header className="recs-header-card">
        <div className="recs-header-top">
          <div className="recs-header-title-wrap">
            <h1 className="recs-page-title">
              <ShieldIcon /> Safety Recommendations
            </h1>
            <p className="recs-page-sub">
              Personalized security advisories synthesized from your stored incident assessments using CyberShield's rule-based engine.
            </p>
          </div>
          <Link to="/tasks" className="btn btn-secondary">
            View Safety Tasks →
          </Link>
        </div>

        <div className="recs-methodology-note">
          <InfoIcon />
          <span>
            <strong>How recommendations work:</strong> Rules evaluate reported incident types, vectors, and risk factors to deliver targeted containment, defense, and safe browsing steps.
          </span>
        </div>
      </header>

      {/* ── Feedback Banners ── */}
      {error && (
        <div className="banner error" role="alert">
          <AlertTriangleIcon /> {error}
        </div>
      )}
      {success && (
        <div className="banner success" role="status" aria-live="polite">
          <CheckCircleIcon /> {success}
        </div>
      )}

      {/* ── Global Empty State (No Incidents / Recommendations) ── */}
      {totalRecs === 0 ? (
        <div className="recs-global-empty-card">
          <div className="recs-empty-icon-wrap">
            <ShieldIcon />
          </div>
          <h2 className="recs-empty-title">No Recommendations Available Yet</h2>
          <p className="recs-empty-desc">
            Safety guidance is personalized and generated dynamically from your reported security incidents. Once you log an incident, relevant containment and protective advice will appear here.
          </p>
          <div className="recs-empty-actions">
            <Link to="/incidents/new" className="btn btn-primary">
              Report an Incident
            </Link>
            <Link to="/tasks" className="btn btn-secondary">
              Open Safety Tasks
            </Link>
          </div>
        </div>
      ) : (
        <>
          {/* ── Real Statistics Row ── */}
          <section className="recs-stats-grid" aria-label="Recommendation and Task Metrics">
            <div className="recs-stat-card">
              <div className="recs-stat-val teal">{totalRecs}</div>
              <div className="recs-stat-lbl">Total Recommendations</div>
              <div className="recs-stat-sub">From your incident records</div>
            </div>

            <div className="recs-stat-card">
              <div className="recs-stat-val blue">
                {trackedRecsCount} <span style={{ fontSize: "1rem", color: "var(--muted)" }}>/ {totalRecs}</span>
              </div>
              <div className="recs-stat-lbl">Tracked in Tasks</div>
              <div className="recs-stat-sub">Remediation actions monitored</div>
            </div>

            <div className="recs-stat-card">
              <div className="recs-stat-val amber">{completedTasksCount}</div>
              <div className="recs-stat-lbl">Completed Tasks</div>
              <div className="recs-stat-sub">Personal tasks marked done</div>
            </div>

            <div className="recs-stat-card">
              <div className="recs-stat-val">{remediationProgress}%</div>
              <div className="recs-stat-lbl">Tracking Coverage</div>
              <div className="recs-stat-sub">Recommendations converted</div>
            </div>
          </section>

          {/* ── Filter / Navigation Bar ── */}
          <nav className="recs-filter-bar" aria-label="Recommendation Category Filters">
            <button
              type="button"
              className={`recs-filter-pill ${activeFilter === "ALL" ? "active" : ""}`}
              onClick={() => setActiveFilter("ALL")}
            >
              All Sections <span className="recs-pill-count">{totalRecs}</span>
            </button>
            <button
              type="button"
              className={`recs-filter-pill immediate ${activeFilter === "IMMEDIATE" ? "active" : ""}`}
              onClick={() => setActiveFilter("IMMEDIATE")}
            >
              <ZapIcon /> Immediate Actions <span className="recs-pill-count">{immediateRecs.length}</span>
            </button>
            <button
              type="button"
              className={`recs-filter-pill ${activeFilter === "ACCOUNT_SECURITY" ? "active" : ""}`}
              onClick={() => setActiveFilter("ACCOUNT_SECURITY")}
            >
              <LockIcon /> Account Security <span className="recs-pill-count">{accountSecurityRecs.length}</span>
            </button>
            <button
              type="button"
              className={`recs-filter-pill safeBrowsing ${activeFilter === "SAFE_BROWSING" ? "active" : ""}`}
              onClick={() => setActiveFilter("SAFE_BROWSING")}
            >
              <GlobeIcon /> Safe Browsing <span className="recs-pill-count">{safeBrowsingRecs.length}</span>
            </button>
          </nav>

          {/* ── Sections Container ── */}
          <div className="recs-sections-container">
            {/* ── Section 1: Immediate Actions ── */}
            {(activeFilter === "ALL" || activeFilter === "IMMEDIATE") && (
              <section className="recs-section" aria-labelledby="immediate-heading">
                <div className="recs-section-header">
                  <div className="recs-section-title-wrap">
                    <span className="recs-section-icon immediate">
                      <ZapIcon />
                    </span>
                    <h2 id="immediate-heading" className="recs-section-title">
                      Immediate Actions
                    </h2>
                    <span className="recs-section-badge immediate">
                      {immediateRecs.length} Urgent
                    </span>
                  </div>
                  <p className="recs-section-sub">
                    Critical containment actions to stop ongoing exploitation, protect accounts, and minimize damage.
                  </p>
                </div>

                {immediateRecs.length === 0 ? (
                  <div className="recs-section-empty">
                    No immediate crisis containment actions required based on your reported incidents.
                  </div>
                ) : (
                  <div className="recs-cards-list">
                    {immediateRecs.map((rec) => {
                      const matchedTask = findMatchingTask(rec);
                      const isAdding = addingRecId === rec.id;

                      return (
                        <div key={rec.id} className="rec-card-compact immediate">
                          <div className="rec-card-main">
                            <span className="rec-mini-icon immediate">
                              <AlertTriangleIcon />
                            </span>
                            <div className="rec-content">
                              <span className="rec-title">{rec.title}</span>
                              <span className="rec-desc">{rec.cleanText}</span>
                            </div>
                          </div>

                          <div className="rec-action-wrap">
                            {matchedTask ? (
                              <Link
                                to="/tasks"
                                className={`task-pill-tiny ${matchedTask.completed ? "done" : "pending"}`}
                                title="View this task on the Safety Tasks page"
                              >
                                <CheckCircleIcon /> {matchedTask.completed ? "Completed" : "Added to Tasks"}
                              </Link>
                            ) : (
                              <button
                                type="button"
                                className="btn-add-task-tiny"
                                onClick={() => void handleAddTask(rec)}
                                disabled={isAdding}
                                aria-label={`Add "${rec.title}" to safety tasks`}
                              >
                                <PlusIcon /> {isAdding ? "Adding…" : "Add to Safety Tasks"}
                              </button>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </section>
            )}

            {/* ── Section 2: Account Security ── */}
            {(activeFilter === "ALL" || activeFilter === "ACCOUNT_SECURITY") && (
              <section className="recs-section" aria-labelledby="account-sec-heading">
                <div className="recs-section-header">
                  <div className="recs-section-title-wrap">
                    <span className="recs-section-icon accountSecurity">
                      <LockIcon />
                    </span>
                    <h2 id="account-sec-heading" className="recs-section-title">
                      Account Security
                    </h2>
                    <span className="recs-section-badge accountSecurity">
                      {accountSecurityRecs.length} Defensive
                    </span>
                  </div>
                  <p className="recs-section-sub">
                    Credential hygiene, multi-factor authentication, and identity safeguards to fortify your accounts.
                  </p>
                </div>

                {accountSecurityRecs.length === 0 ? (
                  <div className="recs-section-empty">
                    No account security advisories currently flagged for your recorded incidents.
                  </div>
                ) : (
                  <div className="recs-cards-list">
                    {accountSecurityRecs.map((rec) => {
                      const matchedTask = findMatchingTask(rec);
                      const isAdding = addingRecId === rec.id;

                      return (
                        <div key={rec.id} className="rec-card-compact accountSecurity">
                          <div className="rec-card-main">
                            <span className="rec-mini-icon accountSecurity">
                              <LockIcon />
                            </span>
                            <div className="rec-content">
                              <span className="rec-title">{rec.title}</span>
                              <span className="rec-desc">{rec.cleanText}</span>
                            </div>
                          </div>

                          <div className="rec-action-wrap">
                            {matchedTask ? (
                              <Link
                                to="/tasks"
                                className={`task-pill-tiny ${matchedTask.completed ? "done" : "pending"}`}
                                title="View this task on the Safety Tasks page"
                              >
                                <CheckCircleIcon /> {matchedTask.completed ? "Completed" : "Added to Tasks"}
                              </Link>
                            ) : (
                              <button
                                type="button"
                                className="btn-add-task-tiny"
                                onClick={() => void handleAddTask(rec)}
                                disabled={isAdding}
                                aria-label={`Add "${rec.title}" to safety tasks`}
                              >
                                <PlusIcon /> {isAdding ? "Adding…" : "Add to Safety Tasks"}
                              </button>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </section>
            )}

            {/* ── Section 3: Safe Browsing ── */}
            {(activeFilter === "ALL" || activeFilter === "SAFE_BROWSING") && (
              <section className="recs-section" aria-labelledby="safe-browsing-heading">
                <div className="recs-section-header">
                  <div className="recs-section-title-wrap">
                    <span className="recs-section-icon safeBrowsing">
                      <GlobeIcon />
                    </span>
                    <h2 id="safe-browsing-heading" className="recs-section-title">
                      Safe Browsing
                    </h2>
                    <span className="recs-section-badge safeBrowsing">
                      {safeBrowsingRecs.length} Hygiene
                    </span>
                  </div>
                  <p className="recs-section-sub">
                    Guidance for evaluating unverified links, checking sender domains, and preventing web-based traps.
                  </p>
                </div>

                {safeBrowsingRecs.length === 0 ? (
                  <div className="recs-section-empty">
                    No browsing or link advisories currently flagged for your recorded incidents.
                  </div>
                ) : (
                  <div className="recs-cards-list">
                    {safeBrowsingRecs.map((rec) => {
                      const matchedTask = findMatchingTask(rec);
                      const isAdding = addingRecId === rec.id;

                      return (
                        <div key={rec.id} className="rec-card-compact safeBrowsing">
                          <div className="rec-card-main">
                            <span className="rec-mini-icon safeBrowsing">
                              <GlobeIcon />
                            </span>
                            <div className="rec-content">
                              <span className="rec-title">{rec.title}</span>
                              <span className="rec-desc">{rec.cleanText}</span>
                            </div>
                          </div>

                          <div className="rec-action-wrap">
                            {matchedTask ? (
                              <Link
                                to="/tasks"
                                className={`task-pill-tiny ${matchedTask.completed ? "done" : "pending"}`}
                                title="View this task on the Safety Tasks page"
                              >
                                <CheckCircleIcon /> {matchedTask.completed ? "Completed" : "Added to Tasks"}
                              </Link>
                            ) : (
                              <button
                                type="button"
                                className="btn-add-task-tiny"
                                onClick={() => void handleAddTask(rec)}
                                disabled={isAdding}
                                aria-label={`Add "${rec.title}" to safety tasks`}
                              >
                                <PlusIcon /> {isAdding ? "Adding…" : "Add to Safety Tasks"}
                              </button>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </section>
            )}
          </div>
        </>
      )}

      {/* ── System Disclaimer ── */}
      <footer className="recs-disclaimer-card" aria-label="System advisory notice">
        <div className="recs-disclaimer-header">
          <ShieldIcon />
          <span>Advisory Notice &amp; Protection Scope</span>
        </div>
        <div className="recs-disclaimer-lines">
          {disclaimer.map((line) => (
            <p key={line} className="recs-disclaimer-line">
              {line}
            </p>
          ))}
        </div>
      </footer>
    </div>
  );
}
