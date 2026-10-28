export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";
export type IncidentStatus = "REPORTED" | "UNDER_REVIEW" | "RESOLVED";
export type IncidentType =
  | "PHISHING"
  | "SUSPICIOUS_LINK"
  | "OTP_SCAM"
  | "IMPERSONATION"
  | "SUSPICIOUS_EMAIL"
  | "OTHER";
export type IncidentSource =
  | "SMS"
  | "EMAIL"
  | "WEBSITE"
  | "SOCIAL_MEDIA"
  | "PHONE_CALL"
  | "OTHER";

export interface User {
  userId: string;
  username: string;
  email: string;
  fullName: string;
  registeredAt: string;
  lastLoginAt: string | null;
}

export interface Incident {
  incidentId: string;
  userId: string;
  title: string;
  type: IncidentType;
  description: string;
  source: IncidentSource;
  incidentDate: string;
  suspiciousUrl: string;
  riskLevel: RiskLevel;
  status: IncidentStatus;
  reportedAt: string;
  assessmentReasons: string[];
  recommendations: string[];
  typeSpecificDetails: string;
  claimedToBeLegitOrg?: boolean;
  containedAttachment?: boolean;
  linkWasClicked?: boolean;
  requestedCredentials?: boolean;
  otpWasShared?: boolean;
  financialInfoShared?: boolean;
}

export interface SecurityTask {
  taskId: string;
  userId: string;
  title: string;
  description: string;
  completed: boolean;
  createdAt: string;
  completedAt: string | null;
  relatedIncidentId?: string | null;
}

export interface DashboardData {
  totalIncidents: number;
  highRiskCount: number;
  unresolvedCount: number;
  pendingTasks: number;
  taskCompletionPercent: number;
  recentIncidents: Incident[];
  pendingTaskList: SecurityTask[];
  byRisk: Record<RiskLevel, number>;
  byType: Record<IncidentType, number>;
  byStatus: Record<IncidentStatus, number>;
}

export interface ReportData {
  totalIncidents: number;
  resolvedCount: number;
  unresolvedCount: number;
  byRisk: Record<RiskLevel, number>;
  byType: Record<IncidentType, number>;
  byStatus: Record<IncidentStatus, number>;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  taskCompletionPercent: number;
}

export interface RecommendationsData {
  recommendations: string[];
  disclaimer: string[];
  totalIncidents?: number;
  totalTasks?: number;
  completedTasks?: number;
}

export const TYPE_LABELS: Record<IncidentType, string> = {
  PHISHING: "Phishing",
  SUSPICIOUS_LINK: "Suspicious Link",
  OTP_SCAM: "OTP Scam",
  IMPERSONATION: "Impersonation",
  SUSPICIOUS_EMAIL: "Suspicious Email",
  OTHER: "Other",
};

export const SOURCE_LABELS: Record<IncidentSource, string> = {
  SMS: "SMS",
  EMAIL: "Email",
  WEBSITE: "Website",
  SOCIAL_MEDIA: "Social Media",
  PHONE_CALL: "Phone Call",
  OTHER: "Other",
};

export const STATUS_LABELS: Record<IncidentStatus, string> = {
  REPORTED: "Reported",
  UNDER_REVIEW: "Under Review",
  RESOLVED: "Resolved",
};

// ── Personal Security Assessment Types ───────────────────────────────────────

export type AssessmentCategory = "EXCELLENT" | "GOOD" | "FAIR" | "POOR";
export type FindingStatus = "SECURE" | "MODERATE" | "VULNERABLE" | "NEEDS_ATTENTION";
export type RecommendationPriority = "HIGH" | "MEDIUM" | "LOW";

export interface PracticeFinding {
  practiceKey: string;
  title: string;
  answerKey: string;
  answerLabel: string;
  score: number;
  maxScore: number;
  status: FindingStatus;
  explanation: string;
  recommendation: string | null;
  taskTitle: string | null;
  taskDescription: string | null;
}

export interface AssessmentRecommendation {
  recommendationId: string;
  practiceKey: string;
  title: string;
  priority: RecommendationPriority;
  description: string;
  taskTitle: string;
  taskDescription: string;
}

export interface SecurityAssessment {
  assessmentId: string;
  userId: string;
  score: number;
  category: AssessmentCategory;
  categoryLabel: string;
  riskSummary: string;
  scoreInterpretation: string;
  disclaimer: string;
  answers: Record<string, string>;
  findings: PracticeFinding[];
  recommendations: AssessmentRecommendation[];
  createdAt: string;
}

export interface AssessmentRequest {
  twoFactorAuth: string;
  passwordReuse: string;
  strongPasswords: string;
  systemUpdates: string;
  linkVerification: string;
  recoveryMethods: string;
  publicWifiPrecautions: string;
  phishingRecognition: string;
}

export const ASSESSMENT_CATEGORY_LABELS: Record<AssessmentCategory, string> = {
  EXCELLENT: "Excellent Security Posture",
  GOOD: "Good Security Posture",
  FAIR: "Fair Security Posture",
  POOR: "At-Risk Security Posture",
};

export const FINDING_STATUS_LABELS: Record<FindingStatus, string> = {
  SECURE: "Secure",
  MODERATE: "Moderate Risk",
  VULNERABLE: "Vulnerable",
  NEEDS_ATTENTION: "Needs Attention",
};

export type ComparisonStatus =
  | "IMPROVED"
  | "WORSENED"
  | "NO_CHANGE"
  | "SINGLE_ASSESSMENT"
  | "NO_HISTORY";

export interface AssessmentComparison {
  latestScore: number | null;
  previousScore: number | null;
  scoreDifference: number | null;
  signedDifference: string | null;
  statusMessage: string | null;
  status: ComparisonStatus;
  hasComparison: boolean;
  hasHistory: boolean;
  latestAssessmentId?: string | null;
  previousAssessmentId?: string | null;
}
