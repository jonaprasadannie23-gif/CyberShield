import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, ApiError } from "../api";
import type {
  AssessmentCategory,
  AssessmentComparison,
  AssessmentRecommendation,
  AssessmentRequest,
  FindingStatus,
  PracticeFinding,
  SecurityAssessment,
  SecurityTask,
} from "../types";

// ============================================================================
// Question Definitions & Options
// ============================================================================

interface QuestionOption {
  value: string;
  label: string;
  description?: string;
}

interface QuestionDef {
  key: keyof AssessmentRequest;
  number: number;
  title: string;
  subtitle: string;
  options: QuestionOption[];
}

const QUESTIONS: QuestionDef[] = [
  {
    key: "twoFactorAuth",
    number: 1,
    title: "Two-Factor Authentication (2FA)",
    subtitle: "Do you have two-factor authentication enabled on important accounts?",
    options: [
      {
        value: "ALWAYS",
        label: "Enabled everywhere important",
        description: "Active on email, banking, cloud storage, and password managers.",
      },
      {
        value: "SOMETIMES",
        label: "Enabled on some accounts",
        description: "Turned on for a few services, but not yet all critical accounts.",
      },
      {
        value: "NEVER",
        label: "Not enabled on any accounts",
        description: "Accounts rely exclusively on single passwords.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure / Unverified",
        description: "Uncertain if 2FA is active on personal accounts.",
      },
    ],
  },
  {
    key: "passwordReuse",
    number: 2,
    title: "Password Reuse Across Accounts",
    subtitle: "How often do you reuse passwords or variations across websites?",
    options: [
      {
        value: "NEVER",
        label: "Never reuse passwords",
        description: "Every account has a distinct, independent password.",
      },
      {
        value: "RARELY",
        label: "Rarely (throwaway sites only)",
        description: "Primary accounts are unique; minor sites may share one.",
      },
      {
        value: "SOMETIMES",
        label: "Shared across several accounts",
        description: "A few favorite passwords are used across multiple services.",
      },
      {
        value: "FREQUENTLY",
        label: "Same password everywhere",
        description: "One standard password is used for nearly everything.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure about overlap",
        description: "Uncertain how many accounts share identical passwords.",
      },
    ],
  },
  {
    key: "strongPasswords",
    number: 3,
    title: "Password Strength & Generation",
    subtitle: "What is your approach to password complexity and length?",
    options: [
      {
        value: "MANAGER_OR_STRONG",
        label: "Password manager / 16+ character random",
        description: "Use a password manager to generate long, unpredictable passphrases.",
      },
      {
        value: "MEMORIZED_COMPLEX",
        label: "Complex memorized passphrases",
        description: "12+ characters with mixed casing, digits, and symbols.",
      },
      {
        value: "SIMPLE_PATTERNS",
        label: "Short words, names, or simple patterns",
        description: "Dictionary words, birthdays, names, or basic substitutions.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure how strong they are",
        description: "Have not tested passwords against complexity guidelines.",
      },
    ],
  },
  {
    key: "systemUpdates",
    number: 4,
    title: "Operating System & Application Updates",
    subtitle: "How promptly do you install system updates and security patches?",
    options: [
      {
        value: "AUTOMATIC",
        label: "Automatic updates enabled",
        description: "Updates and security patches install automatically and prompt restarts.",
      },
      {
        value: "REGULAR_MANUAL",
        label: "Regular manual checks",
        description: "Check for and apply updates every few weeks.",
      },
      {
        value: "DELAYED",
        label: "Postponed for months",
        description: "Frequently delay, ignore, or disable update prompts.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure / Don't check",
        description: "Uncertain whether devices are up to date.",
      },
    ],
  },
  {
    key: "linkVerification",
    number: 5,
    title: "Verification of Suspicious Links",
    subtitle: "What do you do before clicking links in unsolicited messages or emails?",
    options: [
      {
        value: "ALWAYS_VERIFY",
        label: "Always verify sender & URL destination",
        description: "Hover over URLs, check domain names, and navigate directly if unsure.",
      },
      {
        value: "SOMETIMES_VERIFY",
        label: "Verify only obvious red flags",
        description: "Check links only when messages look noticeably suspicious.",
      },
      {
        value: "RARELY_VERIFY",
        label: "Rarely or never verify links",
        description: "Usually click links directly without checking destination URLs.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure how to verify URLs",
        description: "Unfamiliar with previewing URLs or identifying spoofed domains.",
      },
    ],
  },
  {
    key: "recoveryMethods",
    number: 6,
    title: "Secure Account Recovery Methods",
    subtitle: "How are your account recovery and backup options configured?",
    options: [
      {
        value: "UP_TO_DATE_SECURE",
        label: "Secure secondary contact & offline codes",
        description: "Dedicated backup email and offline one-time recovery codes stored safely.",
      },
      {
        value: "BASIC_EMAIL_SMS",
        label: "Standard SMS or primary email only",
        description: "Rely on phone SMS or main email without offline backup codes.",
      },
      {
        value: "OUTDATED_OR_WEAK",
        label: "Outdated contacts or predictable questions",
        description: "Old phone number on file or easily guessable security questions.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure what is configured",
        description: "Have not reviewed account recovery settings recently.",
      },
    ],
  },
  {
    key: "publicWifiPrecautions",
    number: 7,
    title: "Precautions on Public Wi-Fi",
    subtitle: "What precautions do you take when using public Wi-Fi networks?",
    options: [
      {
        value: "ALWAYS_PROTECTED",
        label: "VPN or mobile hotspot",
        description: "Always route public connections through a VPN or cellular hotspot.",
      },
      {
        value: "NEVER_USE_PUBLIC",
        label: "Never connect to public Wi-Fi",
        description: "Strictly avoid connecting to open or untrusted networks.",
      },
      {
        value: "BASIC_HTTPS",
        label: "Browse on public Wi-Fi with HTTPS",
        description: "Connect directly but rely on HTTPS and avoid banking.",
      },
      {
        value: "NO_PRECAUTIONS",
        label: "Connect freely without precautions",
        description: "Connect to any available Wi-Fi and use accounts normally.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure about public Wi-Fi risks",
        description: "Unaware of precautions needed on open networks.",
      },
    ],
  },
  {
    key: "phishingRecognition",
    number: 8,
    title: "Recognition of Phishing & Impersonation",
    subtitle: "How confident are you in spotting fraudulent communications?",
    options: [
      {
        value: "VERY_CONFIDENT",
        label: "Very confident recognizing scams",
        description: "Spot artificial urgency, verify unexpected requests, and check senders.",
      },
      {
        value: "SOMEWHAT_CONFIDENT",
        label: "Confident with obvious scams only",
        description: "Catch crude spam, but unsure about targeted impersonations.",
      },
      {
        value: "NOT_CONFIDENT",
        label: "Struggle to tell real from fake",
        description: "Find it challenging to distinguish legitimate messages from fraud.",
      },
      {
        value: "NOT_SURE",
        label: "Not sure what phishing looks like",
        description: "Unfamiliar with modern phishing and social engineering tactics.",
      },
    ],
  },
];

const EMPTY_FORM: AssessmentRequest = {
  twoFactorAuth: "",
  passwordReuse: "",
  strongPasswords: "",
  systemUpdates: "",
  linkVerification: "",
  recoveryMethods: "",
  publicWifiPrecautions: "",
  phishingRecognition: "",
};

// ============================================================================
// Helper mapping functions
// ============================================================================

interface SimplifiedStatus {
  label: "Good" | "Moderate" | "Needs Attention";
  className: "status-good" | "status-moderate" | "status-attention";
}

function getSimplifiedStatus(status: FindingStatus): SimplifiedStatus {
  switch (status) {
    case "SECURE":
      return { label: "Good", className: "status-good" };
    case "MODERATE":
      return { label: "Moderate", className: "status-moderate" };
    case "VULNERABLE":
    case "NEEDS_ATTENTION":
    default:
      return { label: "Needs Attention", className: "status-attention" };
  }
}

function getCategoryColor(category: AssessmentCategory): string {
  switch (category) {
    case "EXCELLENT":
      return "var(--risk-low)";
    case "GOOD":
      return "var(--accent-teal)";
    case "FAIR":
      return "var(--risk-medium)";
    case "POOR":
      return "var(--risk-high)";
  }
}

// ============================================================================
// Main Component
// ============================================================================

export function AssessmentPage() {
  const { id } = useParams<{ id?: string }>();
  const navigate = useNavigate();

  // Navigation tab state
  const [activeTab, setActiveTab] = useState<"questionnaire" | "results" | "history">("questionnaire");

  // Wizard step state (0 to 7)
  const [currentStep, setCurrentStep] = useState(0);
  const [stepError, setStepError] = useState("");

  // Answers & submission state
  const [answers, setAnswers] = useState<AssessmentRequest>(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  // Loaded data state
  const [currentAssessment, setCurrentAssessment] = useState<SecurityAssessment | null>(null);
  const [historyList, setHistoryList] = useState<SecurityAssessment[]>([]);
  const [comparison, setComparison] = useState<AssessmentComparison | null>(null);
  const [tasks, setTasks] = useState<SecurityTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Task conversion state
  const [addingTaskId, setAddingTaskId] = useState<string | null>(null);
  const [taskFeedback, setTaskFeedback] = useState<{ id: string; msg: string; isError?: boolean } | null>(null);

  // ── Load Assessment Data ──────────────────────────────────────────────────
  useEffect(() => {
    let isMounted = true;
    setLoading(true);
    setError("");

    Promise.all([
      api.get<SecurityAssessment[]>("/api/assessments").catch(() => [] as SecurityAssessment[]),
      api.get<SecurityTask[]>("/api/tasks").catch(() => [] as SecurityTask[]),
      api.get<AssessmentComparison>("/api/assessments/comparison").catch(() => null),
    ])
      .then(([history, userTasks, comp]) => {
        if (!isMounted) return;
        setHistoryList(history || []);
        setTasks(userTasks || []);
        setComparison(comp);

        if (id) {
          const target = history.find((a) => a.assessmentId === id);
          if (target) {
            setCurrentAssessment(target);
            setActiveTab("results");
          } else {
            api
              .get<SecurityAssessment>(`/api/assessments/${id}`)
              .then((fetched) => {
                if (!isMounted) return;
                setCurrentAssessment(fetched);
                setActiveTab("results");
              })
              .catch((err) => {
                if (!isMounted) return;
                setError(err instanceof ApiError ? err.message : "Assessment not found.");
              });
          }
        } else {
          if (history.length > 0) {
            setCurrentAssessment(history[0]);
          }
          setActiveTab("questionnaire");
        }
      })
      .catch((err) => {
        if (!isMounted) return;
        setError(err instanceof ApiError ? err.message : "Failed to load assessments.");
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [id]);

  // ── Wizard Controls ───────────────────────────────────────────────────────
  const currentQuestion = QUESTIONS[currentStep];
  const selectedAnswer = answers[currentQuestion.key];
  const isLastQuestion = currentStep === QUESTIONS.length - 1;

  function handleSelectOption(value: string) {
    setAnswers((prev) => ({ ...prev, [currentQuestion.key]: value }));
    setStepError("");
  }

  function handleNext() {
    if (!answers[currentQuestion.key]) {
      setStepError("Please select an answer to continue.");
      return;
    }
    setStepError("");
    if (!isLastQuestion) {
      setCurrentStep((s) => s + 1);
    }
  }

  function handlePrevious() {
    setStepError("");
    if (currentStep > 0) {
      setCurrentStep((s) => s - 1);
    }
  }

  async function handleSubmit() {
    if (!answers[currentQuestion.key]) {
      setStepError("Please select an answer to submit.");
      return;
    }

    // Verify all 8 are answered
    const unanswered = QUESTIONS.find((q) => !answers[q.key]);
    if (unanswered) {
      setStepError(`Please answer question ${unanswered.number} before submitting.`);
      setCurrentStep(unanswered.number - 1);
      return;
    }

    setSubmitting(true);
    setSubmitError("");
    setStepError("");

    try {
      const result = await api.post<SecurityAssessment>("/api/assessments", answers);
      const updatedComp = await api.get<AssessmentComparison>("/api/assessments/comparison").catch(() => null);
      setCurrentAssessment(result);
      setComparison(updatedComp);
      setHistoryList((prev) => [result, ...prev]);
      setActiveTab("results");
      setAnswers(EMPTY_FORM);
      setCurrentStep(0);
      navigate("/assessment");
    } catch (err) {
      setSubmitError(err instanceof ApiError ? err.message : "Failed to calculate assessment.");
    } finally {
      setSubmitting(false);
    }
  }

  function handleStartRetake() {
    setAnswers(EMPTY_FORM);
    setCurrentStep(0);
    setStepError("");
    setSubmitError("");
    setActiveTab("questionnaire");
  }

  // ── Safety Task Integration ───────────────────────────────────────────────
  function isTaskTracked(rec: AssessmentRecommendation): boolean {
    const cleanTitle = rec.taskTitle.trim().toLowerCase();
    const cleanDesc = (rec.taskDescription || "").trim().toLowerCase();
    return tasks.some(
      (t) =>
        t.title.trim().toLowerCase() === cleanTitle ||
        (cleanDesc && t.description && t.description.trim().toLowerCase() === cleanDesc)
    );
  }

  function isTaskCompleted(rec: AssessmentRecommendation): boolean {
    const cleanTitle = rec.taskTitle.trim().toLowerCase();
    return tasks.some((t) => t.completed && t.title.trim().toLowerCase() === cleanTitle);
  }

  async function handleConvertToTask(rec: AssessmentRecommendation) {
    setAddingTaskId(rec.recommendationId);
    setTaskFeedback(null);

    try {
      const created = await api.post<SecurityTask>("/api/tasks", {
        title: rec.taskTitle,
        description: rec.taskDescription,
      });
      setTasks((prev) => [...prev, created]);
      setTaskFeedback({
        id: rec.recommendationId,
        msg: `Added to Safety Tasks.`,
      });
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setTaskFeedback({
          id: rec.recommendationId,
          msg: `Already tracked in Safety Tasks.`,
        });
        void api.get<SecurityTask[]>("/api/tasks").then(setTasks).catch(() => {});
      } else {
        setTaskFeedback({
          id: rec.recommendationId,
          msg: err instanceof ApiError ? err.message : "Failed to create task.",
          isError: true,
        });
      }
    } finally {
      setAddingTaskId(null);
    }
  }

  if (loading) {
    return (
      <div className="assessment-loading-state" role="status">
        <div className="loading-spinner" />
        <p className="muted">Loading personal security assessment…</p>
      </div>
    );
  }

  // Practices needing attention count
  const attentionCount = currentAssessment
    ? currentAssessment.findings.filter((f) => f.status !== "SECURE").length
    : 0;

  // Comparison fallback derived strictly from server-loaded assessment history if comparison not yet loaded
  const activeComparison: AssessmentComparison | null =
    comparison ??
    (historyList.length > 0
      ? historyList.length >= 2
        ? {
            latestScore: historyList[0].score,
            previousScore: historyList[1].score,
            scoreDifference: historyList[0].score - historyList[1].score,
            signedDifference:
              historyList[0].score - historyList[1].score > 0
                ? `+${historyList[0].score - historyList[1].score}`
                : String(historyList[0].score - historyList[1].score),
            status:
              historyList[0].score > historyList[1].score
                ? "IMPROVED"
                : historyList[0].score < historyList[1].score
                ? "WORSENED"
                : "NO_CHANGE",
            statusMessage:
              historyList[0].score > historyList[1].score
                ? "Improved"
                : historyList[0].score < historyList[1].score
                ? "Worsened"
                : "No change",
            hasComparison: true,
            hasHistory: true,
          }
        : {
            latestScore: historyList[0].score,
            previousScore: null,
            scoreDifference: null,
            signedDifference: null,
            status: "SINGLE_ASSESSMENT",
            statusMessage: "Complete another assessment to compare your progress.",
            hasComparison: false,
            hasHistory: true,
          }
      : null);

  return (
    <div className="assessment-page">
      {/* ── Top Header & Tab Navigation ── */}
      <div className="assessment-top-bar">
        <div>
          <h1 className="page-title">Personal Security Assessment</h1>
          <p className="page-sub">
            Evaluate your security posture, spot critical gaps, and create preventive tasks.
          </p>
        </div>

        <div className="assessment-nav-tabs" role="tablist">
          <button
            type="button"
            className={`assessment-nav-tab ${activeTab === "questionnaire" ? "active" : ""}`}
            onClick={() => setActiveTab("questionnaire")}
            role="tab"
            aria-selected={activeTab === "questionnaire"}
          >
            Questionnaire
          </button>
          <button
            type="button"
            className={`assessment-nav-tab ${activeTab === "results" ? "active" : ""}`}
            onClick={() => {
              if (currentAssessment) setActiveTab("results");
              else setActiveTab("questionnaire");
            }}
            disabled={!currentAssessment}
            role="tab"
            aria-selected={activeTab === "results"}
          >
            Results
          </button>
          <button
            type="button"
            className={`assessment-nav-tab ${activeTab === "history" ? "active" : ""}`}
            onClick={() => setActiveTab("history")}
            role="tab"
            aria-selected={activeTab === "history"}
          >
            History ({historyList.length})
          </button>
        </div>
      </div>

      {error && <div className="banner error">{error}</div>}

      {/* ────────────────────────────────────────────────────────────────────── */}
      {/* 1. QUESTIONNAIRE WIZARD (One Question at a Time)                       */}
      {/* ────────────────────────────────────────────────────────────────────── */}
      {activeTab === "questionnaire" && (
        <div className="assessment-wizard-container">
          <div className="wizard-card">
            {/* Compact Progress Header */}
            <div className="wizard-progress-header">
              <span className="wizard-step-label">
                Question {currentQuestion.number} of {QUESTIONS.length}
              </span>
              <div className="wizard-progress-bar">
                <div
                  className="wizard-progress-fill"
                  style={{ width: `${((currentStep + 1) / QUESTIONS.length) * 100}%` }}
                />
              </div>
            </div>

            {/* Question Details */}
            <div className="wizard-question-body">
              <h2 className="wizard-question-title">{currentQuestion.title}</h2>
              <p className="wizard-question-sub">{currentQuestion.subtitle}</p>

              {/* Answer Options: Compact Full-Width Cards */}
              <div className="wizard-options-list" role="radiogroup" aria-label={currentQuestion.title}>
                {currentQuestion.options.map((opt) => {
                  const isSelected = selectedAnswer === opt.value;
                  const optionInputId = `opt-${String(currentQuestion.key)}-${opt.value}`;
                  return (
                    <label
                      key={opt.value}
                      htmlFor={optionInputId}
                      className={`wizard-option-card ${isSelected ? "selected" : ""}`}
                    >
                      <input
                        id={optionInputId}
                        type="radio"
                        name={String(currentQuestion.key)}
                        value={opt.value}
                        checked={isSelected}
                        onChange={() => handleSelectOption(opt.value)}
                        className="wizard-radio-input"
                      />
                      <div className="wizard-option-content">
                        <span className="wizard-option-label">{opt.label}</span>
                        {opt.description && (
                          <span className="wizard-option-desc">{opt.description}</span>
                        )}
                      </div>
                    </label>
                  );
                })}
              </div>

              {/* Inline Validation Message */}
              {stepError && <div className="wizard-inline-error">{stepError}</div>}
              {submitError && <div className="banner error">{submitError}</div>}
            </div>

            {/* Bottom Navigation Controls */}
            <div className="wizard-footer">
              <button
                type="button"
                className="btn btn-secondary wizard-btn-prev"
                onClick={handlePrevious}
                disabled={currentStep === 0 || submitting}
              >
                Previous
              </button>

              {isLastQuestion ? (
                <button
                  type="button"
                  className="btn btn-primary wizard-btn-submit"
                  onClick={handleSubmit}
                  disabled={submitting}
                >
                  {submitting ? "Evaluating Assessment…" : "Submit Assessment"}
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-primary wizard-btn-next"
                  onClick={handleNext}
                >
                  Next
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ────────────────────────────────────────────────────────────────────── */}
      {/* 2. RESULTS & ACTION PLAN                                               */}
      {/* ────────────────────────────────────────────────────────────────────── */}
      {activeTab === "results" && currentAssessment && (
        <div className="assessment-results-container">
          {/* Top Results Summary */}
          <div className="results-summary-card">
            <div className="summary-left-group">
              <div
                className="summary-score-box"
                style={{ borderColor: getCategoryColor(currentAssessment.category) }}
              >
                <span
                  className="summary-score-value"
                  style={{ color: getCategoryColor(currentAssessment.category) }}
                >
                  {currentAssessment.score}
                </span>
                <span className="summary-score-max">/100</span>
              </div>

              <div className="summary-text-group">
                <div className="summary-category-row">
                  <span
                    className="summary-category-badge"
                    style={{
                      borderColor: getCategoryColor(currentAssessment.category),
                      color: getCategoryColor(currentAssessment.category),
                    }}
                  >
                    {currentAssessment.categoryLabel}
                  </span>
                  <span className="summary-attention-stat">
                    {attentionCount === 0
                      ? "All 8 practices in good standing"
                      : `${attentionCount} practice${attentionCount === 1 ? "" : "s"} need attention`}
                  </span>
                </div>
                <p className="summary-sentence">{currentAssessment.riskSummary}</p>
              </div>
            </div>

            <div className="summary-actions">
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleStartRetake}
              >
                Retake Assessment
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setActiveTab("history")}
              >
                View History
              </button>
            </div>
          </div>

          {/* Assessment Progress Section */}
          {activeComparison && activeComparison.hasHistory && (
            <div className="results-section assessment-progress-section">
              <h3 className="section-heading">Assessment Progress</h3>
              {activeComparison.hasComparison ? (
                <div className={`assessment-progress-card progress-${activeComparison.status.toLowerCase()}`}>
                  <div className="progress-stat">
                    <span className="progress-label">Previous score</span>
                    <span className="progress-val">{activeComparison.previousScore}</span>
                  </div>
                  <div className="progress-stat">
                    <span className="progress-label">Latest score</span>
                    <span className="progress-val">{activeComparison.latestScore}</span>
                  </div>
                  <div className="progress-stat">
                    <span className="progress-label">Score difference</span>
                    <span className={`progress-diff diff-${activeComparison.status.toLowerCase()}`}>
                      {activeComparison.signedDifference}
                    </span>
                  </div>
                  <div className="progress-stat">
                    <span className="progress-label">Status</span>
                    <span className={`progress-badge badge-${activeComparison.status.toLowerCase()}`}>
                      {activeComparison.statusMessage}
                    </span>
                  </div>
                </div>
              ) : (
                <div className="assessment-progress-card progress-neutral">
                  <p className="progress-message">Complete another assessment to compare your progress.</p>
                </div>
              )}
            </div>
          )}

          {/* Individual Practice Findings: Concise Two-Column Grid */}
          <div className="results-section">
            <h3 className="section-heading">Individual Practice Findings</h3>
            <div className="findings-two-col-grid">
              {currentAssessment.findings.map((f: PracticeFinding) => {
                const statusInfo = getSimplifiedStatus(f.status);
                const needsImprovement = f.status !== "SECURE";

                return (
                  <div key={f.practiceKey} className="finding-compact-card">
                    <div className="finding-compact-header">
                      <span className="finding-name">{f.title}</span>
                      <span className={`finding-badge ${statusInfo.className}`}>
                        {statusInfo.label}
                      </span>
                    </div>
                    <div className="finding-compact-rec">
                      {needsImprovement && f.recommendation ? (
                        f.recommendation
                      ) : (
                        <span className="finding-rec-ok">No action needed.</span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Actionable Safety Tasks Integration */}
          {currentAssessment.recommendations.length > 0 && (
            <div className="results-section">
              <h3 className="section-heading">Recommended Safety Tasks</h3>
              <div className="tasks-action-list">
                {currentAssessment.recommendations.map((rec) => {
                  const isTracked = isTaskTracked(rec);
                  const isCompleted = isTaskCompleted(rec);
                  const isBusy = addingTaskId === rec.recommendationId;
                  const feedback =
                    taskFeedback && taskFeedback.id === rec.recommendationId
                      ? taskFeedback
                      : null;

                  return (
                    <div key={rec.recommendationId} className="task-action-row">
                      <div className="task-action-info">
                        <span className="task-action-title">{rec.taskTitle}</span>
                        {rec.taskDescription && (
                          <span className="task-action-desc">{rec.taskDescription}</span>
                        )}
                      </div>

                      <div className="task-action-controls">
                        {isCompleted ? (
                          <span className="task-status-pill completed">✓ Completed</span>
                        ) : isTracked ? (
                          <span className="task-status-pill tracked">✓ Tracked</span>
                        ) : (
                          <button
                            type="button"
                            className="btn btn-secondary btn-task-add"
                            onClick={() => handleConvertToTask(rec)}
                            disabled={isBusy}
                          >
                            {isBusy ? "Adding…" : "+ Add to Tasks"}
                          </button>
                        )}
                      </div>

                      {feedback && (
                        <div
                          className={`task-inline-msg ${feedback.isError ? "error" : "success"}`}
                        >
                          {feedback.msg}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}

      {/* ────────────────────────────────────────────────────────────────────── */}
      {/* 3. ASSESSMENT HISTORY                                                  */}
      {/* ────────────────────────────────────────────────────────────────────── */}
      {activeTab === "history" && (
        <div className="assessment-history-container">
          <div className="history-top-card">
            <div>
              <h2 className="history-title">Assessment History</h2>
              <p className="history-subtitle">
                Compare score changes over time to verify the impact of your security improvements.
              </p>
            </div>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleStartRetake}
            >
              Take New Assessment
            </button>
          </div>

          {historyList.length === 0 ? (
            <div className="history-empty-card">
              <p className="history-empty-title">No completed assessments recorded</p>
              <p className="muted">
                Complete the questionnaire to establish your baseline personal security score.
              </p>
              <button
                type="button"
                className="btn btn-primary"
                style={{ marginTop: 14 }}
                onClick={handleStartRetake}
              >
                Start Assessment
              </button>
            </div>
          ) : (
            <div className="history-card-list">
              {historyList.map((item, index) => {
                const olderItem = historyList[index + 1];
                const delta = olderItem ? item.score - olderItem.score : null;
                const isSelected = currentAssessment?.assessmentId === item.assessmentId;

                return (
                  <div
                    key={item.assessmentId}
                    className={`history-entry-card ${isSelected ? "active" : ""}`}
                  >
                    <div className="history-entry-left">
                      <div
                        className="history-entry-score"
                        style={{ borderColor: getCategoryColor(item.category) }}
                      >
                        <span
                          className="history-entry-val"
                          style={{ color: getCategoryColor(item.category) }}
                        >
                          {item.score}
                        </span>
                        <span className="history-entry-den">/100</span>
                      </div>

                      <div className="history-entry-meta">
                        <div className="history-entry-tags">
                          <span
                            className="history-category-pill"
                            style={{
                              borderColor: getCategoryColor(item.category),
                              color: getCategoryColor(item.category),
                            }}
                          >
                            {item.categoryLabel}
                          </span>
                          <span className="history-entry-date">
                            {new Date(item.createdAt).toLocaleDateString(undefined, {
                              year: "numeric",
                              month: "short",
                              day: "numeric",
                            })}{" "}
                            at{" "}
                            {new Date(item.createdAt).toLocaleTimeString([], {
                              hour: "2-digit",
                              minute: "2-digit",
                            })}
                          </span>
                          {index === 0 && <span className="history-latest-pill">Latest</span>}
                        </div>
                        <p className="history-entry-summary">{item.riskSummary}</p>
                      </div>
                    </div>

                    <div className="history-entry-right">
                      <div className="history-trend-indicator">
                        {delta !== null ? (
                          delta > 0 ? (
                            <span className="trend-pill positive">▲ +{delta} pts</span>
                          ) : delta < 0 ? (
                            <span className="trend-pill negative">▼ {delta} pts</span>
                          ) : (
                            <span className="trend-pill neutral">▬ No change</span>
                          )
                        ) : (
                          <span className="trend-pill baseline">Baseline</span>
                        )}
                      </div>

                      <button
                        type="button"
                        className="btn btn-secondary btn-entry-view"
                        onClick={() => {
                          setCurrentAssessment(item);
                          setActiveTab("results");
                        }}
                      >
                        View Results
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
